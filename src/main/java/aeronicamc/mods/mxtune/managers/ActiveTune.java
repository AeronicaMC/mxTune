package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static aeronicamc.mods.mxtune.util.SheetMusicHelper.formatDuration;

public class ActiveTune
{
    private static final Logger LOGGER2 = LogManager.getLogger(ActiveTune.class.getSimpleName());

    private static ScheduledExecutorService scheduledThreadPool = null;
    private static ExecutorService executor = null;
    private static boolean isInitialized;

    private ScheduledFuture<?> future;
    private final AtomicInteger secondsElapsedAI = new AtomicInteger();
    private int secondsElapsed;
    private boolean done;

    protected final int entityId;
    protected final BlockPos blockPos;
    protected final int playId;
    protected final String musicText;
    protected final int durationSeconds;

    synchronized int getPlayId()
    {
        return playId;
    }

    private ActiveTune()
    {
        this.entityId = 0;
        this.blockPos = null;
        this.playId = PlayIdSupplier.INVALID;
        this.musicText = "";
        this.durationSeconds = 0;
    }

    private ActiveTune(int entityId, @Nullable BlockPos blockPos, int playId, String musicText, int durationSeconds)
    {
        this.entityId = entityId;
        this.blockPos = blockPos;
        this.playId = playId;
        this.musicText = musicText;
        this.durationSeconds = durationSeconds;
    }

    public static ActiveTune newActiveTune(int entityId, int playId, String mml, int durationSeconds)
    {
        return new ActiveTune(entityId, null, playId, mml, durationSeconds);
    }

    public static ActiveTune newActiveTune(BlockPos blockPos, int playId, String mml, int durationSeconds)
    {
        return new ActiveTune(0, blockPos, playId, mml, durationSeconds);
    }

    public static void shutdown()
    {
        executor.shutdown();
        scheduledThreadPool.shutdown();
        isInitialized = false;
    }

    public static void initialize()
    {
        if (!isInitialized)
        {
            ThreadFactory threadFactoryScheduled = new ThreadFactoryBuilder()
                    .setNameFormat(Reference.MOD_NAME + " ActiveTune-Scheduled-Counters-%d")
                    .setDaemon(true)
                    .setPriority(Thread.NORM_PRIORITY)
                    .build();
            scheduledThreadPool = Executors.newScheduledThreadPool(2, threadFactoryScheduled);

            ThreadFactory threadFactoryPool = new ThreadFactoryBuilder()
                    .setNameFormat(Reference.MOD_NAME + " ActiveTune-pool-%d")
                    .setDaemon(true)
                    .setPriority(Thread.NORM_PRIORITY)
                    .build();
            executor = Executors.newCachedThreadPool(threadFactoryPool);
            isInitialized = true;
        }
    }

    synchronized ActiveTune start()
    {
        executor.execute(this::counter);
        return this;
    }

    void cancel()
    {
        synchronized (this)
        {
            LOGGER2.debug("A scheduled or requested cancel was sent for playId: {} that had a duration of {}", playId, formatDuration(durationSeconds));
            LOGGER2.debug("Time elapsed: {}", formatDuration(secondsElapsedAI.get()));
            PlayManager.stopPlayId(playId);
            future.cancel(true);
            done = true;
        }
    }

    private void counter()
    {
        CountDownLatch lock = new CountDownLatch(durationSeconds);
        future = scheduledThreadPool.scheduleAtFixedRate(() -> {
            secondsElapsed = secondsElapsedAI.incrementAndGet();
            lock.countDown();
        }, 500, 1000, TimeUnit.MILLISECONDS);
        try
        {
            lock.await(durationSeconds * 1000, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            LOGGER2.info("Oops: {}", e.getLocalizedMessage());
        }
        finally
        {
            if (!done)
                cancel();
        }
    }

    synchronized int getEntityId()
    {
        return entityId;
    }

    synchronized int getDurationSeconds()
    {
        return durationSeconds;
    }

    synchronized boolean isDone()
    {
        return done;
    }

    synchronized int getSecondsElapsed()
    {
        return secondsElapsed + 1;
    }

    synchronized String getMusicText()
    {
        return musicText;
    }
}

