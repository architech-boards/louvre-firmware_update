package com.example.lorenzo.louvrefirmapp.FirmwareFileLogic;

import android.content.res.Resources;
import android.util.Log;

import com.example.lorenzo.louvrefirmapp.FirmwareFileLogic.Exc.FirmwareRecCreationExc;
import com.example.lorenzo.louvrefirmapp.FirmwareFileLogic.Exc.HexRecordParsingExc;
import com.example.lorenzo.louvrefirmapp.R;
import com.example.lorenzo.louvrefirmapp.Utils.ByteArrays;
import com.example.lorenzo.louvrefirmapp.Utils.Int3B;
import com.example.lorenzo.louvrefirmapp.Utils.UnsByte;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

//TODO validare calcolo address in presenza di hex record type 04
//TODO validare endianess "tempAddress"
public class HexFile
{
    private static final int RECORD_DATA_COUNT = 58; // How many record to include in firmwareRecord data field

    Resources                               activityResources;    // To handle file IO
    ArrayList<HexFileRecord>                hexFileRecordsList;
    TreeMap<Integer, FirmwareFileRecord>    firmwareRecordsMap;


    public TreeMap<Integer, FirmwareFileRecord> getFirmwareRecordsMap()
    {
        return firmwareRecordsMap;
    }


    public HexFile(Resources resources)
    {
        this.activityResources = resources;
        this.hexFileRecordsList = new ArrayList<>();
        this.firmwareRecordsMap = new TreeMap<>();
    }


    /**
     * Read .hex file and create the corresponding hex file record list
     * @throws IOException
     * @throws HexRecordParsingExc
     * @throws FirmwareRecCreationExc
     */
    public void readFromRaw() throws IOException, HexRecordParsingExc, FirmwareRecCreationExc
    {
        InputStream is = activityResources.openRawResource(R.raw.firmware);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("ASCII")));

        int upperLinearBaseAddress = 0; // To handle 04 .hex record type
        String readLineStr;
        while ((readLineStr = br.readLine()) != null)
        {
            if (readLineStr.startsWith(":"))    // Check if valid .hex row
            {
                short[] readLineShort = hexStringToByteArray(readLineStr.substring(1)); // Filter the initial ':'

                // Parse the record row
                if(readLineShort.length >= 5) // 5 bytes is the minimum .hex record structure length
                {
                    short byteCount = readLineShort[0];

                    byte recordType = (byte)readLineShort[3];
                    if(recordType == 0x04) // Handle "Extended Linear Address Record"
                    {
                        upperLinearBaseAddress = readLineShort[4];
                    }
                    else if(recordType != 0x01 && recordType != 0x03) // Filter End Of File and 0x03 record type
                    {
                        // Calculate record address and data
                        int tempAddress = readLineShort[1] << 8 | readLineShort[2];
                        int address = tempAddress + (65536 * upperLinearBaseAddress);
                        short[] data = Arrays.copyOfRange(readLineShort, 4, byteCount + 4);

                        hexFileRecordsList.add(new HexFileRecord(byteCount, address, recordType, data));
                    }
                }
            }
        }
        br.close();
        is.close();

        Log.d("readFromRaw", "File parsed");

        createFirmwareRecordsMap();

        Log.d("readFromRaw", "Firmware records created");
    }


    /**
     * Scan the hex file record list and create corresponding firmware record map
     * @throws FirmwareRecCreationExc
     */
    public void createFirmwareRecordsMap() throws FirmwareRecCreationExc
    {
        // Initialize 1 MB array to 0xFF
        short[] flashSimul = new short[1000000];
        Arrays.fill(flashSimul, (short)0xFF);

        try
        {
            // Fill the flashSimul array with the hexRecord data starting at hexRecord address
            for (HexFileRecord hexRecord : hexFileRecordsList)
            {
                int j = 0;
                for(int i = hexRecord.address; i < (hexRecord.data.length + hexRecord.address); i++)
                {
                    flashSimul[i] = hexRecord.data[j];
                    j++;
                }
            }

            // Create firmware record from the flashSimul array
            for(int i = 0; i < flashSimul.length; )
            {
                i = skipValueFF(i, flashSimul); // Skip all the consecutive 0xFF value inside flashSimul

                if(i >= flashSimul.length) // End condition
                {
                    break;
                }

                // Take RECORD_DATA_COUNT valid bytes from array
                int recordAddress = i;
                short[] recordData = new short[RECORD_DATA_COUNT];
                for(int j = 0; j < RECORD_DATA_COUNT && i < flashSimul.length; j++ )
                {
                    recordData[j] = flashSimul[i];
                    i++;
                }

                recordData = trimEndFF(recordData); // Trim trailing 0xFF value

                FirmwareFileRecord firmwareFileRecord = new FirmwareFileRecord(
                        FirmwareFileRecord.CommandsType.BOOTLOADER_RECORD,
                        new Int3B(recordAddress), recordData);

                // Save the firmware record to the map ordered by address
                if(firmwareRecordsMap.put(firmwareFileRecord.address.getIntValue(),
                        firmwareFileRecord) != null)
                {
                    throw new FirmwareRecCreationExc("Firmware record with same address already present");
                }
            }

            // Set last record type of the last record in the map
            firmwareRecordsMap.lastEntry().getValue().command =
                    FirmwareFileRecord.CommandsType.LAST_BOOTLOADER_RECORD.getValue();
        }
        catch (Exception exc)
        {
            throw new FirmwareRecCreationExc("Error creating firmware record", exc);
        }
    }


    /**
     * Return firmware records map values converted into byte array
     * @return firmware record map byte array
     */
    public byte[] getFirmwareRecordsMapBytes()
    {
        byte[] combinedArrays = new byte[0];
        for(Map.Entry entry : firmwareRecordsMap.entrySet())
        {
            FirmwareFileRecord fr = (FirmwareFileRecord)entry.getValue();
            combinedArrays = ByteArrays.concat(combinedArrays, fr.getRecordBytes());
        }

        return combinedArrays;
    }


    /**
     * Skip the value equal to 0xFF inside data array
     * @param index updated index
     * @param data array to consider
     */
    private int skipValueFF(int index, short[] data)
    {
        while(index < data.length) // Skip 0xFF values
        {
            if(data[index] != 0xFF)
            {
                break;
            }

            index++;
        }

        return index;
    }


    /**
     * Trim the data array removing all the trailing 0xFF values
     * @param data Array to trim
     * @return Array trimmed
     */
    private short[] trimEndFF(short[] data)
    {
        int endIndex = data.length;
        for(int i = data.length -1; i != 0; i--)
        {
            if(data[i] != 0xFF)
            {
                endIndex = i + 1;
                break;
            }
        }

        if(endIndex != data.length)
        {
            return Arrays.copyOfRange(data, 0, endIndex);
        }
        else
        {
            return data;
        }
    }


    /**
     * Convert string object to short array
     * @param s string to convert to short array
     * @return short array created from specified string
     * @throws HexRecordParsingExc
     */
    private short[] hexStringToByteArray(String s) throws HexRecordParsingExc
    {
        try
        {
            int len = s.length();
            short[] data = new short[len / 2];
            for (int i = 0; i < len; i += 2)
            {
                data[i / 2] = (short) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }

            return data;
        }
        catch (Exception exc)
        {
            throw new HexRecordParsingExc("Problem converting from hex char to byte", exc);
        }
    }
}
