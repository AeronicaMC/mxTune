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

import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.groups.GroupHelper;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.network.server.NetworkStringHelper;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.SoundRange;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.aeronica.mods.mxtune.util.MIDISystemUtil.midiUnavailableWarn;

public class PlayBlockMusicMessage extends AbstractClientMessage<PlayBlockMusicMessage>
{
    private Integer playID;
    private BlockPos blockPos;
    private String musicText;
    private SoundRange soundRange;
    private NetworkStringHelper stringHelper = new NetworkStringHelper();

    @SuppressWarnings("unused")
    public PlayBlockMusicMessage() {/* Required by the PacketDispatcher */}

    public PlayBlockMusicMessage(Integer playID, BlockPos blockPos, String musicText, SoundRange soundRange)
    {
        this.playID = playID;
        this.blockPos = blockPos;
        this.musicText = musicText;
        this.soundRange = soundRange;
    }
    
    @Override
    protected void read(PacketBuffer buffer)
    {
        playID = buffer.readInt();
        blockPos = buffer.readBlockPos();
        musicText = stringHelper.readLongString(buffer);
        soundRange = buffer.readEnumValue(SoundRange.class);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeInt(playID);
        buffer.writeBlockPos(blockPos);
        stringHelper.writeLongString(buffer, musicText);
        buffer.writeEnumValue(soundRange);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side.isClient())
            handleClient(player);
    }

    @SideOnly(Side.CLIENT)
    private void handleClient(EntityPlayer player)
    {
        if (!midiUnavailableWarn(player) && ClientCSDMonitor.canMXTunesPlay())
        {
            ModLogger.debug("musicText:  " + musicText.substring(0, Math.min(25, musicText.length())));
            ModLogger.debug("playID:     " + playID);
            ModLogger.debug("SoundRange: " + soundRange);
            GroupHelper.addServerManagedActivePlayID(playID);
            ClientAudio.play(playID, blockPos, musicText, soundRange);
        }
        else if (ModConfig.showWelcomeStatusMessage())
            ClientCSDMonitor.sendErrorViaChat(player);
    }
}
