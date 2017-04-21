package co.createlou.cmta;

/**
 * Created by Bryan on 1/5/17.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class IssueFragment extends DialogFragment implements View.OnClickListener, OnItemSelectedListener  {

    private static final String TAG = "IssueDetails";
    private static final int REQUEST_IMAGE_CAPTURE = 1888;

    private EditText editIssueLocation;
    private EditText editIssueDetails;
    private Spinner editIssueStatus;
    private ImageButton camButton;
    private BitmapDrawable bdrawable;
    private Bitmap bmap;
    public String spinnerItem;
    public String report;
    public boolean okayToDismiss = false;
    private Uri imageUri;
    File photo;
    String imageurl;

    private Boolean wantToCloseDialog;
    private byte[] imageData;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder createIssueAlert = new AlertDialog.Builder(getActivity());
        createIssueAlert.setTitle("Create Issue");
        Bundle args = getArguments();
        report = args.getString("report_name");
        wantToCloseDialog = false;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_issue, null);
        //setting the fragment alert view to the view initialized and inflated above
        createIssueAlert.setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //Do nothing here because we override this button later to change the close behaviour.
                        //However, we still need this because on older versions of Android unless we
                        //pass a handler the button doesn't get instantiated
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,	int which) {
                        dismiss();
                    }
                });
        //Initializing the EditTexts from above to casts of the cooresponding views in the fragment
        editIssueDetails = (EditText) dialogView.findViewById(R.id.issueDetails);
        editIssueLocation = (EditText) dialogView.findViewById(R.id.issueLocation);
        editIssueStatus = (Spinner)dialogView.findViewById(R.id.spinner);
        editIssueStatus.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.status ,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editIssueStatus.setAdapter(adapter);
        camButton = (ImageButton) dialogView.findViewById(R.id.imageButton);
        camButton.setOnClickListener(this);
        return createIssueAlert.create();
    }

    @Override
    public void onStart()
    {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    createIssue();
                    if(wantToCloseDialog)
                        d.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerItem = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public  interface OnCompleteListener {
        void onComplete(Issue issue);

    }
    private OnCompleteListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mListener = (OnCompleteListener)context;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCompleteListener");
        }
    }

    //Getter methods for issue name, number, and location
    public String getIssueLocation() {
        return editIssueLocation.getText().toString();
    }
    public String getIssueDetails() {
        return editIssueDetails.getText().toString();
    }
    public String getIssueStatus() {
        return spinnerItem;
    }

    public void createIssue() {

        final String issueLocation = getIssueLocation();
        final String issueDetails = getIssueDetails();
        final String issueStatus = getIssueStatus();


        if (TextUtils.isEmpty(issueLocation)) {
            Toast.makeText(getActivity(), "Please enter a Issue Location", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(issueDetails)) {
            Toast.makeText(getActivity(), "Please enter Issue Details", Toast.LENGTH_LONG).show();
            return;
        }
        if(!okayToDismiss){
            Toast.makeText(getActivity(), "Please add issue data", Toast.LENGTH_LONG).show();
            return;
        }
        Issue issue = new Issue(issueLocation, issueStatus, issueDetails,report, imageData,imageData.toString(), bdrawable,bmap);
        Log.d(TAG, "Issue Added with details " + issue.location +", " + issue.status +", " + issue.details);
        wantToCloseDialog = true;
        this.mListener.onComplete(issue);

    }

    public void onClick(View view){
        if(view.getId() == R.id.imageButton){ //&& hasPermissionInManifest(getBaseContext(), "CAMERA")) {

            Intent camintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try{
                photo = this.createTemporaryFile("picture",".jpg");
                photo.delete();

            }catch(Exception e)
            {
                Log.v(TAG, "Can't create file to take picture!");
                Toast.makeText(getActivity(), "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG);
            }
            imageUri = Uri.fromFile(photo);
            camintent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            if (camintent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(camintent,REQUEST_IMAGE_CAPTURE);
            }
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap tempbmap = (Bitmap) extras.get("data");
//            bdrawable = new BitmapDrawable(getResources(),tempbmap);
//            camButton.setBackground(bdrawable);
//            encodeBitmap(bmap);
            this.grabImage(camButton);
            okayToDismiss = true;
        }
    }
    public void encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,25,baos);
        imageData = baos.toByteArray();
    }
    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }
    public void grabImage(ImageButton imageView)
    {
        getActivity().getContentResolver().notifyChange(imageUri, null);
        ContentResolver cr = getActivity().getContentResolver();
        try
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            InputStream stream = cr.openInputStream(imageUri);
            bmap = BitmapFactory.decodeStream(stream,null,options);
            encodeBitmap(bmap);
            Glide.with(getActivity()).load(imageData).into(imageView);
            //imageView.setImageBitmap(thumbImage);
        }
        catch (Exception e)
        {
            Toast.makeText(getActivity(), "Failed to load", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to load", e);
        }
    }
//public void onClick(View view){
//    if(view.getId() == R.id.imageButton){ //&& hasPermissionInManifest(getBaseContext(), "CAMERA")) {
//        Intent camintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (camintent.resolveActivity(getActivity().getPackageManager()) != null) {
//            startActivityForResult(camintent, REQUEST_IMAGE_CAPTURE);
//        }
//    }
//
//}
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            bmap = (Bitmap) extras.get("data");
//            bdrawable = new BitmapDrawable(getResources(),bmap);
//            camButton.setBackground(bdrawable);
//            encodeBitmap(bmap);
//            okayToDismiss=true;
//        }
//    }
//    public void encodeBitmap(Bitmap bitmap) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        imageData = baos.toByteArray();
//    }
}
