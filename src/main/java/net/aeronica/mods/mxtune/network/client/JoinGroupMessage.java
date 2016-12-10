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
import net.aeronica.mods.mxtune.gui.GuiGroupJoin;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class JoinGroupMessage extends AbstractClientMessage<JoinGroupMessage>
{
    Integer groupID;

    public JoinGroupMessage() {}

    public JoinGroupMessage(Integer groupID) {this.groupID = groupID;}

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        groupID = ByteBufUtils.readVarInt(buffer, 5);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeVarInt(buffer, groupID, 5);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side.isClient())
        {
            player.openGui(MXTuneMain.instance, GuiGroupJoin.GUI_ID, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }
}
