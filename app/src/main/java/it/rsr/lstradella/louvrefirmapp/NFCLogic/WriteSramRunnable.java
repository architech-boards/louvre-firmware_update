package it.rsr.lstradella.louvrefirmapp.NFCLogic;

import android.util.Log;

import it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.ReaderDisconnectionException;
import it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException;

import java.util.List;

/**
 * WriteSramRunnable.java
 *
 * Purpose: define the logic to handle NFC communication in background and report progress to the
 * main UI thread safely
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class WriteSramRunnable implements Runnable
{
    public static interface WritingSramCallback
    {
        void onWritingError(Exception exc);
        void onSuccessfullyWritten();
    }

    Reader                      reader;
    List<byte[]>                bytesToWrite;
    WritingSramCallback         writingSramCallback;

    public WriteSramRunnable(Reader reader, List<byte[]> bytesToWrite, WritingSramCallback callback)
    {
        this.reader = reader;
        this.bytesToWrite = bytesToWrite;
        this.writingSramCallback = callback;
    }


    @Override
    public void run()
    {
        String transceiveErr = "Transceive done with ERROR";

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

            reader.writeSRAMList(this.bytesToWrite);

            Log.d("Write SRAM", "Start waiting for PTHRU_DIR set to 0 ...");
            while(reader.get_NC_REG_sessField(Masks.NC_REG_Sess.PTHRU_DIR)==1) { }

            byte[] res = reader.readSRAM(1); // Read one block of 64 byte for board reply
            if(res.length > 0)
            {
                if(res[0] == 0x00) // Full frame of 0x00 for transceive OK
                {
                    writingSramCallback.onSuccessfullyWritten();
                }
                else
                {
                    writingSramCallback.onWritingError(new Exception(transceiveErr));
                }
            }
            else
            {
                writingSramCallback.onWritingError(new Exception(transceiveErr));
            }

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
