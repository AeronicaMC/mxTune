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

import net.aeronica.mods.mxtune.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class AudiblePingPlayerMessage implements IMessage
{
    private final SoundEvent soundEvent;

    public AudiblePingPlayerMessage(final SoundEvent soundEvent)
    {
        this.soundEvent = soundEvent;
    }

    public static AudiblePingPlayerMessage decode(PacketBuffer buffer)
    {
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(buffer.readResourceLocation());
        return new AudiblePingPlayerMessage(soundEvent != null ? soundEvent : SoundEvents.BLOCK_NOTE_BLOCK_PLING);
    }

    public static void encode(final AudiblePingPlayerMessage message, final PacketBuffer buffer)
    {
        buffer.writeResourceLocation(ForgeRegistries.SOUND_EVENTS.getKey(message.soundEvent));
    }

    public static void handle(final AudiblePingPlayerMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(() -> player.playSound(message.soundEvent, 1F, 1F));
        ctx.get().setPacketHandled(true);
    }
}
