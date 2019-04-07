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

import net.aeronica.mods.mxtune.network.server.ByteArrayPartMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.network.PacketBuffer;

import java.io.*;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static net.aeronica.mods.mxtune.network.MultiPacketSerializedObjectManager.*;
import static net.aeronica.mods.mxtune.util.Util.appendByteArrays;

public class NetworkSerializedHelper
{
    private static final int MAX_BUFFER = 24576;

    public NetworkSerializedHelper() { /* NOP */ }

    public static Serializable readSerializedObject(PacketBuffer buffer) throws IOException
    {
        int timeout = 300;
        Serializable obj;

        UUID uuid = new UUID(buffer.readLong(), buffer.readLong());
        int expectedHashCode = buffer.readInt();
        int expectedLength = buffer.readInt();
        int expectedPackets = buffer.readInt();

        do
        {
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        } while (numberOfPackets(uuid) != expectedPackets && timeout-- > 0);

        if (numberOfPackets(uuid) != expectedPackets && timeout <= 0)
        {
            ModLogger.error("NetworkSerializedHelper#readSerializedObject Network Timeout.");
            return null;
        }

        SortedMap<Integer, SerializedObjectPacket>  sortedPackets = new TreeMap<>();
        for (SerializedObjectPacket packet : getPackets(uuid))
            sortedPackets.put(packet.packetId, packet);

        byte[] byteBuffer = new byte[0];
        for (Map.Entry<Integer, SerializedObjectPacket> entry : sortedPackets.entrySet())
        {
            SerializedObjectPacket packet = entry.getValue();
            byteBuffer = appendByteArrays(byteBuffer, packet.bytes, packet.bytes.length);
        }

        if (byteBuffer.length != expectedLength)
        {
            ModLogger.error("NetworkSerializedHelper#readSerializedObject Data Length Mismatch.");
            return null;
        }

        try
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer);
            ObjectInputStream in = new ObjectInputStream(bis);
            obj = (Serializable) in.readObject();
            in.close();
            ModLogger.debug("");
            ModLogger.debug("ReadObjStats: expectedLength: %d, expectedHashCode: %d, expectedPackets: %d", expectedLength, expectedHashCode, expectedPackets);
        }
        catch (ClassNotFoundException e)
        {
            ModLogger.error(e);
            ModLogger.debug("ClassNotFoundException: obj is null");
            return null;
        }
        if ((obj != null && expectedHashCode == obj.hashCode()))
        {
            return obj;
        }
        else
        {
            ModLogger.error("NetworkSerializedHelper#readSerializedObject received data error: expected hash: %d, actual: %d", expectedHashCode, obj.hashCode());
            return null;
        }
    }

    public static void writeSerializedObject(PacketBuffer buffer, Serializable obj) throws IOException
    {
        byte[] byteBuffer;
        int totalLength;
        int index = 0;
        int start;
        int end;

        // Serialize data object to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject((Serializable) obj);
        out.flush();

        byteBuffer = bos.toByteArray();

        // Get and write a UUID for this MultiPacket
        UUID uuid = UUID.randomUUID();
        buffer.writeLong(uuid.getMostSignificantBits());
        buffer.writeLong(uuid.getLeastSignificantBits());

        // Write hash code
        buffer.writeInt(obj.hashCode());

        // Write total length
        totalLength = byteBuffer.length;
        buffer.writeInt(totalLength);

        // Write number of packets
        int numPackets = totalLength > 0 ? (totalLength / MAX_BUFFER) + 1 : 0;
        buffer.writeInt(numPackets);

        do
        {
            start = (MAX_BUFFER * index);
            end = Math.min((MAX_BUFFER * (index + 1)), totalLength);
            byte[] writeBuffer = new byte[end - start];
            System.arraycopy(byteBuffer, start, writeBuffer, 0, end - start);
            PacketDispatcher.sendToServer(new ByteArrayPartMessage(uuid, index, writeBuffer));
            index++;
        } while (end < totalLength);

        ModLogger.debug("");
        ModLogger.debug("writeSerializedObject: totalLength: %s, obj.hashCode: %d, numPackets: %d", totalLength, obj.hashCode(), numPackets);
    }
}
