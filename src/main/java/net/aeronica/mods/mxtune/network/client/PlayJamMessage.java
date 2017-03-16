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

public class PlayJamMessage extends AbstractClientMessage<PlayJamMessage>
{

    Integer leaderID;
    Integer playID;
    String jamMML;
    
    public PlayJamMessage() {/* Required by the PacketDispacher */}

    public PlayJamMessage(Integer leaderID, Integer playID, String jamMML)
    {
        this.leaderID = leaderID;
        this.playID = playID;
        this.jamMML = jamMML;
    }
    
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        leaderID = buffer.readInt();
        playID = buffer.readInt();
        jamMML = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        buffer.writeInt(leaderID);
        buffer.writeInt(playID);
        ByteBufUtils.writeUTF8String(buffer, jamMML);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        ModLogger.debug("PlayJamMessage#process");
        if (MIDISystemUtil.midiUnavailableWarn(player) == false)
        {
            if (!MusicOptionsUtil.isPlayerMuted(player, (EntityPlayer) player.getEntityWorld().getEntityByID(GROUPS.getMembersGroupLeader(leaderID))))
            {
                ModLogger.debug("musicText:  " + jamMML.substring(0, (jamMML.length() >= 25 ? 25 : jamMML.length())));
                ModLogger.debug("playID:     " + playID);
                ClientAudio.play(playID, jamMML);
            }
        }
    }

}
