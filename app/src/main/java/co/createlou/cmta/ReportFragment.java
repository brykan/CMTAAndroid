package co.createlou.cmta;

/**
 * Created by Bryan on 1/5/17.
 */

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class ReportFragment extends Fragment implements OnItemSelectedListener {

    private static final String TAG = "ReportDetails";

    private EditText editPreparedBy;
    private Spinner editReportPunchListType;
    private EditText editSiteVisitDate;
    private String spinnerItem;
    public String project;
    OnReportDataPass dataPasser;

    public ReportFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        Log.d(TAG, "onCreateView: FUCKINGFUCK");

        project = args.getString("project_name");
        View view = inflater.inflate(R.layout.fragment_report, container,false);
        //Initializing the Items from above to casts of the cooresponding views in the fragment
        editPreparedBy = (EditText) view.findViewById(R.id.editPreparedBy);
        editReportPunchListType = (Spinner) view.findViewById(R.id.spinner);
        editSiteVisitDate = (EditText) view.findViewById(R.id.editSiteVisitDate);
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
        onAttachToParentFragment(getParentFragment());
        return view;
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


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }
    public void onAttachToParentFragment(Fragment fragment)
    {
        try {
            dataPasser = (OnReportDataPass) fragment;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement onReportDataPass");
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
    public interface OnReportDataPass {
        public void onReportDataPass(ArrayList<String> data);
    }
    public void passData(ArrayList<String> data) {
        dataPasser.onReportDataPass(data);
    }
}
