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

import net.aeronica.mods.mxtune.caps.player.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.GUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerSelectedPlayListMessage
{
    private final GUID selectedAreaGuid;
    private final long ddddSigBits;
    private final long ccccSigBits;
    private final long bbbbSigBits;
    private final long aaaaSigBits;

    public PlayerSelectedPlayListMessage(GUID selectedAreaGuid)
    {
        this.selectedAreaGuid = selectedAreaGuid;
        this.ddddSigBits = selectedAreaGuid.getDdddSignificantBits();
        this.ccccSigBits = selectedAreaGuid.getCcccSignificantBits();
        this.bbbbSigBits = selectedAreaGuid.getBbbbSignificantBits();
        this.aaaaSigBits = selectedAreaGuid.getAaaaSignificantBits();
    }

    public static PlayerSelectedPlayListMessage decode(final PacketBuffer buffer)
    {
        long ddddSigBits = buffer.readLong();
        long ccccSigBits = buffer.readLong();
        long bbbbSigBits = buffer.readLong();
        long aaaaSigBits = buffer.readLong();
        GUID selectedAreaGuid = new GUID(ddddSigBits, ccccSigBits, bbbbSigBits, aaaaSigBits);
        return new PlayerSelectedPlayListMessage(selectedAreaGuid);
    }

    public static void encode(final PlayerSelectedPlayListMessage message, final PacketBuffer buffer)
    {
        buffer.writeLong(message.ddddSigBits);
        buffer.writeLong(message.ccccSigBits);
        buffer.writeLong(message.bbbbSigBits);
        buffer.writeLong(message.aaaaSigBits);
    }

    public static void handle(final PlayerSelectedPlayListMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(()->MusicOptionsUtil.setSelectedPlayListGuid(player, message.selectedAreaGuid));
    }
}
