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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.ArrayList;

import java.util.HashMap;


public class ProjectView extends AppCompatActivity implements ProjectFragment.OnCompleteListener, EditProjectFragment.OnCompleteListener{
    //Instantiating Firebase Database Instance

    final Context context = this;
    public DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference().child("projects");
    //Declaring Extras
    public ListView mListView;
    public ArrayList<Project> projectList = new ArrayList<>();
    public ArrayList<String> keys = new ArrayList<>();
    public String android_id;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //File deletion stuff
    static final String appDirectoryName = "Issue_Images";
    static final String imageRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/"+appDirectoryName;



    //Instantiating FragmentManager
    FragmentManager fm = getSupportFragmentManager();
    FirebaseListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android_id = Installation.id(this);
        Log.d("androidID", android_id);
        setContentView(R.layout.activity_project_view);
        //Setting Firebase Child Reference
        Query childref = projectRef.orderByChild("deviceID").equalTo(android_id);

        childref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        //Pulling the project keys and creating a hashmap of the project data
                        String key = postSnapshot.getKey();
                        HashMap<String,String> projectMap = (HashMap<String,String>) postSnapshot.getValue();
                        //converting the hashmap data into a project object
                        String name = projectMap.get("projectName");
                        String num = projectMap.get("projectNumber");
                        String loc = projectMap.get("projectLocation");
                        Project project = new Project(name,num,loc,android_id);
                        //utilizing the project objects and key data
                        projectList.add(project);
                        keys.add(key);
                        Log.d("MAIN", key);
                        Log.d("MAIN","project added with details "+name + " " + num+ " " + loc);
                    }
                }
            }
                public void onCancelled (DatabaseError firebaseError){
                //doing nothing for now // TODO: 3/27/2017 Add Error Handling
                }
            });
        //Setting up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Projects");
        //Setting up ListView and ListViewAdapter
        mListView = (ListView) findViewById(R.id.projectList);
        mAdapter = new FirebaseListAdapter<Project>(this, Project.class, R.layout.itemlistrow, childref) {
            @Override
            protected void populateView(View view, Project project, int position) {
                ((TextView)view.findViewById(R.id.projectName)).setText(project.getProjectName());

            }
        };
        mListView.setAdapter(mAdapter);
        //Setting up Click and LongClick Listeners
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DatabaseReference mRef = mAdapter.getRef(position);
                final String majorKey = mRef.getKey();
                final int testPosition = position;
                final HashMap<String,String> projectMap = new HashMap<String, String>();

                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                //Pulling the project keys and creating a hashmap of the project data
                                String key =(String)postSnapshot.getKey();
                                String value = (String)postSnapshot.getValue();
                                //converting the hashmap data into a project object
                                projectMap.put(postSnapshot.getKey(),(String)postSnapshot.getValue());
                                //converting the hashmap data into a project object

                                //utilizing the project objects and key data
                                Log.d("MAIN", key);
                                Log.d("MAIN", value);

                            }
                        }
                        String name = projectMap.get("projectName");
                        String num = projectMap.get("projectNumber");
                        String loc = projectMap.get("projectLocation");
                        final Project testProject = new Project(name,num,loc,android_id);

                        Bundle args = new Bundle();
                        args.putParcelable("project_parcel",testProject);
                        args.putString("key",majorKey);
                        args.putInt("position",testPosition);
                        Intent detailIntent = new Intent(context, ReportView.class);
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
                final HashMap<String,String> projectMap = new HashMap<String, String>();

                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                //Pulling the project keys and creating a hashmap of the project data
                                String key =(String)postSnapshot.getKey();
                                String value = (String)postSnapshot.getValue();
                                //converting the hashmap data into a project object
                                projectMap.put(postSnapshot.getKey(),(String)postSnapshot.getValue());
                                //converting the hashmap data into a project object

                                //utilizing the project objects and key data
                                Log.d("MAIN", key);
                                Log.d("MAIN", value);

                            }
                        }
                        String name = projectMap.get("projectName");
                        String num = projectMap.get("projectNumber");
                        String loc = projectMap.get("projectLocation");
                        final Project testProject = new Project(name,num,loc,android_id);

                        Bundle args = new Bundle();
                        args.putParcelable("project_parcel",testProject);
                        args.putString("key",majorKey);
                        args.putInt("position",testPosition);
                        EditProjectFragment editFragment = new EditProjectFragment();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_project) {
            Bundle args = new Bundle();
            args.putString("deviceID", android_id);
            ProjectFragment alertdFragment = new ProjectFragment();
            // Show Alert DialogFragment
            alertdFragment.setArguments(args);
            alertdFragment.show(fm,"Android Dialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onComplete(Project newProject) {
        String projectKey = projectRef.child("projects").push().getKey();
        Log.d("MAIN", "Storing Project");
        keys.add(projectKey);
        projectRef.child(projectKey).child("projectName").setValue(newProject.getProjectName());
        projectRef.child(projectKey).child("projectNumber").setValue(newProject.getProjectNumber());
        projectRef.child(projectKey).child("projectLocation").setValue(newProject.getProjectLocation());
        projectRef.child(projectKey).child("deviceID").setValue(newProject.getUserKey());
        projectList.add(newProject);
        mAdapter.notifyDataSetChanged();
       // write();
        Bundle args = new Bundle();
        args.putParcelable("project_parcel",newProject);
        args.putString("key",projectKey);
        Intent detailIntent = new Intent(context, ReportView.class);
        detailIntent.putExtras(args);
        startActivity(detailIntent);
    }
    public void onEdit(Project project,int position) {
        DatabaseReference itemRef = mAdapter.getRef(position);
        itemRef.child("projectLocation").setValue(project.getProjectLocation());
        itemRef.child("projectName").setValue(project.getProjectName());
        itemRef.child("projectNumber").setValue(project.getProjectNumber());
        mAdapter.notifyDataSetChanged();

        projectList.set(position,project);
    }
    public void onDelete(int position){
        final String TAG = "DELETION";
        DatabaseReference itemRef = mAdapter.getRef(position);
        String deleteKey = itemRef.getKey();
        Query deleteReportRef = FirebaseDatabase.getInstance().getReference().child("reports").orderByChild("project").equalTo(deleteKey);
        deleteReportRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        //Pulling the project keys and creating a hashmap of the project data
                        String key = postSnapshot.getKey();
                        postSnapshot.getRef().removeValue();
                        Log.d("DELETION", key);
                        Query deleteIssueRef = FirebaseDatabase.getInstance().getReference().child("issues").orderByChild("report").equalTo(key);
                        deleteIssueRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                //Pulling the project keys and creating a hashmap of the project data
                                                String key = postSnapshot.getKey();
                                                postSnapshot.getRef().removeValue();
                                                StorageReference deleteRef = storage.getReference().child("images/"+key);
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
                                        File dest = new File(dir,filename);
                                        dest.delete();
                                        DeleteAndScanFile(context,filename,dest);

                                                Log.d("DELETION", key);
                                            }
                                        }
                                    }

                                    public void onCancelled(DatabaseError firebaseError) {
                                        //doing nothing for now // TODO: 3/27/2017 Add Error Handling
                                    }
                                });
                            }
                        }
                    }
            public void onCancelled (DatabaseError firebaseError){
                //doing nothing for now // TODO: 3/27/2017 Add Error Handling
            }
        });

        itemRef.removeValue();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
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

}
