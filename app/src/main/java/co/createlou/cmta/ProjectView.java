package co.createlou.cmta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Bryan on 3/6/17.
 */

public class ProjectView extends AppCompatActivity implements IssueFragment.OnCompleteListener{

    FragmentManager fm = getSupportFragmentManager();
    public ListView mListView;
    public ArrayList<Issue> issueList = new ArrayList<Issue>();
    final Context context = this;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Get recipe data passed from previous activity
        String title = this.getIntent().getExtras().getString("title");

        // Set title on action bar of this activity
        setTitle(title);
        mListView = (ListView) findViewById(R.id.issueList);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Issue selectedIssue = issueList.get(position);

                Intent detailIntent = new Intent(context, IssueFragment.class);
                detailIntent.putExtra("title", selectedIssue.issueDetails);
                startActivity(detailIntent);
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
            AlertDFragment alertdFragment = new AlertDFragment();
            // Show Alert DialogFragment
            alertdFragment.show(fm,"Android Dialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void onComplete(Issue newIssue) {
        issueList.add(newIssue);
        myIssueAdapter customAdapter = new myIssueAdapter(context, issueList);
        mListView.setAdapter(customAdapter);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
