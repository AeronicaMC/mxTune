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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.network.server.ByteArrayPartMessage;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static net.aeronica.mods.mxtune.network.MultiPacketSerializedObjectManager.*;
import static net.aeronica.mods.mxtune.util.Miscellus.appendByteArrays;

public class NetworkSerializedHelper
{
    private static final int MAX_BUFFER = 24576;
    private static final Logger LOGGER = LogManager.getLogger();
    
    private NetworkSerializedHelper() { /* NOP */ }

    @Nullable
    public static Serializable readSerializedObject(PacketBuffer buffer)
    {
        int timeout = 1200;
        Serializable obj;

        UUID uuid = new UUID(buffer.readLong(), buffer.readLong());
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
                LOGGER.warn("NetworkSerializedHelper#readSerializedObject Network Timeout. {}", e.getLocalizedMessage());
            }
        } while (numberOfPackets(uuid) != expectedPackets && timeout-- > 0);

        if (numberOfPackets(uuid) != expectedPackets && timeout <= 0)
        {
            LOGGER.error("NetworkSerializedHelper#readSerializedObject Network Timeout.");
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
            LOGGER.error("NetworkSerializedHelper#readSerializedObject Data Length Mismatch.");
            return null;
        }

        try
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer);
            ObjectInputStream in = new ObjectInputStream(bis);
            obj = (Serializable) in.readObject();
            in.close();
        }
        catch (IOException|ClassNotFoundException e)
        {
            LOGGER.error(e);
            LOGGER.debug("ClassNotFoundException: obj is null");
            return null;
        }
        if ((obj != null) && (expectedHashCode == obj.hashCode()))
        {
            return obj;
        }
        else
        {
            LOGGER.error("NetworkSerializedHelper#readSerializedObject received data error: null object or hashcode does not match: {}", expectedHashCode);
            return null;
        }
    }

    @Nullable
    public static void writeSerializedObject(PacketBuffer buffer, Serializable obj)
    {
        byte[] byteBuffer = null;
        int totalLength = 0;
        int index = 0;
        int start;
        int end;

        UUID uuid = Reference.EMPTY_UUID;
        try
        {
            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject((Serializable) obj);
            out.flush();

            byteBuffer = bos.toByteArray();

            // Get and write a UUID for this MultiPacket
            uuid = UUID.randomUUID();
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
        } catch(IOException e) {
            LOGGER.error("NetworkSerializedHelper#writeSerializedObject: Failed to write serialized data. {}", e.getLocalizedMessage());
        }

        do
        {
            start = (MAX_BUFFER * index);
            end = Math.min((MAX_BUFFER * (index + 1)), totalLength);
            byte[] writeBuffer = new byte[end - start];
            System.arraycopy(byteBuffer, start, writeBuffer, 0, end - start);
            PacketDispatcher.sendToServer(new ByteArrayPartMessage(uuid, index, writeBuffer));
            index++;
        } while (end < totalLength);
    }
}
