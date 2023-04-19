package aeronicamc.mods.mxtune.network;
import aeronicamc.mods.mxtune.network.messages.ByteArrayPartMessage;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static aeronicamc.mods.mxtune.network.MultiPacketSerializedObjectManager.getPackets;
import static aeronicamc.mods.mxtune.network.MultiPacketSerializedObjectManager.numberOfPackets;
import static aeronicamc.mods.mxtune.util.Misc.appendByteArrays;


public class NetworkSerializedHelper
{
    private static final Logger LOGGER = LogManager.getLogger(NetworkSerializedHelper.class);
    private static final int MAX_BUFFER = 24576;

    private NetworkSerializedHelper() { /* NOP */ }

    @Nullable
    public static Serializable readSerializedObject(PacketBuffer buffer) throws IOException
    {
        int timeout = 1200;
        Serializable obj;

        UUID uuid = buffer.readUUID();
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
                LOGGER.warn("readSerializedObject Network Timeout: {}", e.getLocalizedMessage());
            }
        } while (numberOfPackets(uuid) != expectedPackets && timeout-- > 0);

        if (numberOfPackets(uuid) != expectedPackets && timeout <= 0)
        {
            LOGGER.error("readSerializedObject Network Timeout.");
            return null;
        }

        SortedMap<Integer, MultiPacketSerializedObjectManager.SerializedObjectPacket>  sortedPackets = new TreeMap<>();
        for (MultiPacketSerializedObjectManager.SerializedObjectPacket packet : getPackets(uuid))
            sortedPackets.put(packet.packetId, packet);

        byte[] byteBuffer = new byte[0];
        for (Map.Entry<Integer, MultiPacketSerializedObjectManager.SerializedObjectPacket> entry : sortedPackets.entrySet())
        {
            MultiPacketSerializedObjectManager.SerializedObjectPacket packet = entry.getValue();
            byteBuffer = appendByteArrays(byteBuffer, packet.bytes, packet.bytes.length);
        }

        if (byteBuffer.length != expectedLength)
        {
            LOGGER.error("readSerializedObject Data Length Mismatch.");
            return null;
        }

        try
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer);
            ObjectInputStream in = new ObjectInputStream(bis);
            obj = (Serializable) in.readObject();
            in.close();
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.error(e);
            LOGGER.error("ClassNotFoundException: obj is null");
            return null;
        }
        if ((obj != null) && (expectedHashCode == obj.hashCode()))
        {
            LOGGER.warn("readSerializedObject Expected Length: {}, Expected Packets: {}", expectedLength, expectedPackets);
            LOGGER.warn("readSerializedObject Received Length: {}, Received Packets: {}", byteBuffer.length, sortedPackets.size());
            return obj;
        }
        else
        {
            LOGGER.error("readSerializedObject received data error: null object or hashcode does not match: {}", expectedHashCode);
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
        buffer.writeUUID(uuid);

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
    }
}
