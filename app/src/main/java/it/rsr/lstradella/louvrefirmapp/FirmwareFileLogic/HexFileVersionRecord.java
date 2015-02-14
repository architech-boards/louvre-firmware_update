package it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic;

import it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic.Exc.HexRecordParsingExc;

/**
 * HexFileVersionRecord.java
 *
 * Purpose: define the structure of a hex file version record and provide some logic to handle
 * version information data.
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class HexFileVersionRecord extends HexFileRecord
{
    String versionId;


    public HexFileVersionRecord(short byteCount, int address, byte recordType, short[] data)
            throws HexRecordParsingExc
    {
        super(byteCount, address, recordType, data);

        getVersionIdFromData();
    }


    /**
     * Retrieve version string from data array
     */
    private void getVersionIdFromData()
    {
        versionId = "";

        for(short s : data)
        {
            versionId += Short.toString(s);
        }
    }
}
