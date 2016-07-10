package net.aeronica.mods.mxtune.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.aeronica.mods.mxtune.MXTuneMain;

/** TODO: This need to be reviewed - Use Forge's logger? */
public class ModLogger
{

    private static Logger modLogger;

    public static void initializeLogging()
    {
        modLogger = LogManager.getLogger(MXTuneMain.MODID);
    }

    public static void logInfo(String msg)
    {
        modLogger.log(Level.INFO, msg);
    }

    public static void logWarning(String msg)
    {
        modLogger.log(Level.WARN, msg);
    }

    public static void logError(String msg)
    {
        modLogger.log(Level.ERROR, msg);
    }

    public static void debug(String msg)
    {
        modLogger.log(Level.DEBUG, msg);
    }
}
