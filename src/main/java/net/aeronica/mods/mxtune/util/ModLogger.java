/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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

    private ModLogger() { /* NOP */ }

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
        log(level, MOD_MARKER, "%s", e.getLocalizedMessage());
        for (StackTraceElement s : e.getStackTrace())
            log(level, MOD_MARKER, "%s", s.toString());
    }

    public static void info(String format, Object... data) {log(Level.INFO, MOD_MARKER, format, data);}

    public static void debug(String format, Object... data)
    {
        if (Util.inDev())
            log(Level.INFO, MOD_MARKER, format, data);
        else
            log(Level.DEBUG, MOD_MARKER, format, data);
    }

    public static void warn(String format, Object... data) {log(Level.WARN, MOD_MARKER, format, data);}

    public static void error(String format, Object... data) {log(Level.ERROR, MOD_MARKER, format, data);}

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
    public static <T extends Throwable> void debug(T e)
    {
        if (Util.inDev())
            log(Level.INFO, e);
        else
            log(Level.DEBUG, e);
    }

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

}

