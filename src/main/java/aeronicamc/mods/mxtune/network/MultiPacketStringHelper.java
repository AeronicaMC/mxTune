package aeronicamc.mods.mxtune.network;

import aeronicamc.mods.mxtune.network.messages.StringPartMessage;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static aeronicamc.mods.mxtune.network.MultiPacketStringManager.getPackets;
import static aeronicamc.mods.mxtune.network.MultiPacketStringManager.numberOfPackets;

/**
 * This is used for sending from the client to the server only.
 */
public class MultiPacketStringHelper
{
    private static final Logger LOGGER = LogManager.getLogger(MultiPacketStringHelper.class);
    private static final int MAX_BUFFER = 16384;

    private MultiPacketStringHelper() { /* NOP */ }

    @Nullable
    public static String readLongString(PacketBuffer buffer) throws IOException
    {
        int timeout = 1200;
        StringBuilder stringBuilder = new StringBuilder();

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
                LOGGER.warn("StringPartPacket Network Timeout: {}", e.getLocalizedMessage());
            }
        } while (numberOfPackets(uuid) != expectedPackets && timeout-- > 0);

        if (numberOfPackets(uuid) != expectedPackets && timeout <= 0)
        {
            LOGGER.error("StringPartPacket Network Timeout.");
            return null;
        }

        SortedMap<Integer, MultiPacketStringManager.StringPartPacket>  sortedPackets = new TreeMap<>();
        for (MultiPacketStringManager.StringPartPacket packet : getPackets(uuid))
            sortedPackets.put(packet.packetIndex, packet);


        for (Map.Entry<Integer, MultiPacketStringManager.StringPartPacket> entry : sortedPackets.entrySet())
        {
            MultiPacketStringManager.StringPartPacket packet = entry.getValue();
            stringBuilder.append(packet.part);
        }

        if (stringBuilder.length() != expectedLength)
        {
            LOGGER.error("StringPartPacket Data Length Mismatch.");
            return null;
        }

        if (expectedHashCode == stringBuilder.toString().hashCode())
        {
            LOGGER.warn("readLongString Expected Length: {}, Expected Packets: {}, Expected Hashcode {}", expectedLength, expectedPackets, expectedHashCode);
            LOGGER.warn("readLongString Received Length: {}, Received Packets: {}, Received Hashcode {}", stringBuilder.toString().length(), sortedPackets.size(), stringBuilder.toString().hashCode());
            return stringBuilder.toString();
        }
        else
        {
            LOGGER.error("readLongString received data error: Expected Hashcode does not match: {}", expectedHashCode);
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
        buffer.writeUUID(uuid);

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
