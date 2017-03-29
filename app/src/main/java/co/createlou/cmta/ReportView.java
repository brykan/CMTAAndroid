package co.createlou.cmta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bryan on 3/6/17.
 */

public class ReportView extends AppCompatActivity implements ReportFragment.OnCompleteListener {

    private static final String TAG = "SaveDetails";

    FragmentManager fm = getSupportFragmentManager();
    public ListView mListView;
    public Project myProject;
    public ArrayList<Report> reportList = new ArrayList<>();
    public ReportFragment reportFragment;
    final Context context = this;
    private String projectKey;
    FirebaseListAdapter mAdapter;



    DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference().child("projects");
    DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference().child("reports").child(projectKey);


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view);
        reportRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        //Pulling the project keys and creating a hashmap of the project data
                        String key = snap.getKey();
                        HashMap<String,String> reportMap = (HashMap<String,String>) snap.getValue();
                        //converting the hashmap data into a project object
                        String preparedBy = reportMap.get("preparedBy");
                        String project = reportMap.get("project");
                        String punchListType = reportMap.get("punchListType");
                        String siteVisitDate = reportMap.get("siteVisitDate");
                        Report saveReport = new Report(preparedBy,project,punchListType,siteVisitDate);
                        //utilizing the project objects and key data
                        reportList.add(saveReport);
                        Log.d("MAIN", key);
                        Log.d("MAIN","report added with details "+preparedBy + " " + project+ " " + punchListType + " "+siteVisitDate);
                    }
                }
            }
            public void onCancelled (DatabaseError firebaseError){
                //doing nothing for now // TODO: 3/27/2017 Add Error Handling
            }
        });

        //Setting up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Reports");
        // Get project data passed from previous activity
        projectKey = this.getIntent().getExtras().getString("key");
        myProject = this.getIntent().getExtras().getParcelable("project_parcel");
        //Setting up FirebaseUI ListView Adapter and ListView
        mListView = (ListView) findViewById(R.id.reportList);
        mAdapter = new FirebaseListAdapter<Report>(this, Report.class, R.layout.itemlistrow, reportRef) {
            @Override
            protected void populateView(View view, Report report, int position) {
                ((TextView)view.findViewById(R.id.report)).setText(report.getPunchListType()+ " - "+report.getSiteVisitDate());

            }
        };
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Report selectedReport = reportList.get(position);
                Bundle args = new Bundle();
                args.putParcelable("report_parcel", selectedReport);
                args.putLong("position",position);
                Intent detailIntent = new Intent(context, IssueView.class);
                detailIntent.putExtras(args);
                startActivity(detailIntent);
            }

        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Report selectedReport = reportList.get(position);
                Bundle args = new Bundle();
                args.putParcelable("report_parcel",selectedReport);
                args.putInt("position",position);
                AlertEditFragment editFragment = new AlertEditFragment();
                editFragment.setArguments(args);
                editFragment.show(fm, "Android Dialog");
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_report) {
            Bundle args = new Bundle();
            args.putString("project_name",projectKey);
            reportFragment = new ReportFragment();
            reportFragment.setArguments(args);
            // Show Alert DialogFragment
            reportFragment.show(fm, "Android Dialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onComplete(Report newReport) {
        reportList.add(newReport);
        int position = reportList.indexOf(newReport);
        mAdapter.notifyDataSetChanged();
        String reportAutoID = reportRef.push().getKey();
        reportRef.child(reportAutoID).setValue(newReport);
        Query reportKey = reportRef.orderByChild("project").equalTo(projectKey);
        final List<String> reportsArray = new ArrayList<>();
        reportKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        Log.d("REPORT", snap.getKey());
                        String key = snap.getKey();
                        if (key != null) {
                            reportsArray.add(key);
                        }
                    }
                }
                Log.d("REPORT", reportsArray.toString());

                projectRef.child(projectKey).setValue(reportsArray);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Bundle args = new Bundle();
        args.putParcelable("report_parcel",newReport);
        args.putInt("position",position);
        args.putString("key",reportAutoID);
        Intent detailIntent = new Intent(context, IssueView.class);
        detailIntent.putExtras(args);
        startActivity(detailIntent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
