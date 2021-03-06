package it.rsr.lstradella.louvrefirmapp.NFCLogic;

import android.nfc.FormatException;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.util.Log;

import it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.BytesToWriteExceedMax;
import it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException;
import it.rsr.lstradella.louvrefirmapp.Utils.ByteArrays;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Reader.java
 *
 * Purpose: define the communication logic using NTAG functionality (see Louvre board specs)
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class Reader
{
    /**
     * Callbacks interface that activity wanting to track writing progress must implement.
     */
    public static interface WritingReportProgressCallbacks
    {
        void onSramBufferWrote(int currentSession, int sessions);
        void onSramBufferWait();
    }

    WritingReportProgressCallbacks writingReportProgressCallbacks;


    NfcA                nfcA;
    byte[]              command;
    byte[]              answer;
    Addresses.Sector    currentSector;
    static final int    SRAM_BUFFER_SIZE = 64;


    /**
     * Get answer related to the last transceiver operation
     * @return byte of the answer
     */
    public byte[] getAnswer()
    {
        return answer;
    }


    /**
     * Get answer related to the last transceiver operation converted in String
     * @return byte of the answer converted in String
     */
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
    public Reader(Tag tag, WritingReportProgressCallbacks wrCallback)
    {
        this.nfcA = NfcA.get(tag);
        this.currentSector = Addresses.Sector.SECTOR_0;
        this.writingReportProgressCallbacks = wrCallback;
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
     * Reader transceive function with check of connection status
     * @param bytes bytes to send
     * @return bytes received in response of the sent bytes
     * @throws it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException
     * @throws IOException
     */
    private byte[] transceive(byte[] bytes) throws ReaderNotConnectedException, IOException
    {
        if(!this.nfcA.isConnected())
        {
            throw new ReaderNotConnectedException();
        }

        return this.nfcA.transceive(bytes);
    }


    /**
     * Read the specified block address from the tag
     * @param blockAddress Address of the block to read (from 0 - FE)
     * @return 4 memory page of 16 byte each if no error occurred
     * @throws IOException
     * @throws it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException
     */
    public byte[] read(byte blockAddress) throws IOException, ReaderNotConnectedException
    {
        // The READ command requires a start page address, and returns the 16 bytes of four
        // NTAG I2C pages. For example, if address is 03h, then pages 03h, 04h, 05h, 06h are
        // returned.
        this.command = new byte[2];
        this.command[0] = CommandsType.READ.getValue();
        this.command[1] = blockAddress;
        this.answer = this.transceive(this.command);

        return this.answer;
    }


    /**
     * Writes specified bytes starting at the specified block address
     * N.B: it can write maximum 4 bytes (one memory page)
     * @param bytes bytes to write
     * @param blockAddress address of the block where writing operation begins
     * @throws IOException
     * @throws it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.BytesToWriteExceedMax
     */
    public void write(byte[] bytes, byte blockAddress) throws IOException, BytesToWriteExceedMax
    {
        // The WRITE command requires a block address, and writes 4 bytes of data into the
        // addressed NTAG I2C page.

        // Validate bytes dimension
        if(bytes.length > 4)
        {
            throw new BytesToWriteExceedMax("Tag accepts only 4 byte at the time to be written");
        }

        // Update answer with no answer reply
        answer = new byte[0];

        command = new byte[6];
        command[0] = (byte) 0xA2;
        command[1] = blockAddress;
        command[2] = bytes[0];
        command[3] = bytes[1];
        command[4] = bytes[2];
        command[5] = bytes[3];
        nfcA.transceive(command);
    }


    /**
     * Select the specified memory sector of the tag
     * @param sector sector to select
     * @throws IOException
     * @throws ReaderNotConnectedException
     */
    private void selectSector(Addresses.Sector sector) throws IOException, ReaderNotConnectedException
    {
        // If reader already in the specified sector do nothing
        if(this.currentSector == sector)
        {
            return;
        }

        // Update answer with no answer reply
        answer = new byte[0];

        // The SECTOR SELECT command consists of two commands packet: the first one is the
        // SECTOR SELECT command (C2h), FFh and CRC.
        command = new byte[2];
        command[0] = (byte) 0xc2;
        command[1] = (byte) 0xff;

        transceive(command);

        // Upon an ACK answer from the Tag,
        // the second command packet needs to be issued with the related sector address to be
        // accessed and 3 bytes RFU.
        command = new byte[4];
        command[0] = sector.getValue();
        command[1] = (byte) 0x00;
        command[2] = (byte) 0x00;
        command[3] = (byte) 0x00;

        // To successfully access to the requested memory sector, the tag shall issue a passive
        // ACK, which is sending NO REPLY for more than 1ms
        this.nfcA.setTimeout(1);

        try
        {
            transceive(command);
        }
        catch (IOException e)
        {
            // Catch exception to handle passive ACK
        }

        nfcA.setTimeout(600);
        this.currentSector = sector;
    }


    /**
     * Write specified bytes to the SRAM buffer
     * N.B: maximum # of bytes allowed is 64 (16 memory pages)
     * @param bytes bytes to write
     * @throws IOException
     * @throws android.nfc.FormatException
     * @throws it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.BytesToWriteExceedMax
     * @throws it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException
     */
    public void writeSRAM(byte[] bytes) throws IOException, FormatException, BytesToWriteExceedMax,
                                               ReaderNotConnectedException
    {
        byte[]      bytesBlock;
        int         sessions;

        // Alert that the writing operation is waiting for the board
        writingReportProgressCallbacks.onSramBufferWait();

        // Wait for PTHRU_ON_OFF
        while(get_NC_REG_sessField(Masks.NC_REG_Sess.PTHRU_ON_OFF) == 0) { }

        // Wait for I2C_LOCKED
        while(get_NS_REG_sessField(Masks.NS_REG_Sess.I2C_LOCKED) == 1) { }

        // Handle multiple writing session
        sessions =  (int)Math.ceil((double)bytes.length / (double)SRAM_BUFFER_SIZE);
        for(int currentSession = 0; currentSession < sessions; currentSession ++)
        {
            int startBlock = currentSession * SRAM_BUFFER_SIZE;
            int endBlock = startBlock + SRAM_BUFFER_SIZE;
            if(endBlock > bytes.length)
            {
                bytesBlock = Arrays.copyOfRange(bytes, startBlock, bytes.length);
            }
            else
            {
                bytesBlock = Arrays.copyOfRange(bytes, startBlock, endBlock);
            }

            // Divide the bytes to write into 4 blocks of 4 bytes each because the write function can
            // handle only 4 bytes at the time
            byte[] pageBuffer = new byte[4];
            int bytesToWriteIndex = 0;
            for (int i = 0; i < 16; i++)
            {
                for (int j = 0; j < 4; j++)
                {
                    if (bytesToWriteIndex < bytesBlock.length)
                    {
                        pageBuffer[j] = bytesBlock[bytesToWriteIndex];
                        bytesToWriteIndex++;
                    }
                    else
                    {
                        pageBuffer[j] = (byte) 0x00;
                    }
                }

                // Select sector 1 where SRAM buffer is mapped
                selectSector(Addresses.Sector.SECTOR_1);
                byte blockAddress = (byte)(Addresses.Registers.SRAM_BEGIN.getValue() + i);
                write(pageBuffer, blockAddress);
            }

            // Wait for I2C to read the data on SRAM buffer
            Log.d("Write SRAM", "Start waiting for RF_LOCKED set to 1 ...");
            while(get_NS_REG_sessField(Masks.NS_REG_Sess.RF_LOCKED) == 0) { }
            Log.d("Write SRAM", "[Session " + (currentSession + 1) + " of " + sessions +
                    "] SRAM buffer written successfully");

            // Alert about the progress in the writing operation
            writingReportProgressCallbacks.onSramBufferWrote(currentSession, sessions);
        }
        Log.d("Write SRAM", "All data written to SRAM successfully");
    }


    /**
     * Write specified bytes list to the SRAM buffer
     * N.B: maximum # of bytes allowed is 64 (16 memory pages)
     * @param bytesList bytes list to write
     * @throws IOException
     * @throws android.nfc.FormatException
     * * @throws it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.BytesToWriteExceedMax
     * @throws it.rsr.lstradella.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException
     */
    public void writeSRAMList(List<byte[]> bytesList) throws IOException, FormatException,
            BytesToWriteExceedMax, ReaderNotConnectedException
    {
        // Alert that the writing operation is waiting for the board
        writingReportProgressCallbacks.onSramBufferWait();

        // Wait for PTHRU_ON_OFF
        while(get_NC_REG_sessField(Masks.NC_REG_Sess.PTHRU_ON_OFF) == 0) { }

        // Wait for I2C_LOCKED
        while(get_NS_REG_sessField(Masks.NS_REG_Sess.I2C_LOCKED) == 1) { }

        // Handle multiple writing session
        for(byte[] bytes : bytesList)
        {
            // Divide the bytes to write into 4 blocks of 4 bytes each because the write function can
            // handle only 4 bytes at the time
            byte[] pageBuffer = new byte[4];
            int bytesToWriteIndex = 0;
            for (int i = 0; i < 16; i++)
            {
                for (int j = 0; j < 4; j++)
                {
                    if (bytesToWriteIndex < bytes.length)
                    {
                        pageBuffer[j] = bytes[bytesToWriteIndex];
                        bytesToWriteIndex++;
                    }
                    else
                    {
                        pageBuffer[j] = (byte) 0x00;
                    }
                }
                // Select sector 1 where SRAM buffer is mapped
                selectSector(Addresses.Sector.SECTOR_1);
                byte blockAddress = (byte)(Addresses.Registers.SRAM_BEGIN.getValue() + i);
                write(pageBuffer, blockAddress);
            }

            // Wait for I2C to read the data on SRAM buffer
            Log.d("Write SRAM", "Start waiting for RF_LOCKED set to 1 ...");

            while (get_NS_REG_sessField(Masks.NS_REG_Sess.RF_LOCKED) == 0) { }
            Log.d("Write SRAM", "[Session " + (bytesList.indexOf(bytes) + 1) + " of " + bytesList.size() +
                    "] SRAM buffer written successfully");

            // Alert about the progress in the writing operation
            writingReportProgressCallbacks.onSramBufferWrote((bytesList.indexOf(bytes) + 1), bytesList.size());
        }
        Log.d("Write SRAM", "All data written to SRAM successfully");
    }


    /**
     * Read the SRAM buffer as many times as specified by block param
     * @param blocks Specify how many times to read the buffer
     * @return bytes read from the buffer
     * @throws IOException
     * @throws ReaderNotConnectedException
     */
    public byte[] readSRAM(int blocks) throws IOException, ReaderNotConnectedException
    {
        byte[] fullAnswer = new byte[0];

        for(int i = 0; i < blocks; i++)
        {
            // Wait I2C to write last SRAM page
            Log.d("readSRAM", "Start waiting for SRAM_RF_READY set to 1 ...");
            while (get_NS_REG_sessField(Masks.NS_REG_Sess.SRAM_RF_READY) == 0) { }

            // Read 16 byte from SRAM buffer
            selectSector(Addresses.Sector.SECTOR_1);

            Log.d("readSRAM", "Start reading SRAM block " + i + " of " + blocks);
            fullAnswer = ByteArrays.concat(fullAnswer, fast_read((byte)0xF0, (byte)0xFF));
            Log.d("readSRAM", "SRAM bock " + i + " read successfully");
        }

        return fullAnswer;
    }


    /**
     * Read from startAddr to endAddr
     * @param startAddr Address from where to start reading
     * @param endAddr Address where to finish reading
     * @return Bytes read
     * @throws IOException
     */
    public byte[] fast_read(byte startAddr, byte endAddr) throws IOException
    {
        // Update answer with no answer reply
        answer = new byte[0];

        command = new byte[3];
        command[0] = (byte) 0x3A;
        command[1] = startAddr;
        command[2] = endAddr;

        answer = nfcA.transceive(command);
        return answer;
    }


    /**
     * Get the specified configuration register
     * @param configurationReg configuration register to retrieve
     * @return configuration register byte requested
     * @throws IOException
     * @throws ReaderNotConnectedException
     */
    public byte getConfigurationReg(Addresses.ConfigurationRegisters configurationReg) throws IOException,
            ReaderNotConnectedException
    {
        selectSector(Addresses.Sector.SECTOR_1);

        return read(Addresses.Registers.CONFIGURATION.getValue())[configurationReg.getValue()];
    }


    /**
     * Get the specified session register
     * @param sessionReg session register to retrieve
     * @return session register byte requested
     * @throws IOException
     * @throws ReaderNotConnectedException
     */
    public byte getSessionReg(Addresses.SessionRegisters sessionReg) throws IOException,
                                                                        ReaderNotConnectedException
    {
        selectSector(Addresses.Sector.SECTOR_3);

        return read(Addresses.Registers.SESSION.getValue())[sessionReg.getValue()];
    }


    /**
     * Get the specified field of the Configuration NC_REG
     * @param field field to retrieve
     * @return requested field value
     * @throws IOException
     * @throws ReaderNotConnectedException
     */
    public byte get_NC_REG_confField(Masks.NC_REG_Conf field) throws IOException, ReaderNotConnectedException
    {
        byte regValue = getConfigurationReg(Addresses.ConfigurationRegisters.NC_REG);

        return (byte)((regValue & field.getValue()) >> field.getShift());
    }


    /**
     * Get the specified field of the Session NC_REG
     * @param field field to retrieve
     * @return requested field value
     * @throws IOException
     * @throws ReaderNotConnectedException
     */
    public byte get_NC_REG_sessField(Masks.NC_REG_Sess field) throws IOException, ReaderNotConnectedException
    {
        byte regValue = getSessionReg(Addresses.SessionRegisters.NC_REG);

        return (byte)((regValue & field.getValue()) >> field.getShift());
    }


    /**
     * Get the specified field of the register Session NS_REG
     * @param field field to retrieve
     * @return requested field value
     * @throws IOException
     * @throws ReaderNotConnectedException
     */
    public byte get_NS_REG_sessField(Masks.NS_REG_Sess field) throws IOException, ReaderNotConnectedException
    {
        byte regValue = getSessionReg(Addresses.SessionRegisters.NS_REG);

        return (byte)((regValue & field.getValue()) >> field.getShift());
    }

}
