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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bryan on 3/6/17.
 */

public class IssueView extends AppCompatActivity implements IssueFragment.OnCompleteListener,EditIssueFragment.OnCompleteListener {

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
                        File imgFile = new File(imageRoot, key + ".png");

                        if (imgFile.exists()) {
                            try {
                                imageData = fullyReadFileToBytes(imgFile);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                            bdrawable = new BitmapDrawable(getResources(), myBitmap);
                            Issue saveIssue = new Issue(issueLocation, issueStatus, issueDetails, saveKey, imageData, bdrawable, myBitmap);
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
        mAdapter = new FirebaseListAdapter<Issue>(this, Issue.class, R.layout.issuelistrow, issueQuery) {
            @Override
            protected void populateView(View view, Issue issue, int position) {
                ((TextView)view.findViewById(R.id.issueLocation)).setText(issueList.get(position).getIssueLocation());
                ((TextView)view.findViewById(R.id.issueDetails)).setText(issueList.get(position).getIssueDetails());
                ((TextView)view.findViewById(R.id.issueStatus)).setText(issueList.get(position).getIssueStatus());
                ((ImageView)view.findViewById(R.id.issueImage)).setBackground(issueList.get(position).getIssueImage());
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
                args.putByteArray("imagedata",selectedIssue.getData());
                args.putString("key",issueKeys.get(position));
                EditIssueFragment editFragment = new EditIssueFragment();
                editFragment.setArguments(args);
                editFragment.show(fm, "Android Dialog");
                return true;
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
        issueRef.child(issueAutoID).child("details").setValue(newIssue.getIssueDetails());
        issueRef.child(issueAutoID).child("location").setValue(newIssue.getIssueLocation());
        issueRef.child(issueAutoID).child("status").setValue(newIssue.getIssueStatus());
        issueRef.child(issueAutoID).child("report").setValue(reportAutoID);
        issueRef.child(issueAutoID).child("image").setValue(newIssue.getData());
        final Bitmap newImage = newIssue.getBmap();
        write(newImage,issueAutoID,newIssue);
        mAdapter.notifyDataSetChanged();
    }
    public void onEdit(Issue editIssue, int position){
        DatabaseReference itemRef = mAdapter.getRef(position);
        itemRef.child("status").setValue(editIssue.getIssueStatus());
        itemRef.child("location").setValue(editIssue.getIssueLocation());
        itemRef.child("details").setValue(editIssue.getIssueDetails());
        write(editIssue.getBmap(),itemRef.getKey(),editIssue);
        mAdapter.notifyDataSetChanged();
        issueList.set(position,editIssue);
    }
    public void onDelete(int position){
        final String TAG = "DELETION";
        DatabaseReference itemRef = mAdapter.getRef(position);
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
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
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

}