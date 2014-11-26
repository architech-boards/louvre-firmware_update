package com.example.lorenzo.louvrefirmapp.ActivityAndFragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.lorenzo.louvrefirmapp.R;
import com.example.lorenzo.louvrefirmapp.RegistersListview.RegisterItems;
import com.example.lorenzo.louvrefirmapp.RegistersListview.RegisterListAdapter;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 */
public class TagRegistersFragment extends Fragment implements AbsListView.OnItemClickListener
{
    private OnFragmentInteractionListener mListener;

    RegisterItems   registerItems;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter<RegisterItems.Item> mAdapter;


    public static TagRegistersFragment newInstance()
    {
        return new TagRegistersFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TagRegistersFragment() { }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerItems = new RegisterItems();

//        mAdapter = new RegisterListAdapter<RegisterItems.Item>(getActivity(),
//                R.layout.register_list_item, registerItems.getItemsList());
        mAdapter = new RegisterListAdapter<RegisterItems.Item>(getActivity(),
                android.R.layout.simple_spinner_item, registerItems.getItemsList());
    }


    /**
     * Add specified registerItem element to the list
     * @param registerItem element to add to the list
     */
    public void addItemToList(RegisterItems.Item registerItem)
    {
        mAdapter.add(registerItem);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tagregisters, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
       (mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(registerItems.getItemsList().get(position).getId());
        }
    }


    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
