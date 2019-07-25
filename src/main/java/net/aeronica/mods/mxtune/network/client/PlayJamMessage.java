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

import static net.aeronica.mods.mxtune.managers.GroupHelper.getMembersGroupLeader;
import static net.aeronica.mods.mxtune.options.MusicOptionsUtil.playerNotMuted;
import static net.aeronica.mods.mxtune.util.MIDISystemUtil.midiUnavailableWarn;

public class PlayJamMessage implements IMessage
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final int leaderID;
    private final int playID;
    private final String jamMML;

    public PlayJamMessage(final int leaderID, final int playID, String jamMML)
    {
        this.leaderID = leaderID;
        this.playID = playID;
        this.jamMML = jamMML;
    }

    public static PlayJamMessage decode(final PacketBuffer buffer)
    {
        final int leaderID = buffer.readInt();
        final int playID = buffer.readInt();
        final String jamMML = NetworkStringHelper.readLongString(buffer);
        return new PlayJamMessage(leaderID, playID, jamMML);
    }

    public static void encode(final PlayJamMessage message, final PacketBuffer buffer)
    {
        buffer.writeInt(message.leaderID);
        buffer.writeInt(message.playID);
        NetworkStringHelper.writeLongString(buffer, message.jamMML);
    }

    public static void handle(final PlayJamMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(() ->
                {
                    ServerPlayerEntity player = ctx.get().getSender();
                    if (player != null && !midiUnavailableWarn(player))
                    {
                        PlayerEntity otherPlayer = (PlayerEntity) player.getEntityWorld().getEntityByID(getMembersGroupLeader(message.leaderID));
                        if (playerNotMuted(player, otherPlayer))
                        {
                            LOGGER.debug("musicText: {}" ,message.jamMML.substring(0, Math.min(25, message.jamMML.length())));
                            LOGGER.debug("playID:    {}", message.playID);
                            GroupHelper.addServerManagedActivePlayID(message.playID);
                            ClientAudio.play(message.playID, message.jamMML);
                        }
                    }
                });
        ctx.get().setPacketHandled(true);
    }
}
