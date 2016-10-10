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

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.MovingMusicRegistered;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NewPlaySoloMessage extends AbstractClientMessage<NewPlaySoloMessage>
{

    String musicTitle;
    String musicText;
    String playerName;
    int entityID;
    BlockPos pos;
    boolean isPlaced;

    public NewPlaySoloMessage() {}

    public NewPlaySoloMessage(int entityID, String playerName)
    {
        this.playerName = playerName;
        this.musicTitle = "";
        this.musicText = "";
        this.entityID = entityID;
        this.pos = new BlockPos(0, 0, 0);
        this.isPlaced = false;
    }

    public NewPlaySoloMessage(int entityID, String playerName, String musicTitle, String musicText, boolean isPlaced)
    {
        this.playerName = playerName;
        this.musicTitle = musicTitle;
        this.musicText = musicText;
        this.entityID = entityID;
        this.pos = new BlockPos(0, 0, 0);
        this.isPlaced = isPlaced;
    }

    public NewPlaySoloMessage(int entityID, String playerName, BlockPos pos, boolean isPlaced)
    {
        this.playerName = playerName;
        this.musicTitle = "";
        this.musicText = "";
        this.entityID = entityID;
        this.pos = pos;
        this.isPlaced = isPlaced;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        playerName = ByteBufUtils.readUTF8String(buffer);
        musicTitle = ByteBufUtils.readUTF8String(buffer);
        musicText = ByteBufUtils.readUTF8String(buffer);
        entityID = ByteBufUtils.readVarInt(buffer, 5);
        pos = new BlockPos(ByteBufUtils.readVarInt(buffer, 5), ByteBufUtils.readVarInt(buffer, 5), ByteBufUtils.readVarInt(buffer, 5));
        isPlaced = (ByteBufUtils.readVarShort(buffer)==1);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeUTF8String(buffer, playerName);
        ByteBufUtils.writeUTF8String(buffer, musicTitle);
        ByteBufUtils.writeUTF8String(buffer, musicText);
        ByteBufUtils.writeVarInt(buffer, entityID, 5);
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
                ModLogger.debug("playerName: " + playerName);
                ModLogger.debug("musicTitle: " + musicTitle);
                ModLogger.debug("musicText:  " + musicText.substring(0, (musicText.length() >= 25 ? 25 : musicText.length())));
                ModLogger.debug("entityID:   " + entityID);
                ModLogger.debug("pos:        " + pos);
                ModLogger.debug("isPlaced:   " + isPlaced);
                ClientAudio.play(entityID, musicText, pos, isPlaced);
                MXTuneMain.proxy.getMinecraft().getSoundHandler().playSound(new MovingMusicRegistered((EntityPlayer) player.getEntityWorld().getEntityByID(this.entityID)));
            }
        }
    }
}
