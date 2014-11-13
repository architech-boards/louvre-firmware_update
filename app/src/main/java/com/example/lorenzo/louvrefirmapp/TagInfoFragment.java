package com.example.lorenzo.louvrefirmapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lorenzo.louvrefirmapp.NFCLogic.Reader;

import java.io.IOException;

//TODO creare logica per inserire address block da prelevare -> pulsante scan tag diventa scan address block specificato

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
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View viewInflated = inflater.inflate(R.layout.fragment_tag_info, container, false);

        // Set click listeners inside the fragment
        Button scanButton = (Button) viewInflated.findViewById(R.id.button_scan_tag);
        scanButton.setOnClickListener(this);

        return viewInflated;
    }

    /*
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (onTagInfoFragmentInterListener != null) {

            NumberPicker np = (NumberPicker)getActivity().findViewById(R.id.numberPicker);
            onTagInfoFragmentInterListener.onScanTagClick(np.getValue());
        }
    }
    */


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


    /**
     * Handler of view click event inside the fragment (workaround to avoid having onClick logic
     * only inside the main activity)
     * @param v
     */
    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.button_scan_tag:
            {
                if (onTagInfoFragmentInterListener != null)
                {
                    onTagInfoFragmentInterListener.onScanTagClick(0);//TODO eliminato number pecker
                }
                break;
            }
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
        public void onScanTagClick(int addressBlock);
    }

}
