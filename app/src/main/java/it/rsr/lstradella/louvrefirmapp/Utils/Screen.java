package it.rsr.lstradella.louvrefirmapp.Utils;

import android.os.PowerManager;

/**
 * Screen.java
 *
 * Purpose: static class to handle screen timeout
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class Screen
{
    static protected PowerManager.WakeLock wakeLock;

    /**
     * Avoid the screen to turn off till a releaseScreenOn call
     * @param powerManager Power manager service
     */
    public static void keepScreenOn(Object powerManager)
    {
        // This code together with the one in onDestroy()
        // will make the screen be always on until this Activity gets destroyed.
        if(wakeLock == null)
        {
            PowerManager pm = (PowerManager)powerManager;
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        }
        wakeLock.acquire();
    }


    /**
     * Release the screen on blocking let the screen to turn off after this call
     */
    public static void releaseScreenOn()
    {
        if(wakeLock != null)
        {
            if(wakeLock.isHeld())
            {
                wakeLock.release();
            }
        }
    }
}
