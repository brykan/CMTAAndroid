package co.createlou.cmta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements ProjectFragment.OnCompleteListener, AlertEditFragment.OnCompleteListener{
    //Instantiating Firebase Database Instance
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    //Instantiating FragmentManager
    FragmentManager fm = getSupportFragmentManager();
    //Declaring Extras
    public ListView mListView;
    public ArrayList<Project> projectList = new ArrayList<>();
    final Context context = this;
    FirebaseListAdapter mAdapter;
    public ArrayList<String> keys = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Setting Firebase Child Reference
        DatabaseReference childref = ref.child("projects");

        childref.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
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
                        Project project = new Project(name,num,loc);
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Projects");
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
                Project selectedProject = projectList.get(position);
                Bundle args = new Bundle();
                args.putParcelable("project_parcel", selectedProject);
                args.putString("key",keys.get(position));
                Intent detailIntent = new Intent(context, ReportView.class);
                detailIntent.putExtras(args);
                startActivity(detailIntent);
            }

        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Project selectedProject = projectList.get(position);
                Bundle args = new Bundle();
                args.putParcelable("project_parcel",selectedProject);
                args.putString("key",keys.get(position));
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
            ProjectFragment alertdFragment = new ProjectFragment();
            // Show Alert DialogFragment
            alertdFragment.show(fm,"Android Dialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onComplete(Project newProject) {
        String projectKey = ref.child("projets").push().getKey();
        Log.d("MAIN", "Storing Project");
        ref.child("projects").child(projectKey).child("projectName").setValue(newProject.getProjectName());
        ref.child("projects").child(projectKey).child("projectNumber").setValue(newProject.getProjectNumber());
        ref.child("projects").child(projectKey).child("projectLocation").setValue(newProject.getProjectLocation());
        projectList.add(newProject);
        int position = projectList.indexOf(newProject);
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
        mAdapter.notifyDataSetChanged();
        projectList.set(position,project);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }
}
