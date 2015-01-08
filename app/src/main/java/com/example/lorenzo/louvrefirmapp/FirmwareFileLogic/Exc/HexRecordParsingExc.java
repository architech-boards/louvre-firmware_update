package com.example.lorenzo.louvrefirmapp.FirmwareFileLogic.Exc;


public class HexRecordParsingExc extends Exception
{
    public HexRecordParsingExc(String msg)
    {
        super();
    }

    public HexRecordParsingExc(String msg, Throwable innerExc)
    {
        super(msg, innerExc);
    }
}

