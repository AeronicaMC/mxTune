package net.aeronica.mods.mxtune.util;

import net.aeronica.mods.mxtune.Reference;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class ModLogger
{
    private static final Marker MOD_MARKER = MarkerManager.getMarker(Reference.MOD_ID);
    private static Logger logger;

    public static void setLogger(Logger logger)
    {
        if (ModLogger.logger != null)
        {
            throw new IllegalStateException("Attempt to replace logger");
        }
        ModLogger.logger = logger;
    }

    public static void log(Level level, final Marker marker, String format, Object... data)
    {
        logger.printf(level, marker, format, data);
    }

    /**
     * Log a stack trace for &lt;T extends Throwable&gt; {@link Throwable} at the specified log {@link Level}
     *
     * @param level The specified log level
     * @param e     The raw exception
     */
    public static <T extends Throwable> void log(Level level, T e)
    {
        if (e != null)
        {
            log(level, MOD_MARKER, "%s", e.getLocalizedMessage());
            for (StackTraceElement s : e.getStackTrace())
                log(level, MOD_MARKER, "%s", s.toString());
        }
    }

    public static void info(String format, Object... data) {log(Level.INFO, MOD_MARKER, format, data);}

    public static void debug(String format, Object... data) {log(Level.DEBUG, MOD_MARKER, format, data);}

    public static void warn(String format, Object... data) {log(Level.WARN, MOD_MARKER, format, data);}

    public static void error(String format, Object... data) {log(Level.ERROR, MOD_MARKER, format, data);}

    public static void fatal(String format, Object... data) {log(Level.FATAL, MOD_MARKER, format, data);}

    /**
     * Log a stack trace for &lt;T extends Throwable&gt; {@link Throwable} at log Level.INFO
     *
     * @param e The raw exception
     */
    public static <T extends Throwable> void info(T e) {log(Level.INFO, e);}

    /**
     * Log a stack trace for &lt;T extends Throwable&gt; {@link Throwable} at log Level.DEBUG
     *
     * @param e The raw exception
     */
    public static <T extends Throwable> void debug(T e) {log(Level.DEBUG, e);}

    /**
     * Log a stack trace for &lt;T extends Throwable&gt; {@link Throwable} at log Level.WARN
     *
     * @param e The raw exception
     */
    public static <T extends Throwable> void warn(T e) {log(Level.WARN, e);}

    /**
     * Log a stack trace for &lt;T extends Throwable&gt; {@link Throwable} at log Level.ERROR
     *
     * @param e The raw exception
     */
    public static <T extends Throwable> void error(T e) {log(Level.ERROR, e);}

    /**
     * Log a stack trace for &lt;T extends Throwable&gt; {@link Throwable} at log Level.FATAL
     *
     * @param e The raw exception
     */
    public static <T extends Throwable> void fatal(T e) {log(Level.FATAL, e);}
}

