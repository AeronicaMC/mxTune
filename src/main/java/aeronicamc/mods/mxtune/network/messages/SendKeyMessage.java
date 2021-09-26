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
package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.gui.Handler;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Supplier;

public class SendKeyMessage extends AbstractMessage<SendKeyMessage>
{
    private static final Logger LOGGER = LogManager.getLogger();
    private String keyBindingDesc = "";

    public SendKeyMessage() { /* NOP */ }

    public SendKeyMessage(final String kb) { this.keyBindingDesc = kb; }

    @Override
    public SendKeyMessage decode(final PacketBuffer buffer)
    {
        String keyBindingDesc = buffer.readUtf(64);
        return new SendKeyMessage(keyBindingDesc);
    }

    @Override
    public void encode(final SendKeyMessage message, final PacketBuffer buffer)
    {
        buffer.writeUtf(message.keyBindingDesc, 64);
    }

    @Override
    public  void handle(final SendKeyMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (ctx.get().getDirection().getReceptionSide().isClient())
        {
            handleClientSide(message, ctx);
        } else
        {
            handleServerSide(message, ctx, Objects.requireNonNull(player));
        }
    }

    private static void handleClientSide(final SendKeyMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(()->{
            if ("fourteen.key.openParty".equalsIgnoreCase(message.keyBindingDesc))
            {
                Handler.openTestScreen();
            }
            if ("fourteen.key.openMusicOptions".equalsIgnoreCase(message.keyBindingDesc))
            {
                Handler.openTestScreen();
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleServerSide(final SendKeyMessage message, final Supplier<NetworkEvent.Context> ctx, final ServerPlayerEntity player)
    {
        ctx.get().enqueueWork(()->{
            if ("ctrl-down".equalsIgnoreCase(message.keyBindingDesc))
            {
                LOGGER.debug("ctrl-down");
                //MusicOptionsUtil.setCtrlKey(player, true);
            }
            else if ("ctrl-up".equalsIgnoreCase(message.keyBindingDesc))
            {
                LOGGER.debug("ctrl-up");
                //MusicOptionsUtil.setCtrlKey(player, false);
            }
            else
                PacketDispatcher.sendTo(new SendKeyMessage(message.keyBindingDesc), player);
        });
        ctx.get().setPacketHandled(true);
    }
}
