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
        logger.printf(level, format, data);
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

    public static <T extends Exception> void error(T e)
    {
        logger.error(e.getStackTrace());
    }
}
