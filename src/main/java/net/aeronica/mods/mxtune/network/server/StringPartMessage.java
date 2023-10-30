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
import net.aeronica.mods.mxtune.network.MultiPacketStringHelper;
import net.aeronica.mods.mxtune.network.MultiPacketStringManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

import static net.aeronica.mods.mxtune.network.MultiPacketStringManager.StringPartPacket;

public class StringPartMessage extends AbstractMessage.AbstractServerMessage<StringPartMessage>
{
    private UUID partStringId;

    // part
    int packetIndex;
    String partString;

    public StringPartMessage() { /* Required by the packetDispatcher */ }

    public StringPartMessage(UUID partStringId, int packetIndex, String partString)
    {
        this.partStringId = partStringId;
        this.packetIndex = packetIndex;
        this.partString = partString;
    }

    @Override
    protected void read(PacketBuffer buffer)
    {
        partStringId = buffer.readUniqueId();
        packetIndex = buffer.readInt();
        partString = buffer.readString(MultiPacketStringHelper.MAX_BUFFER);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeUniqueId(partStringId);
        buffer.writeInt(packetIndex);
        buffer.writeString(partString);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        MultiPacketStringManager.addPacket(new StringPartPacket(partStringId, packetIndex, partString));
    }
}
