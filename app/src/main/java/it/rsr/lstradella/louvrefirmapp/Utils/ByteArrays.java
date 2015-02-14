package it.rsr.lstradella.louvrefirmapp.Utils;

/**
 * ByteArrays.java
 *
 * Purpose: define functions to handle bytes arrays
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class ByteArrays
{
    static public byte[] concat(byte[] a, byte[] b)
    {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
}
