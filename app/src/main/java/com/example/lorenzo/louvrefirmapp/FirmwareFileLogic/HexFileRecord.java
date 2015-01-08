package com.example.lorenzo.louvrefirmapp.FirmwareFileLogic;


import com.example.lorenzo.louvrefirmapp.FirmwareFileLogic.Exc.HexRecordParsingExc;


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
