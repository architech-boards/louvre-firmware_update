package com.example.lorenzo.louvrefirmapp.NFCLogic;

import android.nfc.FormatException;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.util.Log;
import android.widget.TextView;

import com.example.lorenzo.louvrefirmapp.NFCLogic.Exc.BytesToWriteExceedMax;
import com.example.lorenzo.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException;

import java.io.IOException;
import java.util.Arrays;


public class Reader
{
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
    public Reader(Tag tag)
    {
        this.nfcA = NfcA.get(tag);
        this.currentSector = Addresses.Sector.SECTOR_0;
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
     * @throws com.example.lorenzo.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException
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
     * @throws com.example.lorenzo.louvrefirmapp.NFCLogic.Exc.ReaderNotConnectedException
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
     * @throws com.example.lorenzo.louvrefirmapp.NFCLogic.Exc.BytesToWriteExceedMax
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
     * Write specified bytes to the SRAM buffer of the TAG.
     * N.B: maximum # of bytes allowed is 64 (16 memory pages)
     * @param bytes bytes to write
     * @throws IOException
     * @throws android.nfc.FormatException
     */
    public void writeSRAM(byte[] bytes, TextView textViewDebug) throws IOException, FormatException, BytesToWriteExceedMax,
                                               ReaderNotConnectedException
    {
        //TODO timeout to avoid indefinite while blocking
        byte[]      bytesBlock;
        int         sessions;

        // Wait for PTHRU_ON_OFF
        textViewDebug.setText("");
        textViewDebug.append("Start waiting for PTHRU_ON_OFF set to 1 ...\n");
        Log.d("Write SRAM", "Start waiting for PTHRU_ON_OFF set to 1 ...");
        while(get_NC_REG_sessField(Masks.NC_REG_Sess.PTHRU_ON_OFF) == 0)
        { }
        Log.d("Write SRAM", "Found PTHRU_ON_OFF set to 1 ...");
        textViewDebug.append("Found PTHRU_ON_OFF set to 1 ...\n");

        // Wait for I2C_LOCKED
        Log.d("Write SRAM", "Start waiting for I2C_LOCKED set to 0 ...");
        textViewDebug.append("Start waiting for I2C_LOCKED set to 0 ...\n");
        while(get_NS_REG_sessField(Masks.NS_REG_Sess.I2C_LOCKED) == 1)
        { }
        Log.d("Write SRAM", "Found I2C_LOCKED set to 0 ...");
        textViewDebug.append("Found I2C_LOCKED set to 0 ...\n");

        // Handle multiple writing session
        sessions =  (int)Math.ceil(bytes.length / SRAM_BUFFER_SIZE);
        for(int currentSession = 0; currentSession < sessions; currentSession ++)
        {
            // Select sector 1 where SRAM buffer is mapped
            selectSector(Addresses.Sector.SECTOR_1);
            Log.d("Write SRAM", "Memory sector 1 selected");
            textViewDebug.append("Memory sector 1 selected\n");

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

                byte blockAddress = (byte)(Addresses.Registers.SRAM_BEGIN.getValue() + i);
                Log.d("Write SRAM", "[Session " + (currentSession + 1) + " of " + sessions +
                        "] writing 4 bytes to address " + blockAddress  + "...");
                textViewDebug.append("[Session " + (currentSession + 1) + " of " + sessions +
                        "] writing 4 bytes to address " + blockAddress  + "...\n");
                write(pageBuffer, blockAddress);
                Log.d("Write SRAM", "Bytes written");
                textViewDebug.append("Bytes written\n");
            }

            // Wait for I2C to read the data on SRAM buffer
            Log.d("Write SRAM", "Start waiting for RF_LOCKED set to 1 ...");
            textViewDebug.append("Start waiting for RF_LOCKED set to 1 ...\n");
            while(get_NS_REG_sessField(Masks.NS_REG_Sess.RF_LOCKED) == 0)
            { }
            Log.d("Write SRAM", "[Session " + (currentSession + 1) + " of " + sessions +
                    "] SRAM buffer written successfully");
            textViewDebug.append("[Session " + (currentSession + 1) + " of " + sessions +
                    "] SRAM buffer written successfully\n");

            // Print all BIT interested in the communication
            /*
            textViewDebug.append("\n");
            textViewDebug.append("I2C_LOCKED= " + Byte.toString(get_NS_REG_sessField(Masks.NS_REG_Sess.I2C_LOCKED)) + "\n");
            textViewDebug.append("RF_LOCKED= " + Byte.toString(get_NS_REG_sessField(Masks.NS_REG_Sess.RF_LOCKED)) + "\n");
            textViewDebug.append("SRAM_I2C_LOCKED= " + Byte.toString(get_NS_REG_sessField(Masks.NS_REG_Sess.SRAM_I2C_READY)) + "\n");
            textViewDebug.append("SRAM_RF_READY= " + Byte.toString(get_NS_REG_sessField(Masks.NS_REG_Sess.SRAM_RF_READY)) + "\n");
            textViewDebug.append("EEPROM_WR_ERR= " + Byte.toString(get_NS_REG_sessField(Masks.NS_REG_Sess.EEPROM_WR_ERR)) + "\n");
            textViewDebug.append("EEPROM_WR_BUSY= " + Byte.toString(get_NS_REG_sessField(Masks.NS_REG_Sess.EEPROM_WR_BUSY)) + "\n");
            textViewDebug.append("RF_FIELD_PRESENT= " + Byte.toString(get_NS_REG_sessField(Masks.NS_REG_Sess.RF_FIELD_PRESENT)) + "\n");
            textViewDebug.append("I2C_RST_ON_OFF= " + Byte.toString(get_NC_REG_sessField(Masks.NC_REG_Sess.I2C_RST_ON_OFF)) + "\n");
            textViewDebug.append("PTHRU_DIR= " + Byte.toString(get_NC_REG_sessField(Masks.NC_REG_Sess.PTHRU_ON_OFF)) + "\n");
            textViewDebug.append("FD_ON= " + Byte.toString(get_NC_REG_sessField(Masks.NC_REG_Sess.FD_ON)) + "\n");
            textViewDebug.append("FD_OFF=" + Byte.toString(get_NC_REG_sessField(Masks.NC_REG_Sess.FD_OFF)) + "\n");
            textViewDebug.append("SRAM_MIRROR_ON_OFF= " + Byte.toString(get_NC_REG_sessField(Masks.NC_REG_Sess.SRAM_MIRROR_ON_OFF)) + "\n");
            textViewDebug.append("PTHRU_DIR= " + Byte.toString(get_NC_REG_sessField(Masks.NC_REG_Sess.PTHRU_DIR)) + "\n");
            */
        }

        Log.d("Write SRAM", "All data written to SRAM successfully");
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


    /**
     * @throws IOException
     * @throws ReaderNotConnectedException
     */
    public void setBit() throws IOException,
            ReaderNotConnectedException
    {
        /*
        // Set the bit
        digit |= 1 << bit_to_set_pos;

        // Unset the bit.
        digit &= ~(1 << bit_to_set_pos);
        */
    }

}
