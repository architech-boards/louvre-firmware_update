package it.rsr.lstradella.louvrefirmapp.FirmwareFileLogic.Exc;

/**
 * FirmwareRecCreationExc.java
 *
 * Purpose: wrapper for custom .hex file handling exception
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class FirmwareRecCreationExc extends Exception
{
    public FirmwareRecCreationExc(String msg)
    {
        super(msg);
    }

    public FirmwareRecCreationExc(String msg, Throwable innerExc)
    {
        super(msg, innerExc);
    }
}
