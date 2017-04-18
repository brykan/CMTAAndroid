package co.createlou.cmta;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class NotesFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    OnNotesDataPass dataPasser;
    final ArrayList<String> notesList = new ArrayList<>();
    ListView mListView;
    ArrayAdapter<String> adapter;
    private Button addButton;
    private EditText noteText;

    public NotesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View notesView = inflater.inflate(R.layout.fragment_notes,container,false);
        mListView = (ListView)notesView.findViewById(R.id.noteslistview);
        mListView.setAdapter(adapter);
        addButton = (Button)notesView.findViewById(R.id.button4);
        noteText = (EditText)notesView.findViewById(R.id.noteText);
        addButton.setOnClickListener(this);
        onAttachToParentFragment(getParentFragment());

        return notesView;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        adapter= new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_list_item_1,notesList);

    }
    public void onAttachToParentFragment(Fragment fragment)
    {
        try {
            dataPasser = (OnNotesDataPass) fragment;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement onReportDataPass");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public String getNoteText(){
        return noteText.getText().toString();
    }
    @Override
    public void onClick(View v) {
        addNote();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    public List<String> getNotes(){

        return notesList;
    }
    public void addNote(){
        final String text = getNoteText();
        if(TextUtils.isEmpty(text)){
            Toast.makeText(getActivity(),"Please enter note text", Toast.LENGTH_LONG).show();
            return;
        }
        notesList.add(text);
        adapter.notifyDataSetChanged();
    }
    public void dataPassTrigger() {

        List<String> data = getNotes();
        passData(data);

    }
    public interface OnNotesDataPass {
        public void onNotesDataPass(List<String> data);
    }
    public void passData(List<String> data) {
        dataPasser.onNotesDataPass(data);
    }

}
