package com.example.lorenzo.louvrefirmapp.NFCLogic;

import com.example.lorenzo.louvrefirmapp.Utils.ByteEnum;

/**
 * Created by Lorenzo on 08/11/2014.
 */
public class Masks
{
    public enum NC_REG_Conf implements ByteEnum
    {
        I2C_RST_ON_OFF((byte)0x01, (byte)7),
        FD_OFF((byte)0x03, (byte)4),
        FD_ON((byte)0x03, (byte)2),
        TRANSFER_DIR((byte)0x01, (byte)0);

        byte value;
        byte shift;

        private NC_REG_Conf(byte value, byte shift)
        {
            this.shift = shift;
            this.value = (byte)(value << shift);
        }

        public byte getValue()
        {
            return this.value;
        }

        public byte getShift()
        {
            return this.shift;
        }
    }


    public enum NC_REG_Sess implements ByteEnum
    {
        I2C_RST_ON_OFF((byte)0x01, (byte) 7),
        PTHRU_ON_OFF((byte)0x01, (byte)6),
        FD_OFF((byte)0x03, (byte)4),
        FD_ON((byte)0x03, (byte)2),
        SRAM_MIRROR_ON_OFF((byte)0x01, (byte)1),
        PTHRU_DIR((byte)0x01, (byte)0);

        byte value;
        byte shift;

        private NC_REG_Sess(byte value, byte shift)
        {
            this.shift = shift;
            this.value = (byte)(value << shift);
        }

        public byte getValue()
        {
            return value;
        }

        public byte getShift()
        {
            return this.shift;
        }
    }


    public enum NS_REG_Sess implements ByteEnum
    {
        NDEF_DATA_READ((byte) 0x01, (byte) 7),
        I2C_LOCKED((byte)0x01, (byte)6),
        RF_LOCKED((byte)0x01 ,(byte)5),
        SRAM_I2C_READY((byte)0x01, (byte)4),
        SRAM_RF_READY((byte)0x01, (byte)3),
        EEPROM_WR_ERR((byte)0x01,(byte)2),
        EEPROM_WR_BUSY((byte)0x01,(byte)1),
        RF_FIELD_PRESENT((byte)0x01, (byte)0);

        byte value;
        byte shift;

        private NS_REG_Sess(byte value, byte shift)
        {
            this.shift = shift;
            this.value = (byte)(value << shift);
        }

        public byte getValue()
        {
            return value;
        }

        public byte getShift()
        {
            return this.shift;
        }
    }
}
