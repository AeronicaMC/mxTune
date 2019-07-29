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
import net.aeronica.mods.mxtune.items.ItemMusicPaper;
import net.aeronica.mods.mxtune.network.NetworkStringHelper;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;


public class MusicTextMessage
{
    private final String musicTitle;
    private final String musicText;

    public MusicTextMessage(String musicTitle, String musicText)
    {
        this.musicTitle = musicTitle;
        this.musicText = musicText;
    }

    public static MusicTextMessage decode(PacketBuffer buffer)
    {
        String musicTitle = buffer.readString();
        String musicText = NetworkStringHelper.readLongString(buffer);
        return new MusicTextMessage(musicTitle, musicText);
    }

    public static void encode(final MusicTextMessage message, final PacketBuffer buffer)
    {
        buffer.writeString(message.musicTitle);
        NetworkStringHelper.writeLongString(buffer, message.musicText);
    }

    public static void handle(final MusicTextMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && ctx.get().getDirection().getReceptionSide().isServer()) ctx.get().enqueueWork(()->{
            if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemMusicPaper)
            {
                ItemStack sheetMusic = new ItemStack(ModItems.ITEM_SHEET_MUSIC);
                if (SheetMusicUtil.writeSheetMusic(sheetMusic,message.musicTitle, message.musicText))
                {
                    player.inventory.decrStackSize(player.inventory.currentItem, 1);
                    if (!player.inventory.addItemStackToInventory(sheetMusic.copy()))
                        player.dropItem(sheetMusic, false, false);
                }
                else
                {
                    player.sendStatusMessage(new TranslationTextComponent("mxtune.status.mml_server_side_validation_failure"), false);
                    Miscellus.audiblePingPlayer(player, SoundEvents.BLOCK_ANVIL_PLACE);
                }
                player.container.detectAndSendChanges();
            }
            else
            {
                player.sendStatusMessage(new TranslationTextComponent("mxtune.status.mml_server_side_music_paper_supply_empty"), false);
                Miscellus.audiblePingPlayer(player, SoundEvents.BLOCK_ANVIL_PLACE);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
