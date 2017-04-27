package co.createlou.cmta;

/**
 * Created by Bryan on 1/5/17.
 */

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class EditReportFragment extends DialogFragment implements OnItemSelectedListener,NotesFragment.OnCompleteListener,EditNoteFragment.OnCompleteListener {

    private static final String TAG = "ReportDetails";

    //EditTexts and Spinner
    private EditText editPreparedBy;
    private Spinner editReportPunchListType;
    private EditText editSiteVisitDate;
    private Button addNote;
    private ImageButton deleteButton;

    //ListView Items
    public ListView mListView;
    final List<String> notesList = new ArrayList<>();
    ArrayAdapter<String> mAdapter;

    //Extraneous items
    private String spinnerItem;
    public boolean wantToCloseDialog;
    private Report myReport;
    private int position;
    private String key;

    //Date Items
    String myFormat = "LLL dd, yyyy"; //In which you need put here
    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
    public EditReportFragment(){

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder createProjectAlert = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Bundle args = getArguments();
        myReport = args.getParcelable("report_parcel");
        position = args.getInt("position");
        key = args.getString("key");
        View view = inflater.inflate(R.layout.fragment_edit_report, null);
        mAdapter= new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,notesList);
        mListView = (ListView)view.findViewById(R.id.noteslistview);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedNote = notesList.get(position);
                Bundle args = new Bundle();
                args.putInt("position",position);
                args.putString("note",selectedNote);
                EditNoteFragment editFragment = new EditNoteFragment();
                editFragment.setArguments(args);
                editFragment.show(getChildFragmentManager(), "Android Dialog");
                return true;
            }
        });
        Query noteQuery = FirebaseDatabase.getInstance().getReference().child("reports").child(key).child("notes");
        noteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        //Pulling the project keys and creating a hashmap of the project data
                        String value = (String) postSnapshot.getValue();
                        notesList.add(value);
                        Log.d(TAG, "onDataChange: "+value);
                        Log.d(TAG, "onDataChange: "+notesList.toString());
                        mAdapter.notifyDataSetChanged();
                        //converting the hashmap data into a project object

                        //utilizing the project objects and key data

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        wantToCloseDialog = false;
        createProjectAlert.setView(view)
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

        //Initializing the Items from above to casts of the cooresponding views in the fragment
        editPreparedBy = (EditText) view.findViewById(R.id.editPreparedBy);
        editPreparedBy.setText(myReport.getPreparedBy());
        editReportPunchListType = (Spinner) view.findViewById(R.id.spinner);
        editSiteVisitDate = (EditText) view.findViewById(R.id.editSiteVisitDate);
        editSiteVisitDate.setText(myReport.getSiteVisitDate());
        editReportPunchListType.setOnItemSelectedListener(this);
        editSiteVisitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.punchListType ,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editReportPunchListType.setAdapter(adapter);
        editReportPunchListType.setSelection(getSelectionIndex(myReport.getPunchListType()));
        addNote = (Button) view.findViewById(R.id.addButton);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                NotesFragment notesFragment = new NotesFragment();
                notesFragment.show(getChildFragmentManager(),"Android Dialog");

            }
        });
        deleteButton = (ImageButton) view.findViewById(R.id.button3);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                deleteReport();
                getDialog().dismiss();
            }
        });



        return createProjectAlert.create();
    }

    Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    private void updateLabel() {

        editSiteVisitDate.setText(sdf.format(myCalendar.getTime()));

    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerItem = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
                    editReport();
                    if(wantToCloseDialog)
                        d.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });

//                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.90);
//                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
//                d.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onEdit(String note,int position) {
        notesList.set(position,note);
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public void onDeleteNote(int position){
        notesList.remove(position);
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public void onComplete(String note){
        notesList.add(note);
        mAdapter.notifyDataSetChanged();
    }

    public  interface OnCompleteListener {
        void onEdit(Report report,int position);
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
    public String getPreparedBy() {
        return editPreparedBy.getText().toString();
    }
    public String getPunchListType() {
        return spinnerItem;
    }
    public String getSiteVisitDate() {
        return editSiteVisitDate.getText().toString();
    }

    public void editReport() {

        final String prepBy = getPreparedBy();
        final String punchType = getPunchListType();
        final String visitDate = getSiteVisitDate();


        if (TextUtils.isEmpty(prepBy)) {
            Toast.makeText(getActivity(), "Please enter Who Prepared Item", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(punchType)) {
            Toast.makeText(getActivity(), "Please enter PunchList Type", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(visitDate)) {
            Toast.makeText(getActivity(), "Please enter Site Visit Date", Toast.LENGTH_LONG).show();
            return;
        }
        myReport.setPreparedBy(prepBy);
        myReport.setPunchListType(punchType);
        myReport.setSiteVisitDate(visitDate);
        myReport.setNotes(notesList);
        wantToCloseDialog = true;
        this.mListener.onEdit(myReport,position);
    }
    public void deleteReport(){
        this.mListener.onDelete(position);

    }
    public int getSelectionIndex(String selection){
        switch(selection) {
            case "IN WALL" :
                return 0;
            case "ABOVE CEILING":
                return 1;
            case "FINAL":
                return 2;
            case "SITE OBSERVATION":
                return 3;

        }
        return 0;
    }
}
