package it.rsr.lstradella.louvrefirmapp.Views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic.Exc.FirmwareRecCreationExc;
import it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic.Exc.HexRecordParsingExc;
import it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic.HexFile;
import com.example.lorenzo.louvrefirmapp.R;

import java.io.IOException;

/**
 * Activities that contain this fragment must implement the
 * {@link FirmwareUpdateFragment.OnTagInfoFragmentInterListener} interface
 * to handle interaction events.
 */
public class FirmwareUpdateFragment extends Fragment implements View.OnClickListener
{

    View    baseView;
    private OnTagInfoFragmentInterListener onTagInfoFragmentInterListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TagInfoFragment.
     */
    public static FirmwareUpdateFragment newInstance(String hexFileRelId)
    {
        FirmwareUpdateFragment fragment = new FirmwareUpdateFragment();

        // Parse Bundle for ntagReader
        Bundle args = new Bundle();
        args.putString(HexFile.VERSION_ARG, hexFileRelId);
        fragment.setArguments(args);

        return fragment;
    }
    public FirmwareUpdateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // To notify of the presence of particular menu
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.firmware_upload, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        this.baseView = inflater.inflate(R.layout.fragment_firmware_upload, container, false);

        // Update file release id
        Bundle args = getArguments();
        if(args != null)
        {
            TextView tvRelId = (TextView)baseView.findViewById(R.id.tv_demo_value);
            tvRelId.setText(args.getString(HexFile.VERSION_ARG));
        }

        return this.baseView;
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        byte[] bytesRead;

        switch(item.getItemId())
        {
            // Read file test from browse string
            case R.id.test_file_browser:
            {
                /*
                bytesRead = new byte[100];
                if (testReadFileFromBrowsing(bytesRead))
                {
                    Log.d("Test read file from browse", "File content:\n" + new String(bytesRead).trim());
                }
                else
                {
                    Log.d("Test read file from browse", "File not read");
                }
                break;
                */
            }

            // Read file test from raw resources
            case R.id.test_file_raw:
            {
                bytesRead = new byte[100];
                if (testReadFileFromRaw(bytesRead))
                {
                    Log.d("Test read file from raw", "File content:\n" + new String(bytesRead).trim());
                }
                else
                {
                    Log.d("Test read file from raw", "File not read");
                }
                break;
            }

            // Test functionality of report progress via progressbar
            case R.id.test_progressbar:
            {
                onTagInfoFragmentInterListener.onTestProgressbar();
                break;
            }

            default: return false;
        }

        return true;
    }


    /**
     * Test the file reading capability.
     * @param byteRead Bytes read from the file
     * @return True if read successfully, otherwise false
     */
    /*
    private boolean testReadFileFromBrowsing(byte[] byteRead)
    {
        String notFoundErr = "File not found";
        String readingErr = "Problem reading file";

        TextView tw = (TextView) baseView.findViewById(R.id.et_filename);
        if(tw.getText().length() == 0)
        {
            Toast.makeText(baseView.getContext(), "No file selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        try
        {
            String filePath = tw.getText().toString().split(":")[1];
            FileInputStream fis = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fis);

            bis.read(byteRead); // Read all the length of the buffer

            return true;
        }
        catch (FileNotFoundException fexc)
        {
            Toast.makeText(baseView.getContext(), notFoundErr, Toast.LENGTH_SHORT).show();
            Log.e("File Reading From Browsing", notFoundErr + " " + tw.getText().toString() , fexc);
            return false;
        }
        catch(IOException ioexc)
        {
            Toast.makeText(baseView.getContext(), readingErr, Toast.LENGTH_SHORT).show();
            Log.e("File Reading From Browsing", readingErr, ioexc);
            return false;
        }
    }
    */


    /**
     * Test the file reading capability.
     * @param byteRead Bytes read from the file
     * @return True if read successfully, otherwise false
     */
    private boolean testReadFileFromRaw(byte[] byteRead)
    {
        try
        {
            HexFile hexFile = new HexFile(getResources());
            hexFile.readFromRaw();
            return true;
        }
        catch(IOException ioexc)
        {
            Log.e("File Reading From Raw", "Read file error", ioexc);
            return false;
        }
        catch (HexRecordParsingExc pe)
        {
            Log.e("File Reading From Raw", "Read file error: .hex parsing error", pe);
            return false;
        }
        catch (FirmwareRecCreationExc fe)
        {
            Log.e("File Reading From Raw", "File pre-processing error: failed to create firmware records", fe);
            return false;
        }
    }


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            /*
            case R.id.browse_button :
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent,PICKFILE_RESULT_CODE);
            */
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != Activity.RESULT_OK)
        {
            Toast.makeText(getActivity().getBaseContext(), "Browse cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check which request we're responding to
        switch(requestCode)
        {
            /*
            case PICKFILE_RESULT_CODE:
                Uri selectedFile = data.getData();
                EditText et = (EditText)getActivity().findViewById(R.id.et_filename);
                et.setText(selectedFile.toString());
                break;
            */
        }
    }


    /**
     * Retrieve the path of the selected file
     * @return
     * File path
     */
    /*
    public String getBrowsedFilePath()
    {
        EditText et = (EditText)getActivity().findViewById(R.id.et_filename);
        return et.getText().toString();
    }
    */


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

        public void onTestProgressbar();
    }

}
