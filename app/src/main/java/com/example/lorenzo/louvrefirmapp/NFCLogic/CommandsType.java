package com.example.lorenzo.louvrefirmapp.NFCLogic;

import com.example.lorenzo.louvrefirmapp.Utils.ByteEnum;


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
