package it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic;

import it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic.Exc.HexRecordParsingExc;

/**
 * HexFileRecord.java
 *
 * Purpose: define the structure of a hex file record according to the Intel specs and provide some
 * logic to handle the data.
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class HexFileRecord
{
    short       byteCount;      // unsigned byte
    int         address;        // unsigned short
    byte        recordType;
    short[]     data;           // unsigned byte


    public HexFileRecord(short byteCount, int address, byte recordType, short[] data)
            throws HexRecordParsingExc
    {
        this.byteCount = byteCount;
        this.address = address;
        this.recordType = recordType;
        this.data = data;
    }

}
