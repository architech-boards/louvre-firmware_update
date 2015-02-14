package it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc;

/**
 * ReaderDisconnectionException.java
 *
 * Purpose: wrapper for custom NFC handling exception
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class ReaderDisconnectionException extends Exception
{
    public ReaderDisconnectionException()
    {
        super("Reader failed to disconnect");
    }
}

