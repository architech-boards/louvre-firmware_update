package com.example.lorenzo.louvrefirmapp.NFCLogic.Exc;

/**
 * Created by Lorenzo on 09/11/2014.
 */
public class ReaderNotConnectedException extends Exception
{
    public ReaderNotConnectedException()
    {
        super("Reader not connected");
    }
}
