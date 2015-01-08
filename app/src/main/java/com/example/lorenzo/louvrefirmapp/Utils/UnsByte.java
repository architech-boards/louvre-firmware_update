package com.example.lorenzo.louvrefirmapp.Utils;


public class UnsByte
{
    short       shortValue;
    byte        value;


    public short getShortValue()
    {
        return shortValue;
    }

    public byte getByteValue()
    {
        return value;
    }


    public UnsByte(short shortValue)
    {
        this.shortValue = shortValue;

        this.value = (byte) (shortValue & (0XFF << 8) | (shortValue & (0XFF)));
    }
}
