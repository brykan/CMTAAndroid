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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class EditReportPager extends DialogFragment implements EditReportFragment.OnReportEditDataPass, EditNotesFragment.OnNotesEditDataPass,View.OnClickListener{

    private static final String TAG = "ReportDetails";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    public boolean wantToCloseDialog;
    public String project;
    public EditReportFragment dataReferenceReport;
    public EditNotesFragment dataReferenceNotes;
    public ArrayList<String> reportData;
    public List<String> noteData;
    private Button okButton;
    private Button cancelButton;
    private ImageButton deleteButton;
    private int position;
    public Report myReport;
    String key;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        Bundle args = getArguments();
//        project = args.getString("project_name");
        myReport = args.getParcelable("report_parcel");
        position = args.getInt("position");
        key = args.getString("key");
        View dialogView = inflater.inflate(R.layout.fragment_edit_report_pager, null);
        wantToCloseDialog = false;
//        dataReferenceNotes = new NotesFragment();
//        dataReferenceReport = new ReportFragment();


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) dialogView.findViewById(R.id.container);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);
        //setting the fragment alert view to the view initialized and inflated above

        okButton = (Button)dialogView.findViewById(R.id.button5);
        okButton.setOnClickListener(this);
        cancelButton = (Button)dialogView.findViewById(R.id.button6);
        cancelButton.setOnClickListener(this);
        deleteButton = (ImageButton)dialogView.findViewById(R.id.button3);
        deleteButton.setOnClickListener(this);
        Log.d(TAG, "onCreateDialog: FUCK");
        return dialogView;
    }

    @Override
    public void onStart()
    {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        /*final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    createReport();
                    if(wantToCloseDialog)
                        d.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }*/
    }

    @Override
    public void onReportEditDataPass(ArrayList<String> data) {
        this.reportData = new ArrayList<>(data);
    }
    @Override
    public void onNotesEditDataPass(List<String> data) {
        noteData = data;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button5:
                editReport();
                if(wantToCloseDialog) {
                    getDialog().dismiss();

                }
                break;
            case R.id.button6:
                getDialog().dismiss();
                break;
            case R.id.button3:
                deleteReport();
                getDialog().dismiss();

        }
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
    public void getData(){
        dataReferenceReport.dataPassTrigger();
        dataReferenceNotes.dataPassTrigger();
    }
    public void editReport() {
        getData();
        ArrayList<String> report = new ArrayList<>(reportData);
        final String prepBy = report.get(0);
        final String punchType = report.get(1);
        final String visitDate = report.get(2);
        final List<String> notes = noteData;
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
        myReport.setSiteVisitDate(visitDate);
        myReport.setPunchListType(punchType);
        myReport.setNotes(notes);
        wantToCloseDialog = true;
        this.mListener.onEdit(myReport,position);

    }
    public void deleteReport(){
        this.mListener.onDelete(position);

    }
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    EditReportFragment tab1 = new EditReportFragment();
                    Bundle args = new Bundle();
                    args.putString("project_name",project);
                    args.putParcelable("report_parcel",myReport);
                    tab1.setArguments(args);
                    dataReferenceReport = tab1;
                    return tab1;
                case 1:
                    EditNotesFragment tab2 = new EditNotesFragment();
                    Bundle args1 = new Bundle();
                    args1.putString("project_name",project);
                    args1.putString("key",key);
                    args1.putParcelable("report_parcel",myReport);
                    tab2.setArguments(args1);
                    dataReferenceNotes = tab2;
                    return tab2;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
            }
            return null;
        }
    }
}
