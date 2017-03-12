package net.aeronica.mods.mxtune.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class ModLogger {

    private static Logger logger;

    public static void setLogger(Logger logger) {
        if (ModLogger.logger != null) {
            throw new IllegalStateException("Attempt to replace logger");
        }
        ModLogger.logger = logger;
    }

    public static void log(Level level, String format, Object... data) {
        for(Object obj : data)
        {
            if (obj instanceof Exception)
            {
                for(StackTraceElement ste : ((Exception) obj).getStackTrace())
                {
                    logger.printf(level, format, ste); 
                }
            } else
                logger.printf(level, format, data);
        }
    }

    public static void info(String format, Object... data){
        log(Level.INFO, format, data);
    }
    
    public static void debug(String format, Object... data){
        log(Level.DEBUG, format, data);
    }
    
    public static void warning(String format, Object... data){
        log(Level.WARN, format, data);
    }

    public static void error(String format, Object... data){
        log(Level.ERROR, format, data);
    }

    public static void fatal(String format, Object... data){
        log(Level.FATAL, format, data);
    }

    public static void info(Object... data){
        log(Level.INFO, "%s", data);
    }
    
    public static void debug(Object... data){
        log(Level.DEBUG, "%s", data);
    }
    
    public static void warning(Object... data){
        log(Level.WARN, "%s", data);
    }

    public static void error(Object... data){
        log(Level.ERROR, "%s", data);
    }

    public static void fatal(Object... data){
        log(Level.FATAL, "%s", data);
    }
    
}
