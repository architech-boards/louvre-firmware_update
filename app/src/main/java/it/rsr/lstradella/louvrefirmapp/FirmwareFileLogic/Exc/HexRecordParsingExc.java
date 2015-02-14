package it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic.Exc;

/**
 * HexRecordParsingExc.java
 *
 * Purpose: wrapper for custom .hex file handling exception
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
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

