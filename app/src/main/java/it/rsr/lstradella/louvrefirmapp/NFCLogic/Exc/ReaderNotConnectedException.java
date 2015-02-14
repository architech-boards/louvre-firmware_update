package it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc;

/**
 * ReaderNotConnectedException.java
 *
 * Purpose: wrapper for custom NFC handling exception
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class ReaderNotConnectedException extends Exception
{
    public ReaderNotConnectedException()
    {
        super("Reader not connected");
    }
}
