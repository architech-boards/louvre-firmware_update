package it.rsr.lstradella.louvrefirmapp.Utils;

/**
 * Int3B.java
 *
 * Purpose: structure of integer 3 bytes dimension
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
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
        this.value[0] = (byte) ((intValue >> 16) & 0XFF);   // 3rd byte
        this.value[1] = (byte) ((intValue >> 8) & 0XFF);    // 2nd byte
        this.value[2] = (byte) (intValue & (0XFF));         // 1st byte
    }
}