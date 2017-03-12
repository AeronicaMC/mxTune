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
package net.aeronica.mods.mxtune.network.server;

import java.io.IOException;

import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class MusicTextMessage extends AbstractServerMessage<MusicTextMessage>
{
    String musicTitle;
    String musicText;

    public MusicTextMessage() {}

    public MusicTextMessage(String musicTitle, String musicText)
    {
        this.musicTitle = musicTitle;
        this.musicText = musicText;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        musicTitle = ByteBufUtils.readUTF8String(buffer);
        musicText = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeUTF8String(buffer, musicTitle);
        ByteBufUtils.writeUTF8String(buffer, musicText);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side.isClient()) return;
        String mml = musicText.trim().toUpperCase();

        if (player.getHeldItemMainhand() != null)
        {
            ItemStack sheetMusic = new ItemStack(ModItems.ITEM_SHEET_MUSIC);

            sheetMusic.setStackDisplayName(musicTitle);
            NBTTagCompound compound = sheetMusic.getTagCompound();
            if (compound != null)
            {
                NBTTagCompound contents = new NBTTagCompound();
                contents.setString("MML", mml);
                compound.setTag("MusicBook", contents);
            }
            player.inventory.decrStackSize(player.inventory.currentItem, 1);
        
            if (!player.inventory.addItemStackToInventory(sheetMusic.copy()))
            {
                player.dropItem(sheetMusic, false, false);
            }
            player.inventoryContainer.detectAndSendChanges();
        }
    }
}
