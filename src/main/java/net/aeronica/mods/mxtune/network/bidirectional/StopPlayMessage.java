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
package net.aeronica.mods.mxtune.network.bidirectional;

import java.io.IOException;

import net.aeronica.mods.mxtune.groups.PlayManager;
import net.aeronica.mods.mxtune.mml.MMLManager;
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StopPlayMessage extends AbstractMessage<StopPlayMessage>
{
    private Integer playID;

    public StopPlayMessage() {}

    public StopPlayMessage(Integer playID2) {this.playID = playID2;}

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        playID = ByteBufUtils.readVarInt(buffer, 5);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeVarInt(buffer, playID, 5);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side.isClient())
        {
//            handleClientSide(player);
        } else
        {
            handleServerSide(player);
        }
    }

    public void handleClientSide(EntityPlayer playerSP)
    {
        MMLManager.getInstance().mmlKill(playID, true);
        if (playerSP.getEntityId() == (playID))
        {
            /** TODO: Need to consider tracking isPlaced for each group member for dealing with GUI or Riding player rooting */
            ModLogger.debug("PacketPlayStop: try to close Gui for " + playID);
            Minecraft mc = Minecraft.getMinecraft();
            /** close the playing GUI */
            mc.displayGuiScreen((GuiScreen) null);
            mc.setIngameFocus();
        }
    }

    public void handleServerSide(EntityPlayer playerMP)
    {
        PlayManager.dequeueMember(playID);
        PacketDispatcher.sendToAll(new StopPlayMessage(playID));
    }
}
