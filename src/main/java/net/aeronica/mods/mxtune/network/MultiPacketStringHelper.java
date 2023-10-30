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

import net.aeronica.mods.mxtune.network.server.StringPartMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static net.aeronica.mods.mxtune.network.MultiPacketStringManager.*;

/**
 * This is used for sending from the client to the server only.
 */
public class MultiPacketStringHelper
        {
    public static final int MAX_BUFFER = 16384;

    private MultiPacketStringHelper() { /* NOP */ }

    public static String readLongString(PacketBuffer buffer) throws IOException
    {
        int timeout = 1200;
        StringBuilder stringBuilder = new StringBuilder();

        UUID uuid = buffer.readUniqueId();
        int expectedHashCode = buffer.readInt();
        int expectedLength = buffer.readInt();
        int expectedPackets = buffer.readInt();

        do
        {
            try
            {
                Thread.sleep(25);
            } catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                ModLogger.warn("StringPartPacket Network Timeout.", e.getLocalizedMessage());
            }
        } while (numberOfPackets(uuid) != expectedPackets && timeout-- > 0);

        if (numberOfPackets(uuid) != expectedPackets && timeout <= 0)
        {
            ModLogger.error("StringPartPacket Network Timeout.");
            return null;
        }

        SortedMap<Integer, StringPartPacket>  sortedPackets = new TreeMap<>();
        for (StringPartPacket packet : getPackets(uuid))
            sortedPackets.put(packet.packetIndex, packet);

        for (Map.Entry<Integer, StringPartPacket> entry : sortedPackets.entrySet())
        {
            StringPartPacket packet = entry.getValue();
            stringBuilder.append(packet.part);
        }

        if (stringBuilder.length() != expectedLength)
        {
            ModLogger.error("StringPartPacket Data Length Mismatch.");
            return null;
        }

        if (expectedHashCode == stringBuilder.toString().hashCode())
        {
            ModLogger.warn("readLongString Expected Length: %d, Expected Packets: %d, Expected Hashcode %d", expectedLength, expectedPackets, expectedHashCode);
            ModLogger.warn("readLongString Received Length: %d, Received Packets: %d, Received Hashcode %d", stringBuilder.toString().length(), sortedPackets.size(), stringBuilder.toString().hashCode());
            return stringBuilder.toString();
        }
        else
        {
            ModLogger.error("readLongString received data error: Expected Hashcode does not match: %d", expectedHashCode);
            return null;
        }
    }

    public static void writeLongString(PacketBuffer buffer, String longString) throws IOException
    {
        int totalLength;
        int index = 0;
        int start;
        int end;

        // Get and write a UUID for this MultiPacket
        UUID uuid = UUID.randomUUID();
        buffer.writeUniqueId(uuid);

        // Write hash code
        buffer.writeInt(longString.hashCode());

        // Write total length
        totalLength = longString.length();
        buffer.writeInt(totalLength);

        // Write number of packets
        int numPackets = totalLength > 0 ? (totalLength / MAX_BUFFER) + 1 : 0;
        buffer.writeInt(numPackets);

        do
        {
            start = (MAX_BUFFER * index);
            end = Math.min((MAX_BUFFER * (index + 1)), totalLength);
            PacketDispatcher.sendToServer(new StringPartMessage(uuid, index, longString.substring(start, end)));
            index++;
        } while (end < totalLength);
    }
}
