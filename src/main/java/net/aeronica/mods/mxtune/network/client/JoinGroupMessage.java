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

import net.aeronica.mods.mxtune.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.function.Supplier;

public class JoinGroupMessage implements IMessage
{
    private final int groupID;

    public JoinGroupMessage(final int groupID) {this.groupID = groupID; }

    public static JoinGroupMessage decode(final PacketBuffer buffer)
    {
        int groupID = buffer.readInt();
        return new JoinGroupMessage(groupID);
    }

    public static void encode(final JoinGroupMessage message, final PacketBuffer buffer)
    {
        buffer.writeInt(message.groupID);
    }

    public static void handle(final JoinGroupMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(()->{
                // TODO: FIXME:
                NetworkHooks.openGui(player, /* GuiGuid.GUI_GROUP_JOIN */);
            });
        ctx.get().setPacketHandled(true);
    }
}
