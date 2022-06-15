package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;

import static aeronicamc.mods.mxtune.managers.PlayIdSupplier.INVALID;

public class ActiveTune
{
    private static final Logger LOGGER2 = LogManager.getLogger(ActiveTune.class);
    private static final Map<Integer, ActiveTune.Entry> playIdToActiveTuneEntry = new ConcurrentHashMap<>(16);
    private static final Map<Integer, ActiveTune.Entry> entityIdToActiveTuneEntry = new ConcurrentHashMap<>(16);


    private static final Queue<ActiveTune.Entry> deleteEntryQueue = new ConcurrentLinkedQueue<>();

    private static ScheduledExecutorService scheduledThreadPool = null;
    private static boolean isInitialized;

    private ActiveTune() { /* NOP */ }

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
            ExecutorService executor = Executors.newCachedThreadPool(threadFactoryPool);
            isInitialized = true;
            ActiveTune activeTune = new ActiveTune();
            executor.execute(activeTune::counter);
            LOGGER2.debug("ActiveTune initialized.");
        }
    }

    void counter()
    {
        CountDownLatch lock = new CountDownLatch(Integer.MAX_VALUE);
        scheduledThreadPool.scheduleAtFixedRate(
                () ->
                {
                    playIdToActiveTuneEntry.values()
                            .forEach(entry ->
                                     {
                                         if (entry.canRemove())
                                         {
                                             deleteEntryQueue.add(entry);
                                             PlayManager.stopPlayId(entry.playId);
                                         }
                                         entry.tickDuration();
                                     });
                    processDelete();
                    lock.countDown();
                }, 500, 1000, TimeUnit.MILLISECONDS);
        try
        {
            //noinspection ResultOfMethodCallIgnored
            lock.await(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            LOGGER2.error(e);
        }
    }

    private static void processDelete()
    {
        if (!deleteEntryQueue.isEmpty())
        {
            Entry entry = deleteEntryQueue.remove();
            entityIdToActiveTuneEntry.remove(entry.entityId);
            playIdToActiveTuneEntry.remove(entry.playId);
        }
    }

    static void addEntry(int entityId, @Nullable BlockPos blockPos, int playId, String mml, int durationSeconds)
    {
        synchronized (playIdToActiveTuneEntry)
        {
            Entry entry = Entry.newEntry(entityId, playId, mml, durationSeconds);
            playIdToActiveTuneEntry.putIfAbsent(playId, Entry.newEntry(entityId, playId, mml, durationSeconds));
            entityIdToActiveTuneEntry.putIfAbsent(entityId, entry);
        }
    }

    synchronized static List<Entry> getActiveTuneEntries()
    {
        return new ArrayList<>(playIdToActiveTuneEntry.values());
    }

    synchronized static Set<Integer> getActivePlayIds()
    {
        return playIdToActiveTuneEntry.keySet();
    }

    synchronized static boolean entityExists(int entityId)
    {
        return entityIdToActiveTuneEntry.containsKey(entityId);
    }

    synchronized static boolean entityActive(int entityId)
    {
        return entityExists(entityId) && entityIdToActiveTuneEntry.get(entityId).isActive();
    }

    synchronized static int getPlayIdForEntity(int entityId)
    {
        return entityExists(entityId) ? entityIdToActiveTuneEntry.get(entityId).playId : INVALID;
    }

    synchronized static boolean isActivePlayId(int playId)
    {
        return playIdToActiveTuneEntry.containsKey(playId) && playIdToActiveTuneEntry.get(playId).isActive();
    }

    synchronized static Optional<Entry> getActiveTuneByEntityId(@Nullable Entity entity)
    {
        return getActiveTuneEntries().stream().filter(entry -> ((entity != null) && (entry.entityId == entity.getId()))).findFirst();
    }

    static void remove(int playId)
    {
        synchronized (playIdToActiveTuneEntry)
        {
            if (playIdToActiveTuneEntry.containsKey(playId))
            {
                Entry entry = playIdToActiveTuneEntry.get(playId);
                if (entry.isActive())
                    entry.secondsElapsed = entry.durationSeconds + 1;
            }
        }
    }

    static void removeAll()
    {
        synchronized (playIdToActiveTuneEntry)
        {
            playIdToActiveTuneEntry.clear();
            entityIdToActiveTuneEntry.clear();
            while (!deleteEntryQueue.isEmpty())
            {
                deleteEntryQueue.remove();
            }
        }
    }

    static class Entry
    {
        private int secondsElapsed;

        final String musicText;
        final int entityId;
        final int playId;
        final int durationSeconds;
        final int removalSeconds;

        private Entry(int entityId, int playId, String musicText, int durationSeconds)
        {
            this.entityId = entityId;
            this.playId = playId;
            this.musicText = musicText;
            this.durationSeconds = durationSeconds;
            this.removalSeconds = durationSeconds + 10;
        }

        static Entry newEntry(int entityId, int playId, String mml, int durationSeconds)
        {
            return new Entry(entityId, playId, mml, durationSeconds);
        }

        int getSecondsElapsed()
        {
            return secondsElapsed;
        }

        boolean isActive()
        {
            return durationSeconds >= secondsElapsed;
        }

        boolean canRemove()
        {
            return removalSeconds < secondsElapsed;
        }

        void tickDuration()
        {
            ++secondsElapsed;
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("entityId", entityId)
                    .append("playId", playId)
                    .append("durationSeconds", durationSeconds)
                    .append("removalSeconds", removalSeconds)
                    .build();
        }
    }
}


