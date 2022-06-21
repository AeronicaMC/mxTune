package aeronicamc.mods.mxtune.sound;

import aeronicamc.mods.mxtune.Reference;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.Minecraft;
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
    private static final Map<Integer, AudioData> playIdToActiveAudioEntry = new ConcurrentHashMap<>(16);
    private static final Queue<AudioData> deleteEntryQueue = new ConcurrentLinkedQueue<>();
    private static final Minecraft mc = Minecraft.getInstance();

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
                    .setPriority(Thread.MIN_PRIORITY)
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

    void counter()
    {
        CountDownLatch lock = new CountDownLatch(Integer.MAX_VALUE);
        scheduledThreadPool.scheduleAtFixedRate(
                () ->
                {
                    playIdToActiveAudioEntry.values()
                            .forEach(entry ->
                                     {
                                         if (entry.canRemove() || entry.getStatus().equals(ClientAudio.Status.DONE) || entry.getStatus().equals(ClientAudio.Status.ERROR))
                                         {
                                             entry.expire();
                                             deleteEntryQueue.add(entry);
                                         }
                                         entry.tickDuration();
                                     });
                    if (!deleteEntryQueue.isEmpty())
                        playIdToActiveAudioEntry.remove(deleteEntryQueue.remove().getPlayId());
                    lock.countDown();
                }, 500, 1000, TimeUnit.MILLISECONDS);
        try
        {
            //noinspection ResultOfMethodCallIgnored
            lock.await(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            LOGGER.error(e);
        }
    }

    static boolean addEntry(AudioData audioData)
    {
        boolean hasDuplicatePlayId;
        synchronized (playIdToActiveAudioEntry)
        {
            hasDuplicatePlayId = playIdToActiveAudioEntry.containsKey(audioData.getPlayId());
            if (hasDuplicatePlayId)
                playIdToActiveAudioEntry.replace(audioData.getPlayId(), audioData);
            else
                playIdToActiveAudioEntry.put(audioData.getPlayId(), audioData);
        }
        return hasDuplicatePlayId;
    }

    static List<AudioData> getDistanceSortedSources()
    {
        return Collections.unmodifiableList(playIdToActiveAudioEntry.values().stream().sorted(Comparator.comparingDouble(AudioData::getDistanceTo)).collect(Collectors.toList()));
    }

    @Nullable
    synchronized static AudioData getAudioData(int playId)
    {
        return playIdToActiveAudioEntry.get(playId);
    }

    synchronized static Set<Integer> getActivePlayIds()
    {
        return playIdToActiveAudioEntry.keySet();
    }

    synchronized static boolean isActivePlayId(int playId)
    {
        return (playId != INVALID) && ActiveAudio.getActivePlayIds().contains(playId);
    }

    synchronized static Optional<AudioData> getActiveTuneByEntityId(@Nullable Entity entity)
    {
        return playIdToActiveAudioEntry.values().stream().filter(entry -> ((entity != null) && (entry.getEntityId() == entity.getId()))).findFirst();
    }

    synchronized static Optional<AudioData> getActiveTuneByEntityId(int entityId)
    {
        return getDistanceSortedSources().stream().filter(entry -> (entityId == entry.getEntityId())).findFirst();
    }

    synchronized static int getDeleteQueueSize()
    {
        return deleteEntryQueue.size();
    }

    static void remove(int playId)
    {
        synchronized (playIdToActiveAudioEntry)
        {
            if (!playIdToActiveAudioEntry.isEmpty())
                playIdToActiveAudioEntry.remove(playId);
        }
    }

    static void removeAll()
    {
        synchronized (playIdToActiveAudioEntry)
        {
            playIdToActiveAudioEntry.forEach((playId, audioData) -> audioData.expire());
            while (!deleteEntryQueue.isEmpty())
            {
                deleteEntryQueue.remove();
            }
        }
    }
}


