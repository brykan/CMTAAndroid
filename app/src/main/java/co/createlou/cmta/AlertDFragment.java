package co.createlou.cmta;

/**
 * Created by Bryan on 1/5/17.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;

public class AlertDFragment extends DialogFragment {

    private static final String TAG = "ProjectDetails";

    private EditText editProjectName;
    private EditText editProjectNumber;
    private EditText editProjectLocation;
    private Boolean wantToCloseDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder createProjectAlert = new AlertDialog.Builder(getActivity());

        createProjectAlert.setTitle("Create Project");
        wantToCloseDialog = false;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_main, null);
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
        editProjectName = (EditText) dialogView.findViewById(R.id.editProjectName);
        editProjectNumber = (EditText) dialogView.findViewById(R.id.editProjectNumber);
        editProjectLocation = (EditText) dialogView.findViewById(R.id.editProjectLocation);

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
                    createProject();
                     if(wantToCloseDialog)
                        d.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }
    public  interface OnCompleteListener {
         void onComplete(Project project);

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

    //Getter methods for project name, number, and location
    public String getProjectName() {
        return editProjectName.getText().toString();
    }
    public String getProjectNumber() {
        return editProjectNumber.getText().toString();
    }
    public String getProjectLocation() {
        return editProjectLocation.getText().toString();
    }

    public void createProject() {

        final String projname = getProjectName();
        final String projnum = getProjectNumber();
        final String projloc = getProjectLocation();

        if (TextUtils.isEmpty(projname)) {
            Toast.makeText(getActivity(), "Please enter a Project Name", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(projnum)) {
            Toast.makeText(getActivity(), "Please enter a Project Number", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(projloc)){
            Toast.makeText(getActivity(), "Please enter a Project Location", Toast.LENGTH_LONG).show();
            return;
        }
        Project project = new Project(projname, projnum, projloc);
        Log.d(TAG, "Project Added with details " + project.projectName +", " + project.projectNumber +", " + project.projectLocation);
        wantToCloseDialog = true;
        this.mListener.onComplete(project);

    }

}