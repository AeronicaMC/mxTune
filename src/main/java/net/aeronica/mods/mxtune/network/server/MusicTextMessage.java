/*
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

import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class MusicTextMessage extends AbstractServerMessage<MusicTextMessage>
{
    private String musicTitle;
    private String musicText;
    private NetworkStringHelper stringHelper = new NetworkStringHelper();

    @SuppressWarnings("unused")
    public MusicTextMessage() {/* Required by the PacketDispatcher */}

    public MusicTextMessage(String musicTitle, String musicText)
    {
        this.musicTitle = musicTitle;
        this.musicText = musicText;
    }

    @Override
    protected void read(PacketBuffer buffer)
    {
        musicTitle = ByteBufUtils.readUTF8String(buffer);
        musicText = stringHelper.readLongString(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, musicTitle);
        stringHelper.writeLongString(buffer, musicText);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side.isServer())
            processServer(player);
    }

    private void processServer(EntityPlayer player)
    {
        String mml = musicText.trim();

        if (!player.getHeldItemMainhand().isEmpty())
        {
            ItemStack sheetMusic = new ItemStack(ModItems.ITEM_SHEET_MUSIC);
            if (SheetMusicUtil.writeSheetMusic(sheetMusic, musicTitle, mml))
            {
                player.inventory.decrStackSize(player.inventory.currentItem, 1);
                if (!player.inventory.addItemStackToInventory(sheetMusic.copy()))
                    player.dropItem(sheetMusic, false, false);
            }
            else
            {
                player.sendStatusMessage(new TextComponentTranslation("mxtune.status.mml_server_side_validation_failure"), false);
                player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1F, 1F);
            }
            player.inventoryContainer.detectAndSendChanges();
        }
    }
}
