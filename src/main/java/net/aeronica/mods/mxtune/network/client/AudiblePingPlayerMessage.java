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

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;

public class AudiblePingPlayerMessage extends AbstractClientMessage<AudiblePingPlayerMessage>
{
    private SoundEvent soundEvent;

    @SuppressWarnings("unused")
    public AudiblePingPlayerMessage() {/* Required by the PacketDispatcher */}

    public AudiblePingPlayerMessage(SoundEvent soundEvent)
    {
        this.soundEvent = soundEvent;
    }
    
    @Override
    protected void read(PacketBuffer buffer)
    {
        soundEvent = SoundEvent.REGISTRY.getObjectById(buffer.readInt());
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeInt(SoundEvent.REGISTRY.getIDForObject(soundEvent));
    }

    @Override
    public void process(PlayerEntity player, Side side)
    {
        player.playSound(soundEvent, 1F, 1F);
    }
}
