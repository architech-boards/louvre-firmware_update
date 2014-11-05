package com.example.lorenzo.louvrefirmapp.NFCLogic;

import android.nfc.Tag;
import android.nfc.tech.NfcA;

import java.io.IOException;
import java.nio.channels.NotYetConnectedException;

/**
 * Created by Lorenzo on 31/10/2014.
 */
public class Reader
{
    NfcA nfcA;
    byte[] command;
    byte[] answer;

    /**
     * Create an instance of Reader object used to handle read and write operation on the specified
     * tag
     * @param tag Tag object that has to be read or written
     */
    public Reader(Tag tag)
    {
        this.nfcA = NfcA.get(tag);
    }

    /**
     * Connect the Reader to allow IO operation on the tag associated to the Reader
     * @return True if connected, otherwise false
     */
    public boolean connect()
    {
        try
        {
            this.nfcA.connect();
            return true;
        }
        catch (IOException ioExc)
        {
            return false;
        }
    }


    /**
     * Read the specified block address from the tag
     *
     * @param blockAddress Address of the block to read (from 0 to 254
     *                     )
     * @throws IOException
     * @throws java.lang.IndexOutOfBoundsException
     * @throws java.nio.channels.NotYetConnectedException
     */
    public byte[] read(byte blockAddress) throws IOException
    {
        if(blockAddress < 0 || blockAddress > 254)
        {
            throw new IndexOutOfBoundsException("Block address specified is out of range." +
                                                "Valid range is from 0 to 254");
        }

        if(this.nfcA.isConnected())
        {
            this.command = new byte[2];
            this.command[0] = CommandsType.READ.getValue();
            this.command[1] = blockAddress;
            this.answer = this.nfcA.transceive(this.command);

            return this.answer;
        }
        else
        {
            throw new NotYetConnectedException();
        }
    }


}
