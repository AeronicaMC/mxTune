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

package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.network.MultiPacketSerializedObjectManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

import static net.aeronica.mods.mxtune.network.MultiPacketSerializedObjectManager.SerializedObjectPacket;

public class ByteArrayPartMessage
{
    private final UUID serialObjectId;
    // part
    private final int packetId;
    private final byte[] bytes;

    public ByteArrayPartMessage(final UUID serialObjectId, final int packetId, final byte[] bytes)
    {
        this.serialObjectId = serialObjectId;
        this.packetId = packetId;
        this.bytes = bytes;
    }

    public static ByteArrayPartMessage decode(final PacketBuffer buffer)
    {
        long msb = buffer.readLong();
        long lsb = buffer.readLong();
        UUID serialObjectId = new UUID(msb, lsb);
        // Packet Id and data
        int packetId = buffer.readInt();
        byte[] bytes = buffer.readByteArray();
        return new ByteArrayPartMessage(serialObjectId, packetId, bytes);
    }

    public static void encode(final ByteArrayPartMessage message, final PacketBuffer buffer)
    {
        buffer.writeLong(message.serialObjectId.getMostSignificantBits());
        buffer.writeLong(message.serialObjectId.getLeastSignificantBits());
        // Part
        buffer.writeInt(message.packetId);
        buffer.writeByteArray(message.bytes);
    }

    public static void handle(final ByteArrayPartMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(()->
                MultiPacketSerializedObjectManager.addPacket(new SerializedObjectPacket(message.serialObjectId, message.packetId, message.bytes)));
    }
}
