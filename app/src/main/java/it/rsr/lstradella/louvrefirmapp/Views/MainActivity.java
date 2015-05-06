package it.rsr.lstradella.louvrefirmapp.Views;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.nfc.NfcAdapter;
import android.widget.Toast;

import it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic.FirmwareFileRecord;
import it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic.HexFile;
import it.rsr.lstradella.louvrefirmapp.NFCLogic.Masks;
import it.rsr.lstradella.louvrefirmapp.NFCLogic.Reader;
import it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException;
import it.rsr.lstradella.louvrefirmapp.NFCLogic.WriteSramRunnable;
import com.example.lorenzo.louvrefirmapp.R;
import it.rsr.lstradella.louvrefirmapp.Utils.Screen;
import it.rsr.lstradella.louvrefirmapp.Views.RegistersListview.RegisterItems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
                   TagRegistersFragment.OnFragmentInteractionListener,
                   FirmwareUpdateFragment.OnTagInfoFragmentInterListener,
                   Reader.WritingReportProgressCallbacks,
                   WriteSramRunnable.WritingSramCallback
{

    transient Reader        ntagReader;
    HexFile                 hexFile;


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    TagRegistersFragment    tagRegistersFragment;
    FirmwareUpdateFragment  firmwareUpdateFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence    mTitle;
    private NfcAdapter      mAdapter;
    private PendingIntent   mPendingIntent;
    private IntentFilter[]  mFilters;
    private String[][]      mTechLists;

    private boolean         isNfcEnabled;


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

        if(isNfcEnabled = isNfcEnabled())
        {
            // Set up foreground dispatcher to use to handle scanned tag with app in foreground
            mAdapter = NfcAdapter.getDefaultAdapter(this);
            mPendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, ((Object) this).getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter ntech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            mFilters = new IntentFilter[]{ntech};
            mTechLists = new String[][]{new String[]{NfcA.class.getName()}};
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(isNfcEnabled)
        {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);

            // Check if the application is started because a NTAG was scanned
            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction()))
            {
                Tag discoveredTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);

                this.ntagReader = new Reader(discoveredTag, this);
            }
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();

        if(isNfcEnabled)
        {
            mAdapter.disableForegroundDispatch(this);
        }

        // Let the display turn off in case previously blocked
        Screen.releaseScreenOn();
    }

    /**
     * Called when activity is in foreground and a NTAG is scanned due to foreground dispatch
     * implementation
     *
     * @param intent intent with tag information to handle retrieved
     */
    @Override
    public void onNewIntent(Intent intent){

        // Fetch the tag from the intent and create the reader object
        Tag discoveredTag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        this.ntagReader = new Reader(discoveredTag, this);

        // Switch the page displayed to find the operation to perform on the tag
        switch(this.mNavigationDrawerFragment.getCurrentSelectedPosition())
        {
            // Firmware upload fragment
            case 0:
                if(hexFile != null)
                {
                    writeFirmwareFileToSRAM();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "No .hex file loaded successfully", Toast.LENGTH_LONG).show();
                }
                break;

            // Tag info fragment
            case 1:
                readTagRegisters();
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
                // Load the .hex firmware file in memory
                try
                {
                    this.hexFile = new HexFile(getResources());
                    hexFile.readFromRaw();
                }
                catch (Exception exc)
                {
                    hexFile = null;
                    Toast.makeText(getApplicationContext(), "Failed to load Firmware file", Toast.LENGTH_LONG).show();
                }

                this.firmwareUpdateFragment = FirmwareUpdateFragment.newInstance(hexFile.getFileVersion());
                fragmentManager.beginTransaction().replace(R.id.container,
                        firmwareUpdateFragment).commit();
                break;
            }

            case 1:
            {
                this.tagRegistersFragment = TagRegistersFragment.newInstance();
                fragmentManager.beginTransaction().replace(R.id.container,
                        tagRegistersFragment).commit();
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

        // Communicate with the tag retrieving registers information and close the communication
        // at the end
        try
        {
            // Wait for PTHRU_ON_OFF
            Log.d("Read registers", "Start waiting for PTHRU_ON_OFF set to 1 ...");
            while(ntagReader.get_NC_REG_sessField(Masks.NC_REG_Sess.PTHRU_ON_OFF) == 0) { }

            Log.d("Read registers", "Start retrieving registers info ...");
            tagRegistersFragment.addItemToList(new RegisterItems.Item("SESSION REGISTERS", ""));
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
            tagRegistersFragment.addItemToList(new RegisterItems.Item("PTHRU_DIR",
                    Byte.toString(ntagReader.get_NC_REG_sessField(Masks.NC_REG_Sess.PTHRU_DIR))));
            tagRegistersFragment.addItemToList(new RegisterItems.Item("FD_ON",
                    Byte.toString(ntagReader.get_NC_REG_sessField(Masks.NC_REG_Sess.FD_ON))));
            tagRegistersFragment.addItemToList(new RegisterItems.Item("FD_OFF",
                    Byte.toString(ntagReader.get_NC_REG_sessField(Masks.NC_REG_Sess.FD_OFF))));

            tagRegistersFragment.addItemToList(new RegisterItems.Item("CONFIGURATION REGISTERS", ""));
            tagRegistersFragment.addItemToList(new RegisterItems.Item("TRANSFER_DIR",
                    Byte.toString(ntagReader.get_NC_REG_confField(Masks.NC_REG_Conf.TRANSFER_DIR))));

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


    private void writeFirmwareFileToSRAM()
    {
        // Keep display on to avoid communication problems
        Screen.keepScreenOn(getSystemService(Context.POWER_SERVICE));

        // Start writing SRAM in a second thread
        ArrayList<byte[]> bytesList = new ArrayList<>();
        for(Map.Entry entry : hexFile.getFirmwareRecordsMap().entrySet())
        {
            FirmwareFileRecord fr = (FirmwareFileRecord)entry.getValue();
            bytesList.add(fr.getRecordBytes());
        }
        new Thread(new WriteSramRunnable(ntagReader, bytesList, this)).start();
    }

    @Override
    public void onSramBufferWrote(final int currentSession, final int sessions)
    {
        ProgressBar writingProgressBar = (ProgressBar)findViewById(R.id.writing_progressbar);

        // Update progress value via UI Thread
        writingProgressBar.post(new Runnable()
        {
            @Override
            public void run()
            {
                // Setup the progress bar and its parent layout for the first time
                LinearLayout progressbarLayout = (LinearLayout)findViewById(R.id.progressbar_layout);
                ProgressBar writingProgressBar = (ProgressBar)findViewById(R.id.writing_progressbar);
                TextView tvInfo = (TextView)findViewById(R.id.tv_todo_info);

                if(progressbarLayout.getVisibility() == View.GONE)
                {
                    // Update progressbar
                    progressbarLayout.setVisibility(View.VISIBLE);
                    // Update todo_info
                    tvInfo.setText(getResources().getString(R.string.todo_info_2));
                }

                // Check for end of writing operation
                if(currentSession >= sessions)
                {
                    progressbarLayout.setVisibility(View.GONE);
                    writingProgressBar.setProgress(0);
                }
                else
                {
                    writingProgressBar.setIndeterminate(false);
                    writingProgressBar.setMax(sessions-1);
                    writingProgressBar.setProgress(currentSession);
                    tvInfo.setText(getResources().getString(R.string.todo_info_2) +
                            "\n\nSending block  " + currentSession + "  of  " + sessions);
                }
            }
        });
    }

    @Override
    public void onSramBufferWait()
    {
        ProgressBar writingProgressBar = (ProgressBar)findViewById(R.id.writing_progressbar);

        writingProgressBar.post(new Runnable()
        {
            @Override
            public void run()
            {
                // Setup the progress bar and its parent layout for the first time
                LinearLayout progressbarLayout = (LinearLayout)findViewById(R.id.progressbar_layout);
                ProgressBar writingProgressBar = (ProgressBar)findViewById(R.id.writing_progressbar);
                TextView tvInfo = (TextView)findViewById(R.id.tv_todo_info);

                // Update progressbar layout
                writingProgressBar.setIndeterminate(true);
                progressbarLayout.setVisibility(View.VISIBLE);
                // Update todo_info
                tvInfo.setText(getResources().getString(R.string.todo_info_4));
            }
        });
    }

    @Override
    public void onWritingError(final Exception exc)
    {
        TextView tv = (TextView)findViewById(R.id.tv_todo_info);

        tv.post(new Runnable()
        {
            @Override
            public void run()
            {
                // Hide the progressbar layout
                LinearLayout progressbarLayout = (LinearLayout)findViewById(R.id.progressbar_layout);
                progressbarLayout.setVisibility(View.GONE);
                // Update todo_info
                TextView tv = (TextView)findViewById(R.id.tv_todo_info);
                tv.setText(exc.getMessage() + "\n\n" + getResources().getString(R.string.todo_info_1));
            }
        });
    }

    @Override
    public void onSuccessfullyWritten()
    {
        TextView tv = (TextView)findViewById(R.id.tv_todo_info);

        tv.post(new Runnable()
        {
            @Override
            public void run()
            {
                // Update todo_info
                TextView tv = (TextView) findViewById(R.id.tv_todo_info);
                tv.setText(getResources().getString(R.string.todo_info_3));
            }
        });
    }


    private boolean isNfcEnabled()
    {
        NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("NFC not present or not enabled")
                    .setTitle("NFC Warning");

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            return false;
        }

        return true;
    }


    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
            {
                mTitle = getString(R.string.title_upload_firmware);
                break;
            }
            case 2:
            {
                mTitle = getString(R.string.title_get_tag_info);
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

        //return id==R.id.action_settings || super.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onScanTagClick()
    {
        readTagRegisters();
    }


    @Override
    public void onTestProgressbar()
    {
        // Simulate progress reporting from a different thread
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // Test waiting
                onSramBufferWait();
                try
                {
                    Thread.sleep(2000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                // Test progress
                int maxSession = 211;
                for(int i = 0; i < maxSession; i++)
                {
                    onSramBufferWrote(i, maxSession);
                    try
                    {
                        Thread.sleep(100);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                onSuccessfullyWritten();
            }
        }).start();
    }

    @Override
    public void onFragmentInteraction(String id)
    { }


}
