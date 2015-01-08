package com.example.lorenzo.louvrefirmapp.NFCLogic.Exc;


public class ReaderNotConnectedException extends Exception
{
    public ReaderNotConnectedException()
    {
        super("Reader not connected");
    }
}
