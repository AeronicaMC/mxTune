/*
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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

import net.aeronica.mods.mxtune.network.bidirectional.ClientStateDataMessage;
import net.aeronica.mods.mxtune.status.CSDChatStatus;
import net.aeronica.mods.mxtune.status.ClientStateData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SendCSDChatMessage
{
    private final ClientStateData csd;
    
    public SendCSDChatMessage(final ClientStateData csd) {this.csd = csd;}

    public static SendCSDChatMessage decode(final PacketBuffer buffer)
    {
        ClientStateData csd = ClientStateDataMessage.readCSD(buffer);
        return new SendCSDChatMessage(csd);
    }

    public static void encode(final SendCSDChatMessage message, final PacketBuffer buffer)
    {
        ClientStateDataMessage.writeCSD(buffer, message.csd);
    }

    public static void handle(final SendCSDChatMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(() -> new CSDChatStatus(player, message.csd));
        ctx.get().setPacketHandled(true);
    }
}
