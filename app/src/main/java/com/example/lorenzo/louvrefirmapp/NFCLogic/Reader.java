package com.example.lorenzo.louvrefirmapp.NFCLogic;

import android.nfc.Tag;
import android.nfc.tech.NfcA;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.NotYetConnectedException;

/**
 * Created by Lorenzo on 31/10/2014.
 */
public class Reader implements Serializable
{
    NfcA nfcA;
    byte[] command;
    byte[] answer;

    public byte[] getAnswer()
    {
        return answer;
    }

    public String getAnswerString()
    {
        String res = "";
        for(byte b:this.answer)
        {
            res += String.format("0x%02X", b);
        }
        return res;
    }

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
     * Close the connection to the tag disabling all I/O operations and releasing resources
     * @return True if disconnected, otherwise false
     */
    public boolean disconnect()
    {
        try
        {
            this.nfcA.close();
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
     * @param blockAddress Address of the block to read (from 0 - FE)
     *
     * @throws IOException
     * @throws java.lang.IndexOutOfBoundsException
     * @throws java.nio.channels.NotYetConnectedException
     */
    public byte[] read(byte blockAddress) throws IOException//TODO controllare se validazione OK
    {
        String errorBlockRange = "Specified block address is out of range. Valid range is 0 - FE";

        if(blockAddress < Byte.MIN_VALUE || blockAddress > Byte.MAX_VALUE)
        {
            throw new IndexOutOfBoundsException(errorBlockRange);
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
