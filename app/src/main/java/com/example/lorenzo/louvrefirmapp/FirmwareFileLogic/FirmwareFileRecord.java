package com.example.lorenzo.louvrefirmapp.FirmwareFileLogic;


import com.example.lorenzo.louvrefirmapp.Utils.ByteEnum;
import com.example.lorenzo.louvrefirmapp.Utils.Int3B;
import com.example.lorenzo.louvrefirmapp.Utils.UnsByte;


public class FirmwareFileRecord
{
    public enum CommandsType implements ByteEnum.Byte_enum
    {
        BOOTLOADER_RECORD((byte) 0x02),
        LAST_BOOTLOADER_RECORD((byte) 0x04),
        GET_FIRMWARE_REVISION((byte) 0x01);

        byte value;

        private CommandsType(byte value){ this.value = value;}

        public byte getValue() { return value; }
    }


    byte        command;
    Int3B       address;        // 3 bytes
    byte        length;
    UnsByte[]   data;           // unsigned byte
    byte        crc;

    byte[]      recordBytes;    // bytes array representation of the record


    public byte[] getRecordBytes()
    {
        return recordBytes;
    }


    public FirmwareFileRecord(CommandsType command, Int3B address, short[] data)
    {
        this.command = command.getValue();
        this.address = address;

        // Save data array converted to Unsigned byte
        this.data = new UnsByte[data.length];
        for(int i = 0; i < data.length; i++)
        {
            this.data[i] = new UnsByte(data[i]);
        }

        calculateLength();
        calculateCrcAndRecordBytes();
    }


    /**
     * Calculate and save the data length of the record
     */
    private void calculateLength()
    {
        this.length = (byte)data.length;
    }


    /**
     * Calculate CRC value based on the Intel .hex specification:
     * two's complement of LSB of the sum of all decoded byte values.
     * Update recordBytes array with calculated crc value
     */
    private void calculateCrcAndRecordBytes()
    {
        this.recordBytes = toBytesArray();

        int sum = 0;
        for(int i = 0; i < recordBytes.length - 1; i++) // not consider final value (crc field)
        {
            sum += recordBytes[i];
        }

        // Update crc field value
        short sumLSB = (short) (sum & 0xFF);
        this.crc = (byte)(((~sumLSB) + 1) & 0xFF);

        // Update recordBytes array with calculated crc value
        this.recordBytes[recordBytes.length-1] = this.crc;
    }


    /**
     * Return the record converted into a byte array
     * @return Record to byte array
     */
    private byte[] toBytesArray()
    {
        byte[] bytesArray = new byte[data.length + 6];

        // Command
        bytesArray[0] = this.command;

        // Address
        int i = 1;
        for(byte b : this.address.getBytesArray())
        {
            bytesArray[i] = b;
            i++;
        }

        // Length
        bytesArray[4] = this.length;

        // Data
        i = 5;
        for (UnsByte s : this.data)
        {
            bytesArray[i++] = s.getByteValue();
        }

        bytesArray[i] = this.crc;

        return bytesArray;
    }
}
