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
import net.aeronica.mods.mxtune.gui.GuiPlaying;
import net.aeronica.mods.mxtune.mml.MMLManager;
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PlaySoloMessage extends AbstractMessage<PlaySoloMessage>
{

    String musicTitle;
    String musicText;
    String playerName;
    BlockPos pos;

    public PlaySoloMessage() {}

    public PlaySoloMessage(String playerName)
    {
        this.playerName = playerName;
        this.musicTitle = "";
        this.musicText = "";
        this.pos = new BlockPos(0, 0, 0);
    }

    public PlaySoloMessage(String playerName, String musicTitle, String musicText)
    {
        this.playerName = playerName;
        this.musicTitle = musicTitle;
        this.musicText = musicText;
        this.pos = new BlockPos(0, 0, 0);
    }

    public PlaySoloMessage(String playerName, BlockPos pos)
    {
        this.playerName = playerName;
        this.musicTitle = "";
        this.musicText = "";
        this.pos = pos;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        playerName = ByteBufUtils.readUTF8String(buffer);
        musicTitle = ByteBufUtils.readUTF8String(buffer);
        musicText = ByteBufUtils.readUTF8String(buffer);
        pos = new BlockPos(ByteBufUtils.readVarInt(buffer, 5), ByteBufUtils.readVarInt(buffer, 5), ByteBufUtils.readVarInt(buffer, 5));
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeUTF8String(buffer, playerName);
        ByteBufUtils.writeUTF8String(buffer, musicTitle);
        ByteBufUtils.writeUTF8String(buffer, musicText);
        ByteBufUtils.writeVarInt(buffer, pos.getX(), 5);
        ByteBufUtils.writeVarInt(buffer, pos.getY(), 5);
        ByteBufUtils.writeVarInt(buffer, pos.getZ(), 5);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side.isClient())
        {
            /**
             * Solo play format "<playerName|groupID>=MML@...;" Jam play format
             * just appends with a space between each player=MML sequence
             * "<playername1>=MML@...abcd; <playername2>=MML@...efga; <playername3>=MML@...bead;"
             */

            String mml = new String(playerName + "=" + musicText);
            MMLManager.getInstance().mmlPlay(mml, playerName, true);

            /** Only open the playing gui for the player who is playing */
            if (player.getDisplayName().getUnformattedText().equalsIgnoreCase(playerName))
            {
                player.openGui(MXTuneMain.instance, GuiPlaying.GUI_ID, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            }
        }
    }
}
