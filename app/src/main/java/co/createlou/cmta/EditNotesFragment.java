package co.createlou.cmta;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class EditNotesFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    OnNotesEditDataPass dataPasser;
    final ArrayList<String> notesList = new ArrayList<>();
    ListView mListView;
    ArrayAdapter<String> adapter;
    private Button addButton;
    private EditText noteText;
    String key;
    public EditNotesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        key = getArguments().getString("key");
        Query reportQuery = FirebaseDatabase.getInstance().getReference().child("reports").child(key).child("notes");
        reportQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        //Pulling the project keys and creating a hashmap of the project data
                        String key = snap.getKey();
                        notesList.add((String)snap.getValue());
                        Log.d("MAIN", key);
                    }
                }
            }
            public void onCancelled (DatabaseError firebaseError){
                //doing nothing for now // TODO: 3/27/2017 Add Error Handling
            }
        });
        // Inflate the layout for this fragment
        View notesView = inflater.inflate(R.layout.fragment_edit_notes,container,false);
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
            dataPasser = (OnNotesEditDataPass) fragment;
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
    public interface OnNotesEditDataPass {
        public void onNotesEditDataPass(List<String> data);
    }
    public void passData(List<String> data) {
        dataPasser.onNotesEditDataPass(data);
    }

}
