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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

/**
 * Created by Bryan on 3/6/17.
 */

public class ProjectView extends AppCompatActivity implements IssueFragment.OnCompleteListener {

    private static final String TAG = "SaveDetails";

    FragmentManager fm = getSupportFragmentManager();
    public ListView mListView;
    public myIssueAdapter customAdapter;
    public ArrayList<Issue> issueList;
    public Project project;
    public ArrayList<Project> projectList = new ArrayList<>();
    public IssueFragment issueFragment;
    ApplicationData applicationData = ((ApplicationData)getApplication());
    private int position;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Get recipe data passed from previous activity
        position = this.getIntent().getExtras().getInt("position");
        project = this.getIntent().getExtras().getParcelable("project_parcel");
        projectList = applicationData.getProjectList();
        // Set title on action bar of this activity
        setTitle(String.valueOf(project.getAmountofissues()));
        mListView = (ListView) findViewById(R.id.issueList);
        issueList = project.issues;
        customAdapter = new myIssueAdapter(this, 1, issueList);
        mListView.setAdapter(customAdapter);
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
        if (id == R.id.add_issue) {
            issueFragment = new IssueFragment();
            // Show Alert DialogFragment
            issueFragment.show(fm, "Android Dialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onComplete(Issue newIssue) {
        project.addIssue(newIssue);
        projectList.set(position, project);
        applicationData.setProjectList(projectList);
        customAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}