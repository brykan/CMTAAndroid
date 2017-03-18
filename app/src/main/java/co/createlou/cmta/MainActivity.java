package co.createlou.cmta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AlertDFragment.OnCompleteListener{

    FragmentManager fm = getSupportFragmentManager();
    public ListView mListView;
    public ArrayList<Project> projectList = new ArrayList<Project>();
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Projects");
        mListView = (ListView) findViewById(R.id.projectList);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Project selectedProject = projectList.get(position);

                Intent detailIntent = new Intent(context, ProjectView.class);
                detailIntent.putExtra("title", selectedProject.projectName);
                startActivity(detailIntent);
            }

        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Project selectedProject = projectList.get(position);

                Intent detailIntent = new Intent(context, ProjectView.class);
                detailIntent.putExtra("title", selectedProject.projectName);
                startActivity(detailIntent);
                return false;
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
            AlertDFragment alertdFragment = new AlertDFragment();
            // Show Alert DialogFragment
            alertdFragment.show(fm,"Android Dialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onComplete(Project newProject) {

        projectList.add(newProject);
        myProjectAdapter customAdapter = new myProjectAdapter(context, projectList);
        mListView.setAdapter(customAdapter);

        Intent detailIntent = new Intent(context, ProjectView.class);
        detailIntent.putExtra("title", newProject.projectName);
        startActivity(detailIntent);
    }


}
