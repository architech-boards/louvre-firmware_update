package it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc;

/**
 * BytesToWriteExceedMax.java
 *
 * Purpose: wrapper for custom NFC handling exception
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class BytesToWriteExceedMax extends Exception
{
    public BytesToWriteExceedMax(String msg)
    {
        super(msg);
    }
}
