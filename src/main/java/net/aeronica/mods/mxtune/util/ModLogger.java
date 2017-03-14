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
    
    /**
     * Log a stack trace for &lt;T extends Exception&gt; {@link e} at the specified log {@link level}
     * @param level     The specified log level
     * @param e         The raw exception 
     */
    public static <T extends Exception> void log(Level level, T e)
    {
        if (e != null) {
            log(level, "%s", e.getLocalizedMessage());
            for(StackTraceElement s: e.getStackTrace())
                log(level, "%s", s.toString());
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

    /**
     * Log a stack trace for &lt;T extends Exception&gt; {@link e} at log Level.INFO
     * @param e         The raw exception 
     */
    public static <T extends Exception> void info(T e)
    {
        log(Level.INFO, e);
    }
    
    /**
     * Log a stack trace for &lt;T extends Exception&gt; {@link e} at log Level.DEBUG
     * @param e         The raw exception 
     */
    public static <T extends Exception> void debug(T e)
    {
        log(Level.DEBUG, e);
    }
    
    /**
     * Log a stack trace for &lt;T extends Exception&gt; {@link e} at log Level.WARM
     * @param e         The raw exception 
     */
    public static <T extends Exception> void warning(T e)
    {
        log(Level.WARN, e);
    }
    
    /**
     * Log a stack trace for &lt;T extends Exception&gt; {@link e} at log Level.ERROR
     * @param e         The raw exception 
     */
    public static <T extends Exception> void error(T e)
    {
        log(Level.ERROR, e);
    }
    
    /**
     * Log a stack trace for &lt;T extends Exception&gt; {@link e} at log Level.FATAL
     * @param e         The raw exception 
     */
    public static <T extends Exception> void fatal(T e)
    {
        log(Level.FATAL, e);
    }
    
}
