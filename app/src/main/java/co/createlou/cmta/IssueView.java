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
import android.support.design.widget.FloatingActionButton;
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


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bryan on 3/6/17.
 */

public class IssueView extends AppCompatActivity implements IssueFragment.OnCompleteListener,EditIssueFragment.OnCompleteListener,ReportRequest.OnCompleteListener {

    private static final String TAG = "IssueView";

    final Context context = this;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;
    //Fragment items
    FragmentManager fm = getSupportFragmentManager();
    public IssueFragment issueFragment;

    //Various List items
    public ListView mListView;
    public ArrayList<Issue> issueList = new ArrayList<>();
    public Report myReport;
    public int position;
    FirebaseListAdapter mAdapter;
    private FloatingActionButton fab;

    //Database References
    DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference().child("reports");
    DatabaseReference issueRef = FirebaseDatabase.getInstance().getReference().child("issues");

    //Various Keys
    public String reportAutoID;
    ArrayList<String> imageKeys = new ArrayList<>();
    ArrayList<String> issueKeys = new ArrayList<>();

    //Image Data
    final long THREE_MEGABYTES = 1024*1024*3;

    public byte[] imageData;
    public BitmapDrawable bdrawable;
    static final String appDirectoryName = "Issue_Images";
    static final String imageRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/"+appDirectoryName;
    public void encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,25,baos);
        imageData = baos.toByteArray();
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_view);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://cmta-8ecda.appspot.com/");
        //pulling issue data if need be
        reportAutoID = this.getIntent().getExtras().getString("key");
        Query issueQuery = issueRef.orderByChild("report").equalTo(reportAutoID);
        issueQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        //Pulling the project keys and creating a hashmap of the project data
                        String key = snap.getKey();
                        issueKeys.add(key);
                        HashMap<String, String> issueMap = (HashMap<String, String>) snap.getValue();
                        //converting the hashmap data into a project object
                        final String issueLocation = issueMap.get("location");
                        final String issueStatus = issueMap.get("status");
                        final String issueDetails = issueMap.get("details");
                        final String saveKey = issueMap.get("report");
                        imageData = getImage(key);
                        File imgFile = new File(imageRoot, key + ".png");
                        Log.d(TAG, "onDataChange: "+imageData.length);
                        if (imgFile.exists()) {
//                            BitmapFactory.Options options = new BitmapFactory.Options();
//                            options.inSampleSize = 4;
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                            encodeBitmap(myBitmap);
//                            final int THUMBSIZE = 256;
//                            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(myBitmap,THUMBSIZE,THUMBSIZE);
                            bdrawable = new BitmapDrawable(getResources(), myBitmap);
                            Issue saveIssue = new Issue(issueLocation, issueStatus, issueDetails, saveKey, imageData,imageData.toString(), bdrawable, myBitmap);
                            Log.d(TAG, "onDataChange: "+saveIssue.toString());
                            //utilizing the project objects and key data
                            issueList.add(saveIssue);
                        }
                        imageKeys.add(key);
                        Log.d("MAIN", key);
                        Log.d("MAIN", "issue added with location " + issueLocation + " status " + issueStatus + " details " + issueDetails);
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
        //Setting up listView and firebaseadapter
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openRequest();

            }
        });
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
                Issue selectedIssue = issueList.get(position);
                Bundle args = new Bundle();
                args.putParcelable("issue_parcel",selectedIssue);
                args.putInt("position",position);
                args.putByteArray("image",selectedIssue.getData());
                args.putString("key",mAdapter.getRef(position).getKey());
                EditIssueFragment editFragment = new EditIssueFragment();
                editFragment.setArguments(args);
                editFragment.show(fm, "Android Dialog");
                return true;
            }
        });

    }

    public void openRequest(){
        Bundle args = new Bundle();
        args.putString("key", reportAutoID);
        ReportRequest request = new ReportRequest();
        request.setArguments(args);

        // Show Alert DialogFragment
        request.show(fm, "Android Dialog");
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
            Bundle args = new Bundle();
            args.putString("report_name", reportAutoID);
            issueFragment = new IssueFragment();
            issueFragment.setArguments(args);

            // Show Alert DialogFragment
            issueFragment.show(fm, "Android Dialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onComplete(Issue newIssue) {
        issueList.add(newIssue);
        String issueAutoID = issueRef.push().getKey();
        issueKeys.add(issueAutoID);
        issueRef.child(issueAutoID).child("details").setValue(newIssue.getDetails());
        issueRef.child(issueAutoID).child("location").setValue(newIssue.getLocation());
        issueRef.child(issueAutoID).child("status").setValue(newIssue.getStatus());
        issueRef.child(issueAutoID).child("report").setValue(reportAutoID);
        issueRef.child(issueAutoID).child("image").setValue(Base64.encodeToString(newIssue.getData(),Base64.NO_WRAP));
        final Bitmap newImage = newIssue.getBmap();
        write(newImage,issueAutoID,newIssue);
        mAdapter.notifyDataSetChanged();
    }
    public void onEdit(Issue editIssue, int position){
        DatabaseReference itemRef = mAdapter.getRef(position);
        itemRef.child("status").setValue(editIssue.getStatus());
        itemRef.child("location").setValue(editIssue.getLocation());
        itemRef.child("details").setValue(editIssue.getDetails());
        write(editIssue.getBmap(),itemRef.getKey(),editIssue);
        mAdapter.notifyDataSetChanged();
        issueList.set(position,editIssue);
    }
    public void onDelete(int position){
        final String TAG = "DELETION";
        issueKeys.remove(position);
        issueList.remove(position);
        DatabaseReference itemRef = mAdapter.getRef(position);
        Log.d(TAG, "onDelete: "+position);
        String deleteKey = itemRef.getKey();
        StorageReference deleteRef = storage.getReference().child("images/" + deleteKey);
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
}