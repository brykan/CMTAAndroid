package co.createlou.cmta;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bryan on 3/6/17.
 */

public class IssueView extends AppCompatActivity implements IssueFragment.OnCompleteListener {

    private static final String TAG = "SaveDetails";

    FragmentManager fm = getSupportFragmentManager();
    public ListView mListView;
    public ArrayList<Issue> issueList = new ArrayList<>();
    public Report myReport;
    public String reportKey;
    public IssueFragment issueFragment;
    public int position;
    FirebaseListAdapter mAdapter;
    ArrayList<String> imageKeys = new ArrayList<>();
    final long THREE_MEGABYTES = 1024*1024*3;
    DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference().child("reports");
    DatabaseReference issueRef = FirebaseDatabase.getInstance().getReference().child("issues").child(reportKey);
    public byte[] imageData;
    public BitmapDrawable bdrawable;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_view);
        //pulling issue data if need be
        issueRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        //Pulling the project keys and creating a hashmap of the project data
                        String key = snap.getKey();
                        HashMap<String,String> issueMap = (HashMap<String,String>) snap.getValue();
                        //converting the hashmap data into a project object
                        String issueLocation = issueMap.get("issueLocation");
                        String issueStatus = issueMap.get("issueStatus");
                        String issueDetails = issueMap.get("issueDetails");
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference pathReference = storageRef.child("images/"+key);
                        storageRef.getBytes(THREE_MEGABYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                imageData = bytes;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                        bdrawable = new BitmapDrawable(getResources(),bitmap);
                        Issue saveIssue = new Issue(issueLocation,issueStatus,issueDetails,imageData,bdrawable);
                        //utilizing the project objects and key data
                        issueList.add(saveIssue);
                        imageKeys.add(key);
                        Log.d("MAIN", key);
                        Log.d("MAIN","issue added with details "+issueLocation + " " + issueStatus + " "+issueDetails);
                    }
                }
            }
            public void onCancelled (DatabaseError firebaseError){
                //doing nothing for now // TODO: 3/27/2017 Add Error Handling
            }
        });
        //Setting up the Toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Issues");
        // Get report data passed from previous activity
        position = this.getIntent().getExtras().getInt("position");
        myReport = this.getIntent().getExtras().getParcelable("report_parcel");
        reportKey = this.getIntent().getExtras().getString("key");
        //Setting up listView and firebaseadapter
        mAdapter = new FirebaseListAdapter<Issue>(this, Issue.class, R.layout.issuelistrow, issueRef) {
            @Override
            protected void populateView(View view, Issue issue, int position) {
                ((TextView)view.findViewById(R.id.issueLocation)).setText(issue.getIssueLocation());
                ((TextView)view.findViewById(R.id.issueDetails)).setText(issue.getIssueDetails());
                ((TextView)view.findViewById(R.id.issueStatus)).setText(issue.getIssueStatus());
                (view.findViewById(R.id.issueImage)).setBackground(issue.getIssueImage());
            }
        };
        mListView = (ListView) findViewById(R.id.issueList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Issue selectedIssue = issueList.get(position);
                Bundle args = new Bundle();
                args.putParcelable("project_parcel", selectedIssue);
                IssueFragment issueFragment = new IssueFragment();
                issueFragment.setArguments(args);
                issueFragment.show(fm, "Android Dialog");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_issue) {
            issueFragment = new IssueFragment();
            // Show Alert DialogFragment
            issueFragment.show(fm, "Android Dialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onComplete(Issue newIssue) {
        issueList.add(newIssue);
        String issueKey = issueRef.push().getKey();
        issueRef.child(issueKey).setValue(newIssue);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}