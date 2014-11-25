package com.example.lorenzo.louvrefirmapp.NFCLogic;

import com.example.lorenzo.louvrefirmapp.Utils.ByteEnum;

/**
 * Created by Lorenzo on 08/11/2014.
 */
public class Addresses
{
    public enum Sector implements ByteEnum
    {
        SECTOR_0((byte) 0x00),
        SECTOR_1((byte) 0x01),
        SECTOR_2((byte) 0x02),
        SECTOR_3((byte) 0x03);

        byte value;

        private Sector(byte value){ this.value = value;}

        public byte getValue() { return value; }
    }


    public enum Registers implements ByteEnum
    {
        SESSION((byte) 0xF8),
        CONFIGURATION((byte) 0xE8),
        SRAM_BEGIN((byte) 0xF0),
        USER_MEMORY_BEGIN((byte) 0x04),
        UID((byte) 0x00);

        byte value;

        private Registers(byte value) { this.value = value;}

        public byte getValue() {
            return value;
        }

    }


    public enum ConfigurationRegisters
    {
        NC_REG((byte) 0x00),
        LAST_NDEF_BLOCK((byte) 0x01),
        SRAM_MIRROR_BLOCK((byte) 0x02),
        WDT_LS((byte) 0x03),
        WDT_MS((byte) 0x04),
        I2C_CLOCK_STR((byte) 0x05),
        REG_LOCK((byte) 0x06),
        FIXED((byte) 0x07);

        byte value;

        private ConfigurationRegisters(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }


    public enum SessionRegisters
    {
        NC_REG((byte) 0x00),
        LAST_NDEF_BLOCK((byte) 0x01),
        SRAM_MIRROR_BLOCK((byte) 0x02),
        WDT_LS((byte) 0x03),
        WDT_MS((byte) 0x04),
        I2C_CLOCK_STR((byte) 0x05),
        NS_REG((byte) 0x06),
        FIXED((byte) 0x07);

        byte value;

        private SessionRegisters(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

}
