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

package net.aeronica.mods.mxtune.network;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.ModLogger;

import java.util.*;

public class MultiPacketStringManager
{
    private static Timer timer;
    private static final Multimap<UUID, TimerTask> tasks = Multimaps.synchronizedMultimap(HashMultimap.create());
    private static final Multimap<UUID, StringPartPacket> parts = Multimaps.synchronizedMultimap(HashMultimap.create());

    private MultiPacketStringManager() { /* NOP */ }

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

    public static void addPacket(StringPartPacket serializedObjectPart)
    {
        if (timer != null)
        {
            parts.put(serializedObjectPart.stringPartId, serializedObjectPart);
            scheduleTimeout(serializedObjectPart.stringPartId, 30);
            ModLogger.debug("MultiPacketStringManager addPacket %s", serializedObjectPart.stringPartId.toString());
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
                    ModLogger.warn("MultiPacketStringManager Timeout for: %s", uuid);
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

    static List<StringPartPacket> getPackets(UUID uuid)
    {
        synchronized (parts)
        {
            List<StringPartPacket> packets = Collections.unmodifiableList(new ArrayList<>(parts.get(uuid)));
            cancel(uuid);
            ModLogger.debug("MultiPacketStringManager getPackets: %s", uuid.toString());
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

    public static class StringPartPacket
    {
        final UUID stringPartId;
        final int packetIndex;
        final String part;

        public StringPartPacket(UUID stringPartId, int packetIndex, String part)
        {
            this.stringPartId = stringPartId;
            this.packetIndex = packetIndex;
            this.part = part;
        }
    }
}
