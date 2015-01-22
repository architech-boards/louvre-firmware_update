package com.example.lorenzo.louvrefirmapp.NFCLogic.Exc;


public class ReaderDisconnectionException extends Exception
{
    public ReaderDisconnectionException()
    {
        super("Reader failed to disconnect");
    }
}

