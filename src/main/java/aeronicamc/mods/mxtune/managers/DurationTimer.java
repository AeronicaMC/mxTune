package aeronicamc.mods.mxtune.managers;



import aeronicamc.mods.mxtune.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

import static aeronicamc.mods.mxtune.util.SheetMusicHelper.formatDuration;


/**
 * Schedule removal of the playID after a specified duration in seconds.
 * This is a simple implementation with no management options, such as removing tasks.
 */
public class DurationTimer
{
    private static final Logger LOGGER = LogManager.getLogger(DurationTimer.class.getSimpleName());
    private static Timer timer;
    private DurationTimer() { /* NOP */ }

    public static void start()
    {
        timer = new Timer(Reference.MOD_NAME + " Duration Timer");
    }

    public static void shutdown()
    {
        if (timer != null)
        {
            timer.purge();
            timer.cancel();
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

    private static synchronized void stop(int playID, int duration)
    {
        if (timer != null)
        {
            LOGGER.debug("A scheduled stop was sent for playID: {} that had a duration of {}", playID, formatDuration(duration));
            PlayManager.stopPlayID(playID);
        }
    }
}
