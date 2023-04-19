package aeronicamc.mods.mxtune.network;

import aeronicamc.mods.mxtune.Reference;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MultiPacketSerializedObjectManager
{
    private static final Logger LOGGER = LogManager.getLogger(MultiPacketSerializedObjectManager.class);
    private static Timer timer;
    private static final Multimap<UUID, TimerTask> tasks = Multimaps.synchronizedMultimap(HashMultimap.create());
    private static final Multimap<UUID, SerializedObjectPacket> parts = Multimaps.synchronizedMultimap(HashMultimap.create());

    private MultiPacketSerializedObjectManager() { /* NOP */ }

    public static void start()
    {
        if (timer == null)
            timer = new Timer(Reference.MOD_NAME + " MultiPacket Timer");
    }

    public static void shutdown()
    {
        if (timer != null)
        {
            timer.purge();
            timer.cancel();
            timer = null;
        }
        parts.clear();
        tasks.clear();
    }

    public static void addPacket(SerializedObjectPacket serializedObjectPart)
    {
        if (timer != null)
        {
            parts.put(serializedObjectPart.serialObjectId, serializedObjectPart);
            scheduleTimeout(serializedObjectPart.serialObjectId, 30);
        }
    }

    private static void scheduleTimeout(UUID uuid, int timeout)
    {
        if (timer != null)
        {
            TimerTask task;
            task = new TimerTask()
            {
                @Override
                public void run()
                {
                    timedOut(uuid);
                    LOGGER.warn("Timeout for: {}", uuid);
                }
            };
            tasks.put(uuid, task);
            long delay = timeout * 1000L;
            timer.schedule(task, delay);
        }
    }

    private static synchronized void timedOut(UUID uuid)
    {
        synchronized (parts)
        {
            if (parts.containsKey(uuid))
                parts.removeAll(uuid);
        }
        tasks.removeAll(uuid);
    }

    static int numberOfPackets(UUID uuid)
    {
        synchronized (parts)
        {
            return parts.get(uuid).size();
        }
    }

    static List<SerializedObjectPacket> getPackets(UUID uuid)
    {
        synchronized (parts)
        {
            List<SerializedObjectPacket> packets = Collections.unmodifiableList(new ArrayList<>(parts.get(uuid)));
            cancel(uuid);
            return packets;
        }
    }

    private static void cancel(UUID uuid)
    {
        synchronized (parts)
        {
            parts.removeAll(uuid);
        }
        synchronized (tasks)
        {
            for (TimerTask timerTask : tasks.get(uuid))
                timerTask.cancel();
            tasks.removeAll(uuid);
        }
    }

    // Data classes

    public static class SerializedObjectPacket
    {
        final UUID serialObjectId;
        final int packetId;
        final byte[] bytes;

        public SerializedObjectPacket(UUID serialObjectId, int packetId, byte[] bytes)
        {
            this.serialObjectId = serialObjectId;
            this.packetId = packetId;
            this.bytes = bytes;
        }
    }
}
