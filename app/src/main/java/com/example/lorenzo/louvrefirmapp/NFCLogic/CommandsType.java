package com.example.lorenzo.louvrefirmapp.NFCLogic;

/**
 * Created by Lorenzo on 31/10/2014.
 */
public enum CommandsType implements ByteEnum.Byte_enum
{
    READ((byte)0x30),
    WRITE((byte)0xA2);

    byte value;

    private CommandsType(byte value)
    {
        this.value = value;
    }

    public byte getValue()
    {
        return this.value;
    }
}
