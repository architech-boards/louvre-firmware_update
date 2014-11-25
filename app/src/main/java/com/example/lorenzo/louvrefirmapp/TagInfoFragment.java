package com.example.lorenzo.louvrefirmapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lorenzo.louvrefirmapp.NFCLogic.Reader;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.example.lorenzo.louvrefirmapp.TagInfoFragment.OnTagInfoFragmentInterListener} interface
 * to handle interaction events.
 * Use the {@link TagInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class TagInfoFragment extends Fragment implements View.OnClickListener
{

    private OnTagInfoFragmentInterListener onTagInfoFragmentInterListener;
    private static final int PICKFILE_RESULT_CODE = 1000; // Id for file browser intent

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TagInfoFragment.
     */
    public static TagInfoFragment newInstance()
    {
        TagInfoFragment fragment = new TagInfoFragment();

        // Parse Bundle for ntagReader
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }
    public TagInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tag_info, container, false);

        Button bt_browse = (Button)v.findViewById(R.id.browse_button);
        bt_browse.setOnClickListener(this);

        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onTagInfoFragmentInterListener = (OnTagInfoFragmentInterListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        onTagInfoFragmentInterListener = null;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.browse_button :
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent,PICKFILE_RESULT_CODE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != Activity.RESULT_OK)
        {
            Toast.makeText(getActivity().getBaseContext(), "Browse cancelled", Toast.LENGTH_SHORT);
            return;
        }

        // Check which request we're responding to
        switch(requestCode)
        {
            case PICKFILE_RESULT_CODE:
                Uri selectedFile = data.getData();
                EditText et = (EditText)getActivity().findViewById(R.id.et_filename);
                et.setText(selectedFile.toString());
                break;
        }
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
    public interface OnTagInfoFragmentInterListener
    {
        public void onScanTagClick();
    }

}
