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

package net.aeronica.mods.mxtune.network.client;

import net.aeronica.mods.mxtune.caps.world.ModWorldPlaylistHelper;
import net.aeronica.mods.mxtune.network.IMessage;
import net.aeronica.mods.mxtune.util.GUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateWorldMusicData implements IMessage
{
    private final GUID guid;
    private final long ddddSigBits;
    private final long ccccSigBits;
    private final long bbbbSigBits;
    private final long aaaaSigBits;

    public UpdateWorldMusicData(final GUID guid)
    {
        this.guid = guid;
        ddddSigBits = guid.getDdddSignificantBits();
        ccccSigBits = guid.getCcccSignificantBits();
        bbbbSigBits = guid.getBbbbSignificantBits();
        aaaaSigBits = guid.getAaaaSignificantBits();
    }

    public static UpdateWorldMusicData decode(final PacketBuffer buffer)
    {
        long ddddSigBits = buffer.readLong();
        long ccccSigBits = buffer.readLong();
        long bbbbSigBits = buffer.readLong();
        long aaaaSigBits = buffer.readLong();
        GUID guid = new GUID(ddddSigBits, ccccSigBits, bbbbSigBits, aaaaSigBits);
        return new UpdateWorldMusicData(guid);
    }

    public static void encode(final UpdateWorldMusicData message, final PacketBuffer buffer)
    {
        buffer.writeLong(message.ddddSigBits);
        buffer.writeLong(message.ccccSigBits);
        buffer.writeLong(message.bbbbSigBits);
        buffer.writeLong(message.aaaaSigBits);
    }

    public static void handle(final UpdateWorldMusicData message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(()->{
                ServerPlayerEntity player = ctx.get().getSender();
                World world = player != null ? player.getEntityWorld() : null;
                if (world != null)
                    ModWorldPlaylistHelper.setPlaylistGuid(world, message.guid);
            });
        ctx.get().setPacketHandled(true);
    }
}
