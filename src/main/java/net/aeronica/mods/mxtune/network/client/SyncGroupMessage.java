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
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncGroupMessage implements IMessage
{
    private final String groups;
    private final String members;

    public SyncGroupMessage(final String groups, final String members)
    {
        this.groups = groups;
        this.members = members;
    }

    public static SyncGroupMessage decode(final PacketBuffer buffer)
    {
        String groups = buffer.readString();
        String members = buffer.readString();
        return new SyncGroupMessage(groups, members);
    }

    public static void encode(final SyncGroupMessage message, final PacketBuffer buffer)
    {
        buffer.writeString(message.groups);
        buffer.writeString(message.members);
    }

    public static void handle(final SyncGroupMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient()) ctx.get().enqueueWork(() ->
            {
              GroupHelper.setClientGroups(message.groups);
              GroupHelper.setClientMembers(message.members);
              GroupHelper.setGroupsMembers(message.members);
            });
        ctx.get().setPacketHandled(true);
    }
}
