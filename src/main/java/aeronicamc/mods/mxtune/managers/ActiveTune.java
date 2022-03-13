package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static aeronicamc.mods.mxtune.managers.PlayIdSupplier.INVALID;

public class ActiveTune
{
    private static final Logger LOGGER2 = LogManager.getLogger(ActiveTune.class);
    private static final Map<Integer, ActiveTune.Entry> playIdToActiveTuneEntry = new ConcurrentHashMap<>(16);
    private static final Queue<ActiveTune.Entry> deleteEntryQueue = new ConcurrentLinkedQueue<>();

    private static ScheduledExecutorService scheduledThreadPool = null;
    private static ExecutorService executor = null;
    private static boolean isInitialized;

    private ScheduledFuture<?> future;
    private final AtomicInteger secondsElapsedAI = new AtomicInteger();
    private int secondsElapsed;

    private ActiveTune() { /* NOP */ }

    public static void shutdown()
    {
        isInitialized = false;
        executor.shutdown();
        scheduledThreadPool.shutdown();
        LOGGER2.debug("ActiveTune Shutdown.");
    }

    public static void initialize()
    {
        if (!isInitialized)
        {
            ThreadFactory threadFactoryScheduled = new ThreadFactoryBuilder()
                    .setNameFormat(Reference.MOD_NAME + " ActiveTune-Counter-%d")
                    .setDaemon(true)
                    .setPriority(Thread.MIN_PRIORITY)
                    .build();
            scheduledThreadPool = Executors.newScheduledThreadPool(1, threadFactoryScheduled);

            ThreadFactory threadFactoryPool = new ThreadFactoryBuilder()
                    .setNameFormat(Reference.MOD_NAME + " ActiveTune-pool-%d")
                    .setDaemon(true)
                    .setPriority(Thread.MIN_PRIORITY)
                    .build();
            executor = Executors.newCachedThreadPool(threadFactoryPool);
            isInitialized = true;
            ActiveTune activeTune = new ActiveTune();
            executor.execute(activeTune::counter);
            LOGGER2.debug("ActiveTune initialized.");
        }
    }

    void counter()
    {
        CountDownLatch lock = new CountDownLatch(Integer.MAX_VALUE);
        future = scheduledThreadPool.scheduleAtFixedRate(() -> {
            secondsElapsed = secondsElapsedAI.incrementAndGet();
            playIdToActiveTuneEntry.values().forEach(entry -> {
                if (entry.isDone())
                    deleteEntryQueue.add(entry);
                entry.pollDone();
            });
            if (!deleteEntryQueue.isEmpty())
                playIdToActiveTuneEntry.remove(deleteEntryQueue.remove().playId);
            lock.countDown();
        }, 500, 1000, TimeUnit.MILLISECONDS);
        try
        {
            lock.await(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            LOGGER2.error(e);
        }
    }

    synchronized static void addEntry(int entityId, int playId, String mml, int durationSeconds)
    {
        playIdToActiveTuneEntry.putIfAbsent(playId, Entry.newEntry(entityId, playId, mml, durationSeconds));
    }

    synchronized static void addEntry(BlockPos blockPos, int playId, String mml, int durationSeconds)
    {
        playIdToActiveTuneEntry.putIfAbsent(playId, Entry.newEntry(blockPos, playId, mml, durationSeconds));
    }

    synchronized static List<Entry> getActiveTuneEntries()
    {
        return new ArrayList<>(playIdToActiveTuneEntry.values());
    }

    synchronized static Set<Integer> getActivePlayIds()
    {
        return playIdToActiveTuneEntry.keySet();
    }

    synchronized static boolean isActivePlayId(int playId)
    {
        return (playId != INVALID) && ActiveTune.getActivePlayIds().contains(playId);
    }

    synchronized static boolean isEmpty()
    {
        return playIdToActiveTuneEntry.isEmpty();
    }

    synchronized static Optional<Entry> getActiveTuneByEntityId(@Nullable Entity entity)
    {
        Entry[] result = {null};
        if (entity != null)
            getActiveTuneEntries().stream().filter(entry -> entry.entityId == entity.getId()).findFirst().ifPresent(entry -> {
                result[0] = entry;
            });
        return Optional.ofNullable(result[0]);
    }

    synchronized static void remove(int playId)
    {
        if (!playIdToActiveTuneEntry.isEmpty())
            playIdToActiveTuneEntry.remove(playId);
    }

    synchronized static void removeAll()
    {
        playIdToActiveTuneEntry.clear();
        while (!deleteEntryQueue.isEmpty()) {
            deleteEntryQueue.remove();
        }
    }

    static class Entry
    {
        private int secondsElapsed;

        final String musicText;
        final int entityId;
        final BlockPos blockPos;
        final int playId;
        final int durationSeconds;

        private Entry()
        {
            this.entityId = 0;
            this.blockPos = null;
            this.playId = INVALID;
            this.musicText = "";
            this.durationSeconds = 0;
        }

        private Entry(int entityId, @Nullable BlockPos blockPos, int playId, String musicText, int durationSeconds)
        {
            this.entityId = entityId;
            this.blockPos = blockPos;
            this.playId = playId;
            this.musicText = musicText;
            this.durationSeconds = durationSeconds;
        }

        static Entry newEntry(int entityId, int playId, String mml, int durationSeconds)
        {
            return new Entry(entityId, null, playId, mml, durationSeconds);
        }

        static Entry newEntry(BlockPos blockPos, int playId, String mml, int durationSeconds)
        {
            return new Entry(0, blockPos, playId, mml, durationSeconds);
        }

        int getSecondsElapsed()
        {
            return secondsElapsed;
        }

        int getDurationSeconds()
        {
            return durationSeconds;
        }

        boolean isDone()
        {
            return durationSeconds < secondsElapsed;
        }

        void pollDone()
        {
            boolean isDone = durationSeconds <= ++secondsElapsed;
            if (isDone) executor.execute(()-> PlayManager.stopPlayId(playId));
        }
    }
}


