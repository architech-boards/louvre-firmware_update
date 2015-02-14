package it.rsr.lstradella.louvrefirmapp.Utils;

/**
 * UnsByte.java
 *
 * Purpose: structure of unsigned byte
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
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
