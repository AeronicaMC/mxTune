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

package net.aeronica.mods.mxtune.groups;

import net.aeronica.mods.mxtune.util.ModLogger;

import java.util.Timer;
import java.util.TimerTask;

import static net.aeronica.mods.mxtune.util.SheetMusicUtil.formatDuration;

/**
 * Schedule removal of the playID after a specified duration in seconds.
 * This is a simple implementation with no management options, such as removing tasks.
 */
public class DurationTimer
{
    private static Timer timer;
    private DurationTimer() { /* NOP */ }

    public static void start()
    {
        timer = new Timer("Timer");
    }

    public static void shutdown()
    {
        if (timer != null)
        {
            timer.purge();
            timer = null;
        }
    }

    static void scheduleStop(int playID, int duration)
    {
        if (timer != null)
        {
            TimerTask task;
            task = new TimerTask()
            {
                @Override
                public void run()
                {
                    stop(playID, duration);
                }
            };

            long delay = duration * 1000L;
            timer.schedule(task, delay);
        }
    }

    private synchronized static void stop(int playID, int duration)
    {
        if (timer != null)
        {
            ModLogger.debug("A scheduled stop was sent for playID: %d that had a duration of %s", playID, formatDuration(duration));
            PlayManager.stopPlayID(playID);
        }
    }
}
