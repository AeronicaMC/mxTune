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
import net.aeronica.mods.mxtune.network.NetworkStringHelper;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import static net.aeronica.mods.mxtune.caps.player.MusicOptionsUtil.playerNotMuted;
import static net.aeronica.mods.mxtune.managers.GroupHelper.getSoloMemberByPlayID;
import static net.aeronica.mods.mxtune.util.MIDISystemUtil.midiUnavailableWarn;

public class PlaySoloMessage implements IMessage
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final int playID;
    private final String musicText;
    
    public PlaySoloMessage(final int playID, final String musicText)
    {
        this.playID = playID;
        this.musicText = musicText;
    }

    public static PlaySoloMessage decode(final PacketBuffer buffer)
    {
        int playID = buffer.readInt();
        String musicText = NetworkStringHelper.readLongString(buffer);
        return new PlaySoloMessage(playID, musicText);
    }

    public static void encode(final PlaySoloMessage message, final PacketBuffer buffer)
    {
        buffer.writeInt(message.playID);
        NetworkStringHelper.writeLongString(buffer, message.musicText);
    }

    public static void handle(final PlaySoloMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(() ->
                {
                    ServerPlayerEntity player = ctx.get().getSender();
                    if (player != null && !midiUnavailableWarn(player))
                    {
                      /* This is messy, but we want to ensure we return a valid player entity ID */
                      Integer otherEntityID = (getSoloMemberByPlayID(message.playID) > 0) ? player.getEntityId() : getSoloMemberByPlayID(message.playID);
                      if (playerNotMuted(player, (PlayerEntity) (player.getEntityWorld().getEntityByID(otherEntityID))))
                      {
                          LOGGER.debug("musicText: {}" ,message.musicText.substring(0, Math.min(25, message.musicText.length())));
                          LOGGER.debug("playID:    {}", message.playID);
                          GroupHelper.addServerManagedActivePlayID(message.playID);
                          ClientAudio.play(message.playID, message.musicText);
                      }
                    }
                });
        ctx.get().setPacketHandled(true);
    }

}
