package co.createlou.cmta;

/**
 * Created by Bryan on 1/5/17.
 */

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ReportFragment extends DialogFragment implements OnItemSelectedListener {

    private static final String TAG = "ReportDetails";

    private EditText editPreparedBy;
    private Spinner editReportPunchListType;
    private EditText editSiteVisitDate;
    private String spinnerItem;
    private Boolean wantToCloseDialog;
    private String project;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder createReportAlert = new AlertDialog.Builder(getActivity());
        createReportAlert.setTitle("Create Report");
        wantToCloseDialog = false;
        Bundle args = getArguments();
        project = args.getString("project_name");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_report, null);
        //setting the fragment alert view to the view initialized and inflated above
        createReportAlert.setView(dialogView)
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
        editPreparedBy = (EditText) dialogView.findViewById(R.id.editPreparedBy);
        editReportPunchListType = (Spinner) dialogView.findViewById(R.id.spinner);
        editSiteVisitDate = (EditText) dialogView.findViewById(R.id.editSiteVisitDate);
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
        return createReportAlert.create();
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

        String myFormat = "LLL dd, yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editSiteVisitDate.setText(sdf.format(myCalendar.getTime()));
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerItem = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public  interface OnCompleteListener {
        void onComplete(Report report);

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

    public void createIssue() {

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

        Report report = new Report(prepBy, project, punchType,visitDate);
        Log.d(TAG, "Report Added to project " +report.getProject()+  " with details " + report.getPreparedBy() +", " + report.getPunchListType() +", " + report.getSiteVisitDate());
        wantToCloseDialog = true;
        this.mListener.onComplete(report);

    }
}
