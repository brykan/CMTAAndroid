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
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

public class IssueFragment extends DialogFragment {

    private static final String TAG = "IssueDetails";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private EditText editIssueLocation;
    private EditText editIssueDetails;
    private Spinner editIssueStatus;
    private ImageButton camButton;
    private BitmapDrawable bdrawable;

    private Boolean wantToCloseDialog;
    private byte[] imageData;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder createIssueAlert = new AlertDialog.Builder(getActivity());
        createIssueAlert.setTitle("Create Issue");
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
        String[] items = new String[]{"open", "closed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        editIssueStatus.setAdapter(adapter);

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
        return editIssueStatus.toString();
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

        Issue issue = new Issue(issueLocation, issueDetails, issueStatus, imageData, bdrawable);
        Log.d(TAG, "Issue Added with details " + issue.issueLocation +", " + issue.issueDetails +", " + issue.issueStatus);
        wantToCloseDialog = true;
        this.mListener.onComplete(issue);

    }
    public void onClick(View view){
        if(view.getId() == R.id.imageButton){ //&& hasPermissionInManifest(getBaseContext(), "CAMERA")) {
            Intent camintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (camintent.resolveActivity(getActivity().getPackageManager()) != null) {
                getActivity().startActivityForResult(camintent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        camButton = (ImageButton) getView().findViewById(R.id.imageButton);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            bdrawable = new BitmapDrawable(getResources(),imageBitmap);
            camButton.setBackground(bdrawable);
            encodeBitmap(imageBitmap);
        }
    }
    public void encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        imageData = baos.toByteArray();
    }

}
