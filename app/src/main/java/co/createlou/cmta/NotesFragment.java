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
import android.widget.Toast;

public class NotesFragment extends DialogFragment {

    private static final String TAG = "NoteFragment";

    private EditText editNoteText;
    private Boolean wantToCloseDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder createProjectAlert = new AlertDialog.Builder(getActivity());

        createProjectAlert.setTitle("Add Note");
        wantToCloseDialog = false;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_notes, null);
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
        editNoteText = (EditText) dialogView.findViewById(R.id.noteText);
        onAttachToParentFragment(getParentFragment());

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
                    addNote();
                    if(wantToCloseDialog)
                        d.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }
    public  interface OnCompleteListener {
        void onComplete(String note);

    }
    private OnCompleteListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }
    public void onAttachToParentFragment(Fragment fragment){
        try {
            this.mListener = (OnCompleteListener)fragment;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement OnCompleteListener");
        }
    }

    //Getter method for note text
    public String getNote() {
        return editNoteText.getText().toString();
    }

    public void addNote() {

        String note = getNote();

        if (TextUtils.isEmpty(note)) {
            Toast.makeText(getActivity(), "Please enter a Note", Toast.LENGTH_LONG).show();
            return;
        }

        wantToCloseDialog = true;
        this.mListener.onComplete(note);

    }

}