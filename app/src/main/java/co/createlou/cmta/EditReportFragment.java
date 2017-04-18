package co.createlou.cmta;

/**
 * Created by Bryan on 1/5/17.
 */

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditReportFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "ReportDetails";

    private EditText editPreparedBy;
    private Spinner editReportPunchListType;
    private EditText editSiteVisitDate;
    private String spinnerItem;
    private Boolean wantToCloseDialog;
    public Report myReport;
    private int position;
    OnReportEditDataPass dataPasser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        myReport = args.getParcelable("report_parcel");
        wantToCloseDialog = false;
        inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_edit_report,container, false);

        //Initializing the EditTexts from above to casts of the cooresponding views in the fragment
        editPreparedBy = (EditText) dialogView.findViewById(R.id.editPreparedBy1);
        editPreparedBy.setText(myReport.getPreparedBy());
        editReportPunchListType = (Spinner) dialogView.findViewById(R.id.spinner1);
        editSiteVisitDate = (EditText) dialogView.findViewById(R.id.editSiteVisitDate1);
        String initialDate = myReport.getSiteVisitDate();
        editSiteVisitDate.setText(initialDate);
        setInitialDate(initialDate);
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
        editReportPunchListType.setSelection((getSelectionIndex(myReport.getPunchListType())));

        onAttachToParentFragment(getParentFragment());
        return dialogView;
    }

    @Override
    public void onStart()
    {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
//        final AlertDialog d = (AlertDialog)getDialog();
//        if(d != null)
//        {
//            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
//            positiveButton.setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    editReport();
//                    if(wantToCloseDialog)
//                        d.dismiss();
//                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
//                }
//            });
//
//        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }
    public void onAttachToParentFragment(Fragment fragment)
    {
        try {
            dataPasser = (OnReportEditDataPass) fragment;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement onReportDataPass");
        }
    }
    Calendar myCalendar = Calendar.getInstance();
    public void setInitialDate(String date){

        String myFormat = "LLL dd, yyyy"; //In which you need put here
        try{
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        Date d = sdf.parse(date);
        myCalendar.setTime(d);
        }catch(java.text.ParseException e){
            e.printStackTrace();
        }

    }
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


    public void dataPassTrigger() {

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

        ArrayList<String> data = new ArrayList<>();
        data.add(prepBy);
        data.add(punchType);
        data.add(visitDate);
        passData(data);
    }
    public interface OnReportEditDataPass {
        public void onReportEditDataPass(ArrayList<String> data);
    }
    public void passData(ArrayList<String> data) {
        dataPasser.onReportEditDataPass(data);
    }
}