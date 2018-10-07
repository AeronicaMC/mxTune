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

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.gui.GuiGroup;
import net.aeronica.mods.mxtune.gui.GuiMusicOptions;
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class SendKeyMessage extends AbstractMessage<SendKeyMessage>
{
    String keyBindingDesc;

    public SendKeyMessage() {/* Required by the PacketDispacher */}

    public SendKeyMessage(String kb) {this.keyBindingDesc = kb;}

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        this.keyBindingDesc = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeUTF8String(buffer, this.keyBindingDesc);
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
        if ("mxtune.key.openParty".equalsIgnoreCase(this.keyBindingDesc))
        {
            playerSP.openGui(MXTune.instance, GuiGroup.GUI_ID, playerSP.getEntityWorld(), 0, 0, 0);
        }
        if ("mxtune.key.openMusicOptions".equalsIgnoreCase(this.keyBindingDesc))
        {
            playerSP.openGui(MXTune.instance, GuiMusicOptions.GUI_ID, playerSP.getEntityWorld(), 0, 0, 0);
        }
    }

    public void handleServerSide(EntityPlayer playerMP)
    {
        PacketDispatcher.sendTo(new SendKeyMessage(this.keyBindingDesc), (EntityPlayerMP) playerMP);
    }

}
