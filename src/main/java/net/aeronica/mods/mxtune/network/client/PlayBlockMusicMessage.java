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

import net.aeronica.mods.mxtune.config.MXTuneConfig;
import net.aeronica.mods.mxtune.managers.GroupHelper;
import net.aeronica.mods.mxtune.network.IMessage;
import net.aeronica.mods.mxtune.network.NetworkStringHelper;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.SoundRange;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import static net.aeronica.mods.mxtune.util.MIDISystemUtil.midiUnavailableWarn;

public class PlayBlockMusicMessage implements IMessage
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final int playID;
    private final BlockPos blockPos;
    private final String musicText;
    private final SoundRange soundRange;

    public PlayBlockMusicMessage(final int playID, final BlockPos blockPos, final String musicText, final SoundRange soundRange)
    {
        this.playID = playID;
        this.blockPos = blockPos;
        this.musicText = musicText;
        this.soundRange = soundRange;
    }

    public static PlayBlockMusicMessage decode(PacketBuffer buffer)
    {
        final int playID = buffer.readInt();
        final BlockPos blockPos = buffer.readBlockPos();
        final String musicText = NetworkStringHelper.readLongString(buffer);
        final SoundRange soundRange = buffer.readEnumValue(SoundRange.class);
        return new PlayBlockMusicMessage(playID, blockPos, musicText, soundRange);
    }

    public static void encode(final PlayBlockMusicMessage message, final PacketBuffer buffer)
    {
        buffer.writeInt(message.playID);
        buffer.writeBlockPos(message.blockPos);
        NetworkStringHelper.writeLongString(buffer, message.musicText);
        buffer.writeEnumValue(message.soundRange);
    }

    public static void handle(final PlayBlockMusicMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
            ctx.get().enqueueWork(() ->
                {
                    ServerPlayerEntity player = ctx.get().getSender();
                    if (player != null && !midiUnavailableWarn(player) && ClientCSDMonitor.canMXTunesPlay())
                    {
                        LOGGER.debug("musicText:  {}", message.musicText.substring(0, Math.min(25, message.musicText.length())));
                        LOGGER.debug("playID:     {}", message.playID);
                        LOGGER.debug("SoundRange: {}", message.soundRange);
                        GroupHelper.addServerManagedActivePlayID(message.playID);
                        ClientAudio.play(message.playID, message.blockPos, message.musicText, message.soundRange);
                    }
                    else if (MXTuneConfig.showWelcomeStatusMessage())
                        ClientCSDMonitor.sendErrorViaChat(player);
                });
        ctx.get().setPacketHandled(true);
    }
}
