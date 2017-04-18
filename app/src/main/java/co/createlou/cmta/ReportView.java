package co.createlou.cmta;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bryan on 3/6/17.
 */

public class ReportView extends AppCompatActivity implements ReportPager.OnCompleteListener, EditReportPager.OnCompleteListener,ReportFragment.OnReportDataPass,NotesFragment.OnNotesDataPass,EditReportFragment.OnReportEditDataPass,EditNotesFragment.OnNotesEditDataPass {

    FragmentManager fm = getSupportFragmentManager();
    public ListView mListView;
    public Project myProject;
    public ArrayList<Report> reportList = new ArrayList<>();
    public ReportPager reportPagerFragment;
    final Context context = this;
    private String projectKey;
    FirebaseListAdapter mAdapter;
    List<String> notes;
    public ArrayList<String> reportKeys = new ArrayList<>();


    DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference().child("projects");
    DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference().child("reports");
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //File deletion stuff
    static final String appDirectoryName = "Issue_Images";
    static final String imageRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/"+appDirectoryName;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view);
        projectKey = this.getIntent().getExtras().getString("key");
        Log.d("projectkey",projectKey);
        projectRef.child(projectKey).child("reports").orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        //Pulling the project keys and creating a hashmap of the project data
                        String key = (String) snap.getValue();
                        //converting the hashmap data into a project object
                        //utilizing the project objects and key data
                        if(reportKeys.contains(key)){
                            Log.d("REPORTS", "key already added");
                        }else{
                            reportKeys.add(key);
                            Log.d("REPORTS", key);

                        }
                    }
                }
            }
            public void onCancelled (DatabaseError firebaseError){
                //doing nothing for now // TODO: 3/27/2017 Add Error Handling
            }
        });
        Query reportQuery = reportRef.orderByChild("project").equalTo(projectKey);
        reportQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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
                        Report saveReport = new Report(preparedBy,project,punchListType,siteVisitDate,notes);
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
        myProject = this.getIntent().getExtras().getParcelable("project_parcel");
        Query childRef = reportRef.orderByChild("project").equalTo(projectKey);
        //Setting up FirebaseUI ListView Adapter and ListView
        mListView = (ListView) findViewById(R.id.reportList);
        mAdapter = new FirebaseListAdapter<Report>(this, Report.class, R.layout.reportlistrow, childRef) {
            @Override
            protected void populateView(View view, Report report, int position) {
                ((TextView)view.findViewById(R.id.report)).setText(report.getPunchListType()+ " - "+report.getSiteVisitDate());

            }
        };
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    DatabaseReference mRef = mAdapter.getRef(position);
                    final String majorKey = mRef.getKey();
                    final int testPosition = position;
                    final HashMap<String,String> reportMap = new HashMap<String, String>();
                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    //Pulling the project keys and creating a hashmap of the project data
                                    String key =(String)postSnapshot.getKey();
                                    if (postSnapshot.getKey().equals("notes")) {

                                    }else{
                                        String value = (String) postSnapshot.getValue();
                                        reportMap.put(postSnapshot.getKey(), (String) postSnapshot.getValue());
                                        Log.d("Report", key);
                                        Log.d("Report", value);}

                                    //converting the hashmap data into a project object

                                    //utilizing the project objects and key data


                                }
                            }
                            String project = reportMap.get("project");
                            Log.d("bullshit", "onDataChange: "+project);
                            String prep = reportMap.get("preparedBy");
                            Log.d("bullshit", "onDataChange: "+prep);

                            String punchType = reportMap.get("punchListType");
                            Log.d("bullshit", "onDataChange: "+punchType);

                            String visitDate = reportMap.get("siteVisitDate");
                            Log.d("bullshit", "onDataChange: "+visitDate);

                            Report testReport = new Report(prep,project,punchType,visitDate);

                            Bundle args = new Bundle();
                            args.putParcelable("report_parcel",testReport);
                            args.putString("key",majorKey);
                            args.putInt("position",testPosition);
                            Intent detailIntent = new Intent(context, IssueView.class);
                            detailIntent.putExtras(args);
                            startActivity(detailIntent);

                        }
                        public void onCancelled (DatabaseError firebaseError){
                            //doing nothing for now // TODO: 3/27/2017 Add Error Handling
                        }

                    });

                }

        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                DatabaseReference mRef = mAdapter.getRef(position);
                final String majorKey = mRef.getKey();
                final int testPosition = position;
                final HashMap<String,String> reportMap = new HashMap<String, String>();
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                //Pulling the project keys and creating a hashmap of the project data
                                String key =(String)postSnapshot.getKey();
                                if (postSnapshot.getKey().equals("notes")) {

                                }else{
                                    String value = (String) postSnapshot.getValue();
                                    reportMap.put(postSnapshot.getKey(), (String) postSnapshot.getValue());
                                    Log.d("Report", key);
                                    Log.d("Report", value);}

                                //converting the hashmap data into a project object

                                //utilizing the project objects and key data


                            }
                        }
                        String project = reportMap.get("project");
                        Log.d("bullshit", "onDataChange: "+project);
                        String prep = reportMap.get("preparedBy");
                        Log.d("bullshit", "onDataChange: "+prep);

                        String punchType = reportMap.get("punchListType");
                        Log.d("bullshit", "onDataChange: "+punchType);

                        String visitDate = reportMap.get("siteVisitDate");
                        Log.d("bullshit", "onDataChange: "+visitDate);

                        Report testReport = new Report(prep,project,punchType,visitDate);

                        Bundle args = new Bundle();
                        args.putParcelable("report_parcel",testReport);
                        args.putString("key",majorKey);
                        args.putInt("position",testPosition);
                        EditReportPager editFragment = new EditReportPager();
                        editFragment.setArguments(args);
                        editFragment.show(fm, "Android Dialog");
                        Log.d("MAIN", "onDataChange: launched dialog now");
                    }
                    public void onCancelled (DatabaseError firebaseError){
                        //doing nothing for now // TODO: 3/27/2017 Add Error Handling
                    }

                });

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
            reportPagerFragment = new ReportPager();
            reportPagerFragment.setArguments(args);
            // Show Alert DialogFragment
            reportPagerFragment.show(fm, "Android Dialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onComplete(Report newReport) {
        String reportAutoID = reportRef.push().getKey();
        reportList.add(newReport);
        int position = reportList.indexOf(newReport);
        mAdapter.notifyDataSetChanged();
        reportRef.child(reportAutoID).child("preparedBy").setValue(newReport.getPreparedBy());
        reportRef.child(reportAutoID).child("project").setValue(newReport.getProject());
        reportRef.child(reportAutoID).child("punchListType").setValue(newReport.getPunchListType());
        reportRef.child(reportAutoID).child("siteVisitDate").setValue(newReport.getSiteVisitDate());
        reportRef.child(reportAutoID).child("notes").setValue(newReport.getNotes());
        Bundle args = new Bundle();
        args.putString("key",reportAutoID);
        args.putParcelable("report_parcel",newReport);
        args.putInt("position",position);
        Intent detailIntent = new Intent(context, IssueView.class);
        detailIntent.putExtras(args);
        startActivity(detailIntent);
    }
    public void onEdit(Report editReport, int position){
        DatabaseReference itemRef = mAdapter.getRef(position);
        itemRef.child("preparedBy").setValue(editReport.getPreparedBy());
        itemRef.child("punchListType").setValue(editReport.getPunchListType());
        itemRef.child("siteVisitDate").setValue(editReport.getSiteVisitDate());
        itemRef.child("notes").setValue(editReport.getNotes());
        mAdapter.notifyDataSetChanged();
        reportList.set(position,editReport);
    }
    public void onDelete(int position){

        final String TAG = "DELETION";
        DatabaseReference itemRef = mAdapter.getRef(position);
        String deleteKey = itemRef.getKey();
        Log.d("DELETION", deleteKey);
        Query deleteIssueRef = FirebaseDatabase.getInstance().getReference().child("issues").orderByChild("report").equalTo(deleteKey);
        deleteIssueRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        //Pulling the project keys and creating a hashmap of the project data
                        String key = postSnapshot.getKey();
                        postSnapshot.getRef().removeValue();
                        StorageReference deleteRef = storage.getReference().child("images/" + key);
                        deleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // File deleted successfully
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                            }

                        });
                        String filename = key + ".png";
                        File dir = new File(imageRoot);
                        dir.mkdirs();
                        File dest = new File(dir, filename);
                        dest.delete();
                        DeleteAndScanFile(context, filename, dest);

                        Log.d("DELETION", key);
                    }
                }
            }

            public void onCancelled(DatabaseError firebaseError) {
                //doing nothing for now // TODO: 3/27/2017 Add Error Handling
            }
        });
        itemRef.removeValue();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void DeleteAndScanFile(final Context context, String path,
                                   final File fi) {
        String fpath = path.substring(path.lastIndexOf("/") + 1);
        Log.i("fpath", fpath);
        try {
            MediaScannerConnection.scanFile(context, new String[]{Environment
                            .getExternalStorageDirectory().toString()
                            + "/Issue_Images/"
                            + fpath.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            if (uri != null) {
                                context.getContentResolver().delete(uri, null,
                                        null);
                            }
                            fi.delete();
                            System.out.println("file Deleted :" + fi.getPath());
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotesDataPass(List<String> data) {

    }

    @Override
    public void onReportDataPass(ArrayList<String> data) {

    }

    @Override
    public void onNotesEditDataPass(List<String> data) {

    }

    @Override
    public void onReportEditDataPass(ArrayList<String> data) {

    }
}
