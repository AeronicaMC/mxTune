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
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.groups.GroupManager;
import net.aeronica.mods.mxtune.gui.GuiPlaying;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class QueueJamMessage extends AbstractClientMessage<QueueJamMessage>
{
    String musicTitle;
    String musicText;
    BlockPos pos;
    GroupManager GM = GroupManager.getInstance();

    public QueueJamMessage()
    {
        this.musicTitle = "";
        this.musicText = "";
        this.pos = new BlockPos(0, 0, 0);
    }

    public QueueJamMessage(BlockPos pos)
    {
        this.musicTitle = "";
        this.musicText = "";
        this.pos = pos;
    }

    public QueueJamMessage(String musicTitle, String musicText)
    {
        this.musicTitle = musicTitle;
        this.musicText = musicText;
        this.pos = new BlockPos(0, 0, 0);
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        musicTitle = ByteBufUtils.readUTF8String(buffer);
        musicText = ByteBufUtils.readUTF8String(buffer);
        pos = new BlockPos(ByteBufUtils.readVarInt(buffer, 5), ByteBufUtils.readVarInt(buffer, 5), ByteBufUtils.readVarInt(buffer, 5));
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
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
            /** Client side - Open GuiPlaying for JAM Members */
            //EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            EntityPlayer thePlayer = MXTuneMain.proxy.getClientPlayer();

            /**
             * Open the Play GUI on for the JAMMER who either queued his part or
             * the leader who kicks off the JAM
             */
            if (GROUPS.getMembersGroupID(player.getDisplayName().getUnformattedText()) != null)
            {

                if (thePlayer.getDisplayName().getUnformattedText().toLowerCase().contentEquals(player.getDisplayName().getUnformattedText().toLowerCase()))
                {
                    ModLogger.logInfo("PacketQueueJAM Client Side Packet - Open GuiPLaying!");
                    thePlayer.openGui(MXTuneMain.instance, GuiPlaying.GUI_ID, thePlayer.worldObj, (int) thePlayer.posX, (int) thePlayer.posY, (int) thePlayer.posZ);
                }
            }
        }
    }
}
