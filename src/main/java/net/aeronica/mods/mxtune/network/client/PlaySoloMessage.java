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

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlaySoloMessage extends AbstractClientMessage<PlaySoloMessage>
{

    int entityID;
    String musicTitle;
    String musicText;
    BlockPos pos;
    boolean isPlaced;

    public PlaySoloMessage() {}
    
    public PlaySoloMessage(int entityID, String musicTitle, String musicText)
    {
        this(entityID, musicTitle, musicText, new BlockPos(0,0,0), false);
    }
    
    public PlaySoloMessage(Integer entityID, String playerName, String musicTitle, String musicText, boolean isPlaced)
    {
        this(entityID, musicTitle, musicText, new BlockPos(0,0,0), isPlaced);
    }
    
    public PlaySoloMessage(int entityID, String musicTitle, String musicText, BlockPos pos, boolean isPlaced)
    {
        this.entityID = entityID;
        this.musicTitle = musicTitle;
        this.musicText = musicText;
        this.pos = pos;
        this.isPlaced = isPlaced;
    }
    
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        entityID = ByteBufUtils.readVarInt(buffer, 5);
        musicTitle = ByteBufUtils.readUTF8String(buffer);
        musicText = ByteBufUtils.readUTF8String(buffer);
        pos = new BlockPos(ByteBufUtils.readVarInt(buffer, 5), ByteBufUtils.readVarInt(buffer, 5), ByteBufUtils.readVarInt(buffer, 5));
        isPlaced = (ByteBufUtils.readVarShort(buffer)==1);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeVarInt(buffer, entityID, 5);
        ByteBufUtils.writeUTF8String(buffer, musicTitle);
        ByteBufUtils.writeUTF8String(buffer, musicText);
        ByteBufUtils.writeVarInt(buffer, pos.getX(), 5);
        ByteBufUtils.writeVarInt(buffer, pos.getY(), 5);
        ByteBufUtils.writeVarInt(buffer, pos.getZ(), 5);
        ByteBufUtils.writeVarShort(buffer, isPlaced?1:0);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side == Side.CLIENT) {process_client(player, side);}
    }

    @SideOnly(Side.CLIENT)
    protected void process_client(EntityPlayer player, Side side)
    {
        if (MIDISystemUtil.getInstance().midiUnavailableWarn(player) == false)
        {
            if (MusicOptionsUtil.getMuteResult(player, (EntityPlayer) player.worldObj.getEntityByID(entityID)) == false)
            {
                ModLogger.debug("musicTitle: " + musicTitle);
                ModLogger.debug("musicText:  " + musicText.substring(0, (musicText.length() >= 25 ? 25 : musicText.length())));
                ModLogger.debug("entityID:   " + entityID);
                ModLogger.debug("pos:        " + pos);
                ModLogger.debug("isPlaced:   " + isPlaced);
                /*
                 * Solo play format "<playerName|groupID>=MML@...;" Jam play
                 * format just appends with a space between each player=MML
                 * sequence
                 * "<playername1>=MML@...abcd; <playername2>=MML@...efga; <playername3>=MML@...bead;"
                 */
                String mml = new String(entityID + "=" + musicText);
                ClientAudio.play(entityID, mml, pos);
            }
        }
    }
    
}
