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
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class EditNoteFragment extends DialogFragment {

    private static final String TAG = "ProjectDetails";

    private EditText editNote;
    public int position;
    private String noteText;
    public boolean wantToCloseDialog;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder createProjectAlert = new AlertDialog.Builder(getActivity());
        onAttachToParentFragment(getParentFragment());
        Bundle args = getArguments();
        position = args.getInt("position");
        noteText = args.getString("note");
        wantToCloseDialog = false;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_edit_note, null);
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
        editNote = (EditText) dialogView.findViewById(R.id.editNote);
        editNote.setText(noteText);

        ImageButton deleteButton = (ImageButton) dialogView.findViewById(R.id.button2);
        deleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteNote();
                if(wantToCloseDialog) {
                    getDialog().dismiss();
                }
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
            d.setCanceledOnTouchOutside(false);

            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    editNote();
                    if(wantToCloseDialog)
                        d.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });

        }
    }
    public  interface OnCompleteListener {
        void onEdit(String note,int position);
        void onDeleteNote(int position);
    }
    private OnCompleteListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }
    public void onAttachToParentFragment(Fragment fragment)
    {
        try {
            this.mListener = (OnCompleteListener) fragment;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement onReportDataPass");
        }
    }


    //Getter methods for project name, number, and location

    public String getNoteText() {
        return editNote.getText().toString();
    }

    public void editNote() {

        final String note = getNoteText();

        if (TextUtils.isEmpty(note)) {
            Toast.makeText(getActivity(), "Please enter note text", Toast.LENGTH_LONG).show();
            return;
        }

        wantToCloseDialog = true;
        this.mListener.onEdit(note,position);

    }
    public void deleteNote(){
        Log.d(TAG, "deleteNote: "+position);
        wantToCloseDialog = true;
        this.mListener.onDeleteNote(position);


    }

}