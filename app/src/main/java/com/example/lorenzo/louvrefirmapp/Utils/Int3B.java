package com.example.lorenzo.louvrefirmapp.Utils;


//TODO check if value endianess is correct
public class Int3B
{
    int         intValue;
    byte []     value;


    public int getIntValue()
    {
        return intValue;
    }

    public byte[] getBytesArray()
    {
        return value;
    }


    public Int3B(int intValue)
    {
        this.intValue = intValue;

        this.value = new byte[3];
        this.value[0] = (byte) (intValue & (0XFF << 16));   // 3rd byte
        this.value[1] = (byte) (intValue & (0XFF << 8));    // 2nd byte
        this.value[2] = (byte) (intValue & (0XFF));         // 1st byte
    }
}