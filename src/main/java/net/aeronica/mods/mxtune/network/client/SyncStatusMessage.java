/*
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.network.client;

import net.aeronica.mods.mxtune.managers.GroupHelper;
import net.aeronica.mods.mxtune.network.IMessage;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncStatusMessage implements IMessage
{
    private final String clientPlayStatuses;
    private final String playIDMembers;
    private final String activePlayIDs;

    public SyncStatusMessage(String clientPlayStatuses, String playIDMembers, String activePlayIDs)
    {
        this.clientPlayStatuses = clientPlayStatuses;
        this.playIDMembers = playIDMembers;
        this.activePlayIDs = activePlayIDs;
    }

    public static SyncStatusMessage decode(PacketBuffer buffer)
    {
        String clientPlayStatuses = buffer.readString();
        String playIDMembers = buffer.readString();
        String activePlayIDs = buffer.readString();
        return new SyncStatusMessage(clientPlayStatuses, playIDMembers, activePlayIDs);
    }

    public static void encode(final SyncStatusMessage message, final PacketBuffer buffer)
    {
        buffer.writeString(message.clientPlayStatuses);
        buffer.writeString(message.playIDMembers);
        buffer.writeString(message.activePlayIDs);
    }

    public static void handle(final SyncStatusMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
            ctx.get().enqueueWork(() ->
                {
                  synchronized (ClientAudio.THREAD_SYNC)
                  {
                      GroupHelper.setClientPlayStatuses(message.clientPlayStatuses);
                      GroupHelper.setPlayIDMembers(message.playIDMembers);
                      GroupHelper.setActiveServerManagedPlayIDs(message.activePlayIDs);
                  }
                });
        ctx.get().setPacketHandled(true);
    }
}
