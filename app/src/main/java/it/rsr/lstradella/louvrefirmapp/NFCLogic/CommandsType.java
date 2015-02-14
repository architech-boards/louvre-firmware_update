package it.rsr.lstradella.louvrefirmapp.NFCLogic;

import it.rsr.lstradella.louvrefirmapp.Utils.ByteEnum;

/**
 * CommandsType.java
 *
 * Purpose: define the values of some NTAG command value (see Louvre board specs)
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
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
