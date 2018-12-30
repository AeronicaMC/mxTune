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
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
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
        // simplistic handling of large string
        String mtFirst = buffer.readString(32768);
        String mtSecond = buffer.readString(32768);
        musicText = mtFirst + mtSecond;
        soundRange = buffer.readEnumValue(SoundRange.class);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeInt(playID);
        buffer.writeBlockPos(blockPos);
        // TODO: Simplistic handling of large string. Make intelligent to handle strings up to 100K (10 parts at 10K per part).
        // To handle large multipart tunes like Maple Story 2 MML the system will need to deal with large strings.
        int len01 = musicText.length() /2;
        String mtFirst = musicText.substring(0, len01);
        String mtSecond = musicText.substring(len01, musicText.length());
        String test = mtFirst + mtSecond;
        ModLogger.info("Lengths: musicText: %d, 1/2: %d, mtFirst: %d, mtSecond: %d: tot: %d, matches: %s",
                       musicText.length(), len01, mtFirst.length(), mtSecond.length(), mtFirst.length() + mtSecond.length(), musicText.equals(test));
        buffer.writeString(mtFirst);
        buffer.writeString(mtSecond);
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
            ModLogger.info("musicText:  " + musicText.substring(0, (musicText.length() >= 25 ? 25 : musicText.length())));
            ModLogger.info("playID:     " + playID);
            ModLogger.info("SoundRange: " + soundRange);
            ClientAudio.play(playID, blockPos, musicText, soundRange);
        }
        else if (ModConfig.showWelcomeStatusMessage())
            ClientCSDMonitor.sendErrorViaChat(player);
    }
}
