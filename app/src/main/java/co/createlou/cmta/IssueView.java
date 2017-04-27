package co.createlou.cmta;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
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
import com.getbase.floatingactionbutton.FloatingActionButton;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Bryan on 3/6/17.
 */

public class IssueView extends AppCompatActivity implements IssueFragment.OnCompleteListener,EditIssueFragment.OnCompleteListener,ReportRequest.OnCompleteListener, View.OnClickListener{

    private static final String TAG = "IssueView";

    final Context context = this;

    //Fragment items
    FragmentManager fm = getSupportFragmentManager();
    public IssueFragment issueFragment;

    //Various List items
    public ListView mListView;
    public Report myReport;
    FirebaseListAdapter mAdapter;
    private FloatingActionButton fab;
    //Database References

    //Various Keys
    public String reportAutoID;

    public byte[] imageData;
    public BitmapDrawable bdrawable;
    static final String appDirectoryName = "Issue_Images";
    static final String imageRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/"+appDirectoryName;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_view);
        //pulling issue data if need be
        reportAutoID = this.getIntent().getExtras().getString("key");
        Query issueQuery = FirebaseDatabase.getInstance().getReference().child("issues").orderByChild("report").equalTo(reportAutoID);

        //Setting up the Toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Issues");
        // Get report data passed from previous activity
       // position = this.getIntent().getExtras().getInt("position");
        myReport = this.getIntent().getExtras().getParcelable("report_parcel");
        //Setting up listView and firebaseadapter
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        mAdapter = new FirebaseListAdapter<Issue>(this, Issue.class, R.layout.issuelistrow, issueQuery) {
            @Override
            protected void populateView(View view, Issue issue, int position) {
                ((TextView)view.findViewById(R.id.issueLocation)).setText(issue.getLocation());
                ((TextView)view.findViewById(R.id.issueDetails)).setText(issue.getDetails());
                ((TextView)view.findViewById(R.id.issueStatus)).setText(issue.getStatus());
                 Glide.with(getBaseContext()).load(getImage(mAdapter.getRef(position).getKey())).fitCenter().into((ImageView)view.findViewById(R.id.issueImage));
                }
            public byte[] getImage(String key){
                File file = new File(imageRoot, key + ".png");
                int size = (int) file.length();
                byte[] bytes = new byte[size];
                try{
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                    buf.read(bytes,0,bytes.length);
                    buf.close();
                }catch(FileNotFoundException e){
                    e.printStackTrace();
                }catch(IOException e){
                  e.printStackTrace();
                }
                return bytes;
            }

        };
        mListView = (ListView) findViewById(R.id.issueList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final DatabaseReference mRef = mAdapter.getRef(position);
                final HashMap<String,String> issueMap = new HashMap<String, String>();
                final int selectedPosition = position;
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                //Pulling the project keys and creating a hashmap of the project data

                                String key =(String)postSnapshot.getKey();
                                if(!postSnapshot.getKey().equals("image")) {
                                    String value = (String) postSnapshot.getValue();
                                    issueMap.put(key, value);
                                    Log.d("Report", key);
                                    Log.d("Report", value);
                                }
                            }
                        }
                        String image = issueMap.get("image");
                        String location = issueMap.get("location");
                        String details = issueMap.get("details");
                        String status = issueMap.get("status");
                        String report = issueMap.get("report");
                        Issue selectedIssue = new Issue(location,status,details,report);
                        Bundle args = new Bundle();
                        args.putParcelable("issue_parcel",selectedIssue);
                        args.putInt("position",selectedPosition);
                        args.putByteArray("image",image.getBytes());
                        args.putString("key",mRef.getKey());
                        EditIssueFragment editFragment = new EditIssueFragment();
                        editFragment.setArguments(args);
                        editFragment.show(fm, "Android Dialog");
                    }
                    public void onCancelled (DatabaseError firebaseError){
                        //doing nothing for now // TODO: 3/27/2017 Add Error Handling
                    }

                });


                return true;
            }
        });

    }

    public void addIssue(){
        Bundle args = new Bundle();
        args.putString("report_name", reportAutoID);
        issueFragment = new IssueFragment();
        issueFragment.setArguments(args);

        // Show Alert DialogFragment
        issueFragment.show(fm, "Android Dialog");
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
        if (id == R.id.export_report) {
            Bundle args = new Bundle();
            args.putString("key", reportAutoID);
            ReportRequest reportRequest = new ReportRequest();
            reportRequest.setArguments(args);

            // Show Alert DialogFragment
            reportRequest.show(fm, "Android Dialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onComplete(Issue newIssue) {
        DatabaseReference issueRef = FirebaseDatabase.getInstance().getReference().child("issues");
        String issueAutoID = issueRef.push().getKey();
        issueRef.child(issueAutoID).child("details").setValue(newIssue.getDetails());
        issueRef.child(issueAutoID).child("location").setValue(newIssue.getLocation());
        issueRef.child(issueAutoID).child("status").setValue(newIssue.getStatus());
        issueRef.child(issueAutoID).child("report").setValue(reportAutoID);
        issueRef.child(issueAutoID).child("image").setValue(Base64.encodeToString(newIssue.getData(),Base64.NO_WRAP));
        final Bitmap newImage = newIssue.getBmap();
        write(newImage,issueAutoID,newIssue);
        mAdapter.notifyDataSetChanged();
    }
    public void onInComplete(Issue newIssue){
        DatabaseReference issueRef = FirebaseDatabase.getInstance().getReference().child("issues");
        String issueAutoID = issueRef.push().getKey();
        issueRef.child(issueAutoID).child("details").setValue(newIssue.getDetails());
        issueRef.child(issueAutoID).child("location").setValue(newIssue.getLocation());
        issueRef.child(issueAutoID).child("status").setValue(newIssue.getStatus());
        issueRef.child(issueAutoID).child("report").setValue(reportAutoID);
        mAdapter.notifyDataSetChanged();
    }
    public void onEdit(Issue editIssue, int position){
        DatabaseReference itemRef = mAdapter.getRef(position);
        itemRef.child("status").setValue(editIssue.getStatus());
        itemRef.child("location").setValue(editIssue.getLocation());
        itemRef.child("details").setValue(editIssue.getDetails());
        itemRef.child("image").setValue(editIssue.getData());
        write(editIssue.getBmap(),itemRef.getKey(),editIssue);
        mAdapter.notifyDataSetChanged();
    }
    public void onIncompleteEdit(Issue editIssue, int position){
        DatabaseReference itemRef = mAdapter.getRef(position);
        itemRef.child("status").setValue(editIssue.getStatus());
        itemRef.child("location").setValue(editIssue.getLocation());
        itemRef.child("details").setValue(editIssue.getDetails());
        mAdapter.notifyDataSetChanged();
    }
    public void onDelete(int position){
        final String TAG = "DELETION";
//        issueKeys.remove(position);
        //issueList.remove(position);
        DatabaseReference itemRef = mAdapter.getRef(position);
        Log.d(TAG, "onDelete: "+position);
        String deleteKey = itemRef.getKey();
                        String filename = deleteKey + ".png";
                        File dir = new File(imageRoot);
                        dir.mkdirs();
                        File dest = new File(dir, filename);
                        dest.delete();
                        DeleteAndScanFile(context, filename, dest);

                        Log.d("DELETION", deleteKey);
        itemRef.removeValue();
        mAdapter.notifyDataSetChanged();
        /*Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);*/

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void write(Bitmap bmp, String keyname,Issue issue){
        FileOutputStream out = null;
        String filename = keyname + ".png";
        File dir = new File(imageRoot);
        dir.mkdirs();
        File dest = new File(dir,filename);

        try {
            out = new FileOutputStream(dest);
            bmp.compress(Bitmap.CompressFormat.JPEG, 25, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("fuckedup", "write: fuck");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MediaScannerConnection.scanFile(this, new String[] { dest.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });


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
    byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis= new FileInputStream(f);;
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        }  catch (IOException e){
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }
    public byte[] getImage(String key){
        File file = new File(imageRoot, key + ".png");
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try{
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes,0,bytes.length);
            buf.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return bytes;
    }
    @Override
    public void onComplete(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(TAG, "onResponse: "+response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.fab)
        {
                addIssue();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }
}