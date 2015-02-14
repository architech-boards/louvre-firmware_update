package it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic;

import it.rsr.lstradella.louvrefirmapp.Utils.ByteEnum;
import it.rsr.lstradella.louvrefirmapp.Utils.Int3B;
import it.rsr.lstradella.louvrefirmapp.Utils.UnsByte;

/**
 * FirmwareFileRecord.java
 *
 * Purpose: define the structure of a bootloader record (see application specs) and provide some
 * logic to handle the data.
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
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


    /**
     * Get the pre-calculated recordBytes field
     * @return recordBytes
     */
    public byte[] getRecordBytes()
    {
        return recordBytes;
    }


    /**
     * Create a new FirmwareFileRecord obj auto-calculating the fields CRC, length and recordBytes
     * according to the specified parameters
     * @param command command code
     * @param address staring memory address value
     * @param data valid data starting from specified address
     */
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
     * Modify the command field and update the bytes array accordingly to specify this record as the
     * last one.
     */
    public void setAsLastRecord()
    {
        this.command = CommandsType.LAST_BOOTLOADER_RECORD.getValue();
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
