/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.network.bidirectional;

import net.aeronica.mods.mxtune.network.IMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Objects;
import java.util.function.Supplier;

public class SendKeyMessage implements IMessage
{
    private final String keyBindingDesc;

    public SendKeyMessage(final String kb) { this.keyBindingDesc = kb; }

    public static SendKeyMessage decode(PacketBuffer buffer)
    {
        String keyBindingDesc = buffer.readString();
        return new SendKeyMessage(keyBindingDesc);
    }

    public static void encode(final SendKeyMessage message, final PacketBuffer buffer)
    {
        buffer.writeString(message.keyBindingDesc);
    }

    public static void handle(final SendKeyMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (ctx.get().getDirection().getReceptionSide().isClient())
        {
            handleClientSide(message, ctx, Objects.requireNonNull(player));
        } else
        {
            handleServerSide(message, ctx, Objects.requireNonNull(player));
        }
    }

    private static void handleClientSide(final SendKeyMessage message, final Supplier<NetworkEvent.Context> ctx, ServerPlayerEntity player)
    {
        ctx.get().enqueueWork(()->{
            if ("mxtune.key.openParty".equalsIgnoreCase(message.keyBindingDesc))
            {
                NetworkHooks.openGui(player, );
            }
            if ("mxtune.key.openMusicOptions".equalsIgnoreCase(message.keyBindingDesc))
            {
                NetworkHooks.openGui(player, );
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleServerSide(final SendKeyMessage message, final Supplier<NetworkEvent.Context> ctx, final ServerPlayerEntity player)
    {
        ctx.get().enqueueWork(()->{
            if ("ctrl-down".equalsIgnoreCase(message.keyBindingDesc))
                MusicOptionsUtil.setCtrlKey(player, true);
            else if ("ctrl-up".equalsIgnoreCase(message.keyBindingDesc))
                MusicOptionsUtil.setCtrlKey(player, false);
            else
                PacketDispatcher.sendTo(new SendKeyMessage(message.keyBindingDesc), player);
        });
        ctx.get().setPacketHandled(true);
    }
}
