package aeronicamc.mods.mxtune.sound;

import aeronicamc.mods.mxtune.Reference;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static aeronicamc.mods.mxtune.managers.PlayIdSupplier.INVALID;

public class ActiveAudio
{
    private static final Logger LOGGER = LogManager.getLogger(ActiveAudio.class);
    private static final List<AudioData> activeAudioData = new ArrayList<>(16);
    private static final Queue<AudioData> deleteAudioDataQueue = new ConcurrentLinkedQueue<>();

    private static ScheduledExecutorService scheduledThreadPool = null;
    private static boolean isInitialized;

    private ActiveAudio() { /* NOP */ }

    public static void initialize()
    {
        if (!isInitialized)
        {
            ThreadFactory threadFactoryScheduled = new ThreadFactoryBuilder()
                    .setNameFormat(Reference.MOD_NAME + " ActiveAudio-Counter-%d")
                    .setDaemon(true)
                    .setPriority(Thread.NORM_PRIORITY)
                    .build();
            scheduledThreadPool = Executors.newScheduledThreadPool(1, threadFactoryScheduled);

            ThreadFactory threadFactoryPool = new ThreadFactoryBuilder()
                    .setNameFormat(Reference.MOD_NAME + " ActiveAudio-pool-%d")
                    .setDaemon(true)
                    .setPriority(Thread.MIN_PRIORITY)
                    .build();
            ExecutorService executor = Executors.newCachedThreadPool(threadFactoryPool);
            isInitialized = true;
            ActiveAudio activeTune = new ActiveAudio();
            executor.execute(activeTune::counter);
            LOGGER.debug("ActiveAudio initialized.");
        }
    }

    static void addEntry(AudioData audioData)
    {
        synchronized (activeAudioData)
        {
            activeAudioData.add(audioData);
        }
    }

    static List<AudioData> getDistanceSortedSources()
    {
        return activeAudioData.stream().sorted(Comparator.comparingDouble(AudioData::getDistanceTo)).collect(Collectors.toList());
    }

    static boolean isPlaying()
    {
        return activeAudioData.stream().anyMatch(audioData -> ClientAudio.PLAYING_STATUSES.contains(audioData.getStatus()));
    }

    @Nullable
    static AudioData getAudioData(int playId)
    {
        return activeAudioData.stream().filter(audioData -> audioData.getPlayId() == playId).findFirst().orElse(null);
    }

    static Set<Integer> getActivePlayIds()
    {
        return activeAudioData.stream().map(AudioData::getPlayId).collect(Collectors.toSet());
    }

    static boolean isActivePlayId(int playId)
    {
        return (playId != INVALID) && ActiveAudio.getActivePlayIds().contains(playId);
    }

    static Optional<AudioData> getActiveTuneByEntityId(@Nullable Entity entity)
    {
        synchronized (activeAudioData)
        {
            return activeAudioData.stream().filter(entry -> ((entity != null) && (entry.getEntityId() == entity.getId()))).findFirst();
        }
    }

    static Optional<AudioData> getActiveTuneByEntityId(int entityId)
    {
        return getDistanceSortedSources().stream().filter(entry -> (entityId == entry.getEntityId())).findFirst();
    }

    static int getDeleteQueueSize()
    {
        return deleteAudioDataQueue.size();
    }

    static void removeAll()
    {
        synchronized (activeAudioData)
        {
            activeAudioData.forEach(AudioData::expire);
        }
        while (!deleteAudioDataQueue.isEmpty())
            synchronized (deleteAudioDataQueue)
            {
                deleteAudioDataQueue.remove();
            }
    }

    void counter()
    {
        CountDownLatch lock = new CountDownLatch(Integer.MAX_VALUE);
        scheduledThreadPool.scheduleAtFixedRate(
                () ->
                {
                    if (!deleteAudioDataQueue.isEmpty())
                        activeAudioData.remove(deleteAudioDataQueue.remove());

                    activeAudioData
                            .forEach(audioData ->
                                     {
                                         if (audioData.canRemove() || audioData.isEntityRemoved() || ClientAudio.DONE_STATUSES.contains(audioData.getStatus()))
                                         {
                                             audioData.expire();
                                             deleteAudioDataQueue.add(audioData);
                                         }
                                         audioData.tick();
                                     });

                    lock.countDown();
                }, 500, 1000, TimeUnit.MILLISECONDS);
        try
        {
            //noinspection ResultOfMethodCallIgnored
            lock.await(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e)
        {
            LOGGER.warn("Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }
}


