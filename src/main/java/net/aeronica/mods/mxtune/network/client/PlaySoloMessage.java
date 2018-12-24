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

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import static net.aeronica.mods.mxtune.groups.GROUPS.getSoloMemberByPlayID;
import static net.aeronica.mods.mxtune.options.MusicOptionsUtil.playerNotMuted;
import static net.aeronica.mods.mxtune.util.MIDISystemUtil.midiUnavailableWarn;

public class PlaySoloMessage extends AbstractClientMessage<PlaySoloMessage>
{

    private Integer playID;
    private String musicText;

    @SuppressWarnings("unused")
    public PlaySoloMessage() {/* Required by the PacketDispatcher */}
    
    public PlaySoloMessage(Integer playID, String musicText)
    {
        this.playID = playID;
        this.musicText = musicText;
    }
    
    @Override
    protected void read(PacketBuffer buffer)
    {
        playID = buffer.readInt();
        musicText = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeInt(playID);
        ByteBufUtils.writeUTF8String(buffer, musicText);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (!midiUnavailableWarn(player))
        {
            /* This is messy, but we want to ensure we return a valid player entity ID */
            Integer otherEntityID = getSoloMemberByPlayID(playID) == null ? player.getEntityId() : getSoloMemberByPlayID(playID);
            if (playerNotMuted(player, (EntityPlayer) (player.getEntityWorld().getEntityByID(otherEntityID))))
            {
                ModLogger.info("musicText: " + musicText.substring(0, (musicText.length() >= 25 ? 25 : musicText.length())));
                ModLogger.info("playID:    " + playID);
                ClientAudio.play(playID, musicText);
            }
        }
    }

}
