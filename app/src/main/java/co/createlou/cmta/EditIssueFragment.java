package co.createlou.cmta;

/**
 * Created by Bryan on 1/5/17.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

public class EditIssueFragment extends DialogFragment implements View.OnClickListener, OnItemSelectedListener  {

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
    public Issue myIssue;
    public int position;


    private Boolean wantToCloseDialog;
    private byte[] imageData;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder createIssueAlert = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        report = args.getString("report_name");
        myIssue = args.getParcelable("issue_parcel");
        position = args.getInt("position");
        imageData = args.getByteArray("imagedata");
        bmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        myIssue.setData(imageData);
        myIssue.setBmap(bmap);
        bdrawable = new BitmapDrawable(getResources(),bmap);
        myIssue.setBitmapDrawable(bdrawable);

        wantToCloseDialog = false;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_edit_issue, null);
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
        editIssueDetails.setText(myIssue.getIssueDetails());
        editIssueLocation = (EditText) dialogView.findViewById(R.id.issueLocation);
        editIssueLocation.setText(myIssue.getIssueLocation());
        editIssueStatus = (Spinner)dialogView.findViewById(R.id.spinner);
        editIssueStatus.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.status ,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editIssueStatus.setAdapter(adapter);
        camButton = (ImageButton) dialogView.findViewById(R.id.imageButton);
        camButton.setBackground(myIssue.getIssueImage());
        camButton.setOnClickListener(this);
        ImageButton deleteButton = (ImageButton) dialogView.findViewById(R.id.button3);
        deleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteIssue();
                getDialog().dismiss();

                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });
        editIssueStatus.setSelection((getSelectionIndex(myIssue.getIssueStatus())));
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
                    editIssue();
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
        void onEdit(Issue issue,int position);
        void onDelete(int position);

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

    public void editIssue() {

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

        myIssue.setIssueStatus(issueStatus);
        myIssue.setIssueDetails(issueDetails);
        myIssue.setIssueLocation(issueLocation);
        myIssue.setData(imageData);
        myIssue.setBitmapDrawable(bdrawable);
        myIssue.setBmap(bmap);
        Log.d(TAG, "Issue Added with details " + myIssue.issueLocation +", " + myIssue.issueStatus +", " + myIssue.issueDetails);
        wantToCloseDialog = true;
        this.mListener.onEdit(myIssue,position);

    }
    public void onClick(View view){
        if(view.getId() == R.id.imageButton){ //&& hasPermissionInManifest(getBaseContext(), "CAMERA")) {
            Intent camintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (camintent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(camintent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bmap = (Bitmap) extras.get("data");
            bdrawable = new BitmapDrawable(getResources(),bmap);
            camButton.setBackground(bdrawable);
            encodeBitmap(bmap);
        }
    }
    public void encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        imageData = baos.toByteArray();
    }
    public int getSelectionIndex(String selection){
        switch(selection) {
            case "OPEN" :
                return 0;
            case "CLOSED":
                return 1;

        }
        return 0;
    }
    public void deleteIssue(){
        this.mListener.onDelete(position);

    }
}
