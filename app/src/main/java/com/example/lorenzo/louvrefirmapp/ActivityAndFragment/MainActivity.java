package com.example.lorenzo.louvrefirmapp.ActivityAndFragment;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.TextView;
import android.nfc.NfcAdapter;
import android.widget.Toast;

import com.example.lorenzo.louvrefirmapp.NFCLogic.Exc.BytesToWriteExceedMax;
import com.example.lorenzo.louvrefirmapp.NFCLogic.Masks;
import com.example.lorenzo.louvrefirmapp.NFCLogic.Reader;
import com.example.lorenzo.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException;
import com.example.lorenzo.louvrefirmapp.R;
import com.example.lorenzo.louvrefirmapp.RegistersListview.RegisterItems;

import java.io.IOException;
import java.util.FormatFlagsConversionMismatchException;

//TODO sistemare codice pulendo commmenti e eliminando riferimenti non utilizzati

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
                   TagRegistersFragment.OnFragmentInteractionListener,
                   FirmwareUpdateFragment.OnTagInfoFragmentInterListener
{

    transient Reader ntagReader;


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    TagRegistersFragment tagRegistersFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence    mTitle;
    private NfcAdapter      mAdapter;
    private PendingIntent   mPendingIntent;
    private IntentFilter[]  mFilters;
    private String[][]      mTechLists;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Set up foreground dispatcher to use to handle scanned tag with app in foreground
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, ((Object) this).getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ntech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        mFilters = new IntentFilter[] {ntech};
        mTechLists = new String[][] { new String[] { NfcA.class.getName() } };
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        this.mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);

        // Check if the application is started because a NTAG was scanned
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction()))
        {
            Tag discoveredTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);

            this.ntagReader = new Reader(discoveredTag);
        }
    }


    /**
     * Called when activity is in foreground and a NTAG is scanned due to foreground dispatch
     * implementation
     *
     * @param intent
     */
    @Override
    public void onNewIntent(Intent intent){

        // Fetch the tag from the intent and create the reader object
        Tag discoveredTag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        this.ntagReader = new Reader(discoveredTag);

        // Switch the page displayed to find the operation to perform on the tag
        switch(this.mNavigationDrawerFragment.getCurrentSelectedPosition())
        {
            // Tag info fragment
            case 0:
                readTagRegisters();
                break;

            // Firmware upload fragment
            case 1:
                writeFakeDataToSRAM();
                break;
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();

        switch(position) // Changes here may need to change also onNewIntent position based switch logic
        {
            case 0:
            {
                this.tagRegistersFragment = TagRegistersFragment.newInstance();
                fragmentManager.beginTransaction().replace(R.id.container,
                        tagRegistersFragment).commit();
                break;
            }

            case 1:
            {
                fragmentManager.beginTransaction().replace(R.id.container,
                        FirmwareUpdateFragment.newInstance()).commit();
                break;
            }
        }
    }


    /**
     * Read the tag registers and display them to the user
     */
    public void readTagRegisters()
    {
        String errorNoTag =         "No tag scanned";
        String errorConnect =       "Failed to connect to the tag";
        String errorRead =          "Failed to read the tag";
        String errorDisconnect =    "Failed to disconnect from the tag";
        String errorAddress =       "Address block is out of range";
        String notConnected =       "Reader not connected";

        // Check if a tag was scanned
        if(this.ntagReader == null)
        {
            Toast.makeText(getApplicationContext(), errorNoTag,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to open a connection to the tag
        if(!this.ntagReader.connect())
        {
            Toast.makeText(getApplicationContext(), errorConnect,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Communicate with the tag retrieving registers information and close the communication at the end
        try
        {
            // Wait for PTHRU_ON_OFF
            Log.d("Read registers", "Start waiting for PTHRU_ON_OFF set to 1 ...");
            while(ntagReader.get_NC_REG_sessField(Masks.NC_REG_Sess.PTHRU_ON_OFF) == 0)
            { }

            Log.d("Read registers", "Start retrieving registers info ...");
            tagRegistersFragment.addItemToList(new RegisterItems.Item("RF_LOCKED",
                    Byte.toString(ntagReader.get_NS_REG_sessField(Masks.NS_REG_Sess.RF_LOCKED))));
            tagRegistersFragment.addItemToList(new RegisterItems.Item("I2C_LOCKED",
                    Byte.toString(ntagReader.get_NS_REG_sessField(Masks.NS_REG_Sess.I2C_LOCKED))));
            tagRegistersFragment.addItemToList(new RegisterItems.Item("RF_FIELD_PRESENT",
                    Byte.toString(ntagReader.get_NS_REG_sessField(Masks.NS_REG_Sess.RF_FIELD_PRESENT))));
            tagRegistersFragment.addItemToList(new RegisterItems.Item("SRAM_RF_READY",
                    Byte.toString(ntagReader.get_NS_REG_sessField(Masks.NS_REG_Sess.SRAM_RF_READY))));
            tagRegistersFragment.addItemToList(new RegisterItems.Item("SRAM_I2C_READY",
                    Byte.toString(ntagReader.get_NS_REG_sessField(Masks.NS_REG_Sess.SRAM_I2C_READY))));
            tagRegistersFragment.addItemToList(new RegisterItems.Item("PTHRU_ON_OFF",
                    Byte.toString(ntagReader.get_NC_REG_sessField(Masks.NC_REG_Sess.PTHRU_ON_OFF))));
            tagRegistersFragment.addItemToList(new RegisterItems.Item("FD_ON",
                    Byte.toString(ntagReader.get_NC_REG_sessField(Masks.NC_REG_Sess.FD_ON))));
            tagRegistersFragment.addItemToList(new RegisterItems.Item("FD_OFF",
                    Byte.toString(ntagReader.get_NC_REG_sessField(Masks.NC_REG_Sess.FD_OFF))));

            Log.d("Read registers", "Registers info retrieved");
        }
        catch (IOException ioexc)
        {
            Toast.makeText(getApplicationContext(), errorRead,
                    Toast.LENGTH_SHORT).show();
        }
        catch(IndexOutOfBoundsException iobexc)
        {
            Toast.makeText(getApplicationContext(), errorAddress,
                    Toast.LENGTH_SHORT).show();
        }
        catch (ReaderNotConnectedException rncexc)
        {
            Toast.makeText(getApplicationContext(), notConnected,
                    Toast.LENGTH_SHORT).show();
        }

        if(!this.ntagReader.disconnect())
        {
            Toast.makeText(getApplicationContext(), errorDisconnect,
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void writeFakeDataToSRAM()
    {
        String errorNoTag =         "No tag scanned";
        String errorConnect =       "Failed to connect to the tag";
        String errorWrite =         "Failed to write SRAM";
        String errorDisconnect =    "Failed to disconnect from the tag";
        String errorAddress =       "Address block is out of range";
        String notConnected =       "Reader not connected";
        String dataToLong =         "Data to write is over 64 bytes";
        String dataFormat =         "Data format exception";

        // Check if a tag was scanned
        if(this.ntagReader == null)
        {
            Toast.makeText(getApplicationContext(), errorNoTag, Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to open a connection to the tag
        if(!this.ntagReader.connect())
        {
            Toast.makeText(getApplicationContext(), errorConnect, Toast.LENGTH_SHORT).show();
            return;
        }

        // Communicate with the tag writing to SRAM and close the communication at the end
        try
        {
            byte[] fakeData = new byte[64];
            for(int i = 0; i < fakeData.length; i++)
            {
                fakeData[i] = 0x0A;
            }
            ntagReader.writeSRAM(fakeData); // Write fake data to SRAM
            Toast.makeText(getApplicationContext(), "SRAM written", Toast.LENGTH_SHORT).show();
        }
        catch (IOException ioexc)
        {
            Toast.makeText(getApplicationContext(), errorWrite, Toast.LENGTH_SHORT).show();
        }
        catch(IndexOutOfBoundsException iobexc)
        {
            Toast.makeText(getApplicationContext(), errorAddress, Toast.LENGTH_SHORT).show();
        }
        catch (ReaderNotConnectedException rncexc)
        {
            Toast.makeText(getApplicationContext(), notConnected, Toast.LENGTH_SHORT).show();
        }
        catch (BytesToWriteExceedMax bmax)
        {
            Toast.makeText(getApplicationContext(), dataToLong, Toast.LENGTH_SHORT).show();
        }
        catch (FormatException formexc)
        {
            Toast.makeText(getApplicationContext(), dataFormat, Toast.LENGTH_SHORT).show();
        }

        if(!this.ntagReader.disconnect())
        {
            Toast.makeText(getApplicationContext(), errorDisconnect, Toast.LENGTH_SHORT).show();
        }
    }



    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
            {
                mTitle = getString(R.string.title_get_tag_info);
                break;
            }
            case 2:
            {
                mTitle = getString(R.string.title_upload_firmware);
                break;
            }
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onScanTagClick()
    {
        readTagRegisters();
    }

    @Override
    public void onFragmentInteraction(String id)
    {

    }


//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            return rootView;
//        }
//
//        @Override
//        public void onAttach(Activity activity) {
//            super.onAttach(activity);
//            ((MainActivity) activity).onSectionAttached(
//                    getArguments().getInt(ARG_SECTION_NUMBER));
//        }
//    }

}