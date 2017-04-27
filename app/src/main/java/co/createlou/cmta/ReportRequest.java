package co.createlou.cmta;

/**
 * Created by Bryan on 1/5/17.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;

public class ReportRequest extends DialogFragment {

    private static final String TAG = "ProjectDetails";

    private EditText editFileName;
    private EditText editEmail;
    private String reportKey;
    private Boolean wantToCloseDialog;
    public String urlString;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder createProjectAlert = new AlertDialog.Builder(getActivity());
        createProjectAlert.setTitle("Export Report PDF");
        wantToCloseDialog = false;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_report_request, null);
        //setting the fragment alert view to the view initialized and inflated above
        createProjectAlert.setView(dialogView)
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
        editFileName = (EditText) dialogView.findViewById(R.id.editFileName);
        editEmail = (EditText) dialogView.findViewById(R.id.editEmailAddress);
        reportKey = getArguments().getString("key");
        return createProjectAlert.create();
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
                    emailRequest();
                    if(wantToCloseDialog)
                        d.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }
    public  interface OnCompleteListener {
        void onComplete(String url);

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

    //Getter methods for filename and email address
    public String getFileName() {
        return editFileName.getText().toString();
    }
    public String getEmail() {
        return editEmail.getText().toString();
    }


    public void emailRequest() {

        final String fileName = getFileName();
        final String email = getEmail();
        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(getActivity(), "Please enter a File Name", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Please enter an E-Mail Address", Toast.LENGTH_LONG).show();
            return;
        }
        fileName.replace(" ","%20");
        urlString = "https://us-central1-cmta-8ecda.cloudfunctions.net/createPDF?reportKey=" + reportKey + "&fileName=" + fileName + "&toEmail=" + email;
        wantToCloseDialog = true;
        this.mListener.onComplete(urlString);
    }

}