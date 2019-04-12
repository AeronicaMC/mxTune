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

import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.network.MultiPacketSerializedObjectManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

import static net.aeronica.mods.mxtune.network.MultiPacketSerializedObjectManager.SerializedObjectPacket;

public class ByteArrayPartMessage extends AbstractMessage.AbstractServerMessage<ByteArrayPartMessage>
{
    private UUID serialObjectId;

    // part
    private int packetId;
    private byte[] bytes;

    public ByteArrayPartMessage() { /* Required by the packetDispatcher */ }

    public ByteArrayPartMessage(UUID serialObjectId, int packetId, byte[] bytes)
    {
        this.serialObjectId = serialObjectId;
        this.packetId = packetId;
        this.bytes = bytes;
    }

    @Override
    protected void read(PacketBuffer buffer)
    {
        long msb = buffer.readLong();
        long lsb = buffer.readLong();
        serialObjectId = new UUID(msb, lsb);
        // Packet Id and data
        packetId = buffer.readInt();
        bytes = buffer.readByteArray();
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeLong(serialObjectId.getMostSignificantBits());
        buffer.writeLong(serialObjectId.getLeastSignificantBits());
        // Part
        buffer.writeInt(packetId);
        buffer.writeByteArray(bytes);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        MultiPacketSerializedObjectManager.addPacket(new SerializedObjectPacket(serialObjectId, packetId, bytes));
    }
}
