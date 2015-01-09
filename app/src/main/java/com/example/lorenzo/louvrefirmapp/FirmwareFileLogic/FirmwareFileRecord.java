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
    Int3B       address;    // 3 bytes
    byte        length;
    UnsByte[]   data;       // unsigned byte
    byte        crc;


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
        calculateCrc();
    }


    private void calculateLength()
    {
        this.length = (byte)data.length;
    }


    private void calculateCrc()
    {
        //TODO inserire logica di calcolo CRC
    }

    public byte[] toBytesArray()
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
