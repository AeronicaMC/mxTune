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

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.gui.GuiGroup;
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class SendKeyMessage extends AbstractMessage<SendKeyMessage>
{
    public String keyBindingDesc;

    public SendKeyMessage() {}

    public SendKeyMessage(String kb) {keyBindingDesc = kb;}

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        keyBindingDesc = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeUTF8String(buffer, keyBindingDesc);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        /** Since this packet is handled differently, we need to check side */
        if (side.isServer())
        {
            handleServerSide(player);
        } else
        {
            handleClientSide(player);
        }
    }

    public void handleClientSide(EntityPlayer playerSP)
    {
        if (keyBindingDesc.equalsIgnoreCase("key.openParty"))
        {
            playerSP.openGui(MXTuneMain.instance, GuiGroup.GUI_ID, playerSP.worldObj, (int) playerSP.posX, (int) playerSP.posY, (int) playerSP.posZ);
        }
    }

    public void handleServerSide(EntityPlayer playerMP)
    {
        PacketDispatcher.sendTo(new SendKeyMessage(keyBindingDesc), (EntityPlayerMP) playerMP);
    }

}
