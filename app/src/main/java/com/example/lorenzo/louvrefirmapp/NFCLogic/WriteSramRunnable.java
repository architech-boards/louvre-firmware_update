package com.example.lorenzo.louvrefirmapp.NFCLogic;

import com.example.lorenzo.louvrefirmapp.NFCLogic.Exc.ReaderDisconnectionException;
import com.example.lorenzo.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException;

public class WriteSramRunnable implements Runnable
{
    public static interface WritingSramCallback
    {
        void onWritingError(Exception exc);
        void onSuccessfullyWritten();
    }

    Reader                      reader;
    byte[]                      bytesToWrite;
    WritingSramCallback         writingSramCallback;

    public WriteSramRunnable(Reader reader, byte[] bytesToWrite, WritingSramCallback callback)
    {
        this.reader = reader;
        this.bytesToWrite = bytesToWrite;
        this.writingSramCallback = callback;
    }


    @Override
    public void run()
    {
        try
        {
            // Check if a tag was scanned
            if(reader == null)
            {
                writingSramCallback.onWritingError(new ReaderNotConnectedException());
                return;
            }

            // Try to open a connection to the tag
            if(!reader.connect())
            {
                writingSramCallback.onWritingError(new ReaderNotConnectedException());
            }

            reader.writeSRAM(this.bytesToWrite);
            writingSramCallback.onSuccessfullyWritten();

            if(!reader.disconnect())
            {
                writingSramCallback.onWritingError(new ReaderDisconnectionException());
            }

        } catch (Exception e)
        {
            writingSramCallback.onWritingError(e);
        }
    }
}
