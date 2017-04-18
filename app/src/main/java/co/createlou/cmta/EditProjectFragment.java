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
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class EditProjectFragment extends DialogFragment {

    private static final String TAG = "ProjectDetails";

    private EditText editProjectName;
    private EditText editProjectNumber;
    private EditText editProjectLocation;
    private Boolean wantToCloseDialog;
    public Project myProject;
    private int position;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder createProjectAlert = new AlertDialog.Builder(getActivity());

        Bundle args = getArguments();
        myProject = args.getParcelable("project_parcel");
        position = args.getInt("position");
        wantToCloseDialog = false;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_edit_project, null);
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
        editProjectName = (EditText) dialogView.findViewById(R.id.editProjectName1);
        editProjectName.setText(myProject.getProjectName());
        editProjectNumber = (EditText) dialogView.findViewById(R.id.editProjectNumber1);
        editProjectNumber.setText(myProject.getProjectNumber());
        editProjectLocation = (EditText) dialogView.findViewById(R.id.editProjectLocation1);
        editProjectLocation.setText(myProject.getProjectLocation());

        ImageButton deleteButton = (ImageButton) dialogView.findViewById(R.id.button2);
        deleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteProject();
                getDialog().dismiss();

                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });
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
                    editProject();
                    if(wantToCloseDialog)
                        d.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });

        }
    }
    public  interface OnCompleteListener {
        void onEdit(Project project,int position);
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

    public void editProject() {

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
        myProject.setProjectName(projname);
        myProject.setProjectNumber(projnum);
        myProject.setProjectLocation(projloc);
        Log.d(TAG, "Project Edited with details " + myProject.getProjectName() +", " + myProject.getProjectNumber() +", " + myProject.getProjectLocation());
        wantToCloseDialog = true;
        this.mListener.onEdit(myProject,position);

    }
    public void deleteProject(){
        this.mListener.onDelete(position);

    }

}