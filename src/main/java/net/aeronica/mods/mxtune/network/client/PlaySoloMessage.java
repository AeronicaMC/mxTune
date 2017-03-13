/**
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

import java.io.IOException;

import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PlaySoloMessage extends AbstractClientMessage<PlaySoloMessage>
{

    Integer playID;
    String musicText;

    public PlaySoloMessage() {/* Required by the PacketDispacher */}
    
    public PlaySoloMessage(Integer playID, String musicText)
    {
        this.playID = playID;
        this.musicText = musicText;
    }
    
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        playID = buffer.readInt();
        musicText = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        buffer.writeInt(playID);
        ByteBufUtils.writeUTF8String(buffer, musicText);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (MIDISystemUtil.midiUnavailableWarn(player) == false)
        {
            /* This is messy, but we want to ensure we return a valid player entity ID */
            Integer otherEntityID = GROUPS.getSoloMemberByPlayID(playID) == null ? player.getEntityId() : GROUPS.getSoloMemberByPlayID(playID);
            if (MusicOptionsUtil.getMuteResult(player, (EntityPlayer) (player.getEntityWorld().getEntityByID(otherEntityID))) == false)
            {
                ModLogger.debug("musicText: " + musicText.substring(0, (musicText.length() >= 25 ? 25 : musicText.length())));
                ModLogger.debug("playID:    " + playID);
                ClientAudio.play(playID, musicText);
            }
        }
    }

}
