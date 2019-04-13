/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.items;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.gui.GuiGuid;
import net.aeronica.mods.mxtune.managers.ServerFileManager;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.world.caps.chunk.ModChunkPlaylistHelper;
import net.aeronica.mods.mxtune.world.caps.world.ModWorldPlaylistHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.List;

public class ItemStaffOfMusic extends Item
{
    public ItemStaffOfMusic()
    {
        this.setMaxStackSize(1);
        this.setCreativeTab(MXTune.TAB_MUSIC);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        if (worldIn.isRemote)
        {
            openGui(worldIn, playerIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }
        else
        {
            BlockPos pos = playerIn.getPosition();
            Chunk chunk = worldIn.getChunk(pos);
            GUID playlist = MusicOptionsUtil.getSelectedAreaGuid(playerIn);
            if (checkPermissionServer(playerIn, chunk))
            {
                applyToChunkOnCtrlRightClick(playerIn, chunk, playlist);
                applyToWorldOnSneakCtrlRightClick(playerIn, worldIn, playlist);
            }
            else
            {
                ModLogger.debug("Player does not have rights to update Chunk or World playlists.");
                notifyPlayer(playerIn, playlist, SoundEvents.BLOCK_GLASS_BREAK, "commands.mxtune.mxtune_server_update_not_allowed");
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    private void openGui(World worldIn, EntityPlayer playerIn)
    {
        if (playerIn.isSneaking() && !MusicOptionsUtil.isCtrlKeyDown(playerIn))
                playerIn.openGui(MXTune.instance, GuiGuid.GUI_AREA_MANAGER, worldIn, 0, 0, 0);
    }

    private boolean checkPermissionServer(EntityPlayer playerIn, Chunk chunk)
    {
        return chunk.hasCapability(ModChunkPlaylistHelper.MOD_CHUNK_DATA, null) && MusicOptionsUtil.isMxTuneServerUpdateAllowed(playerIn);
    }

    private void applyToChunkOnCtrlRightClick(EntityPlayer playerIn, Chunk chunk, GUID playlist)
    {
        if (chunk.hasCapability(ModChunkPlaylistHelper.MOD_CHUNK_DATA, null) && !playerIn.isSneaking() && MusicOptionsUtil.isCtrlKeyDown(playerIn))
        {
            ModChunkPlaylistHelper.setPlaylistGuid(chunk, playlist);
            ModChunkPlaylistHelper.sync(playerIn, chunk);
            notifyPlayer(playerIn, playlist, SoundEvents.BLOCK_NOTE_PLING, "mxtune.gui.guiStaffOverlay.chunk_update_successful");
        }
    }

    private void applyToWorldOnSneakCtrlRightClick(EntityPlayer playerIn, World worldIn, GUID playlist)
    {
        if (worldIn.hasCapability(ModWorldPlaylistHelper.MOD_WORLD_DATA, null) && playerIn.isSneaking() && MusicOptionsUtil.isCtrlKeyDown(playerIn) &&
                !Reference.NO_MUSIC_GUID.equals(playlist))
        {
            ModWorldPlaylistHelper.setPlaylistGuid(worldIn, playlist);
            ModWorldPlaylistHelper.sync(playerIn, worldIn);
            notifyPlayer(playerIn, playlist, SoundEvents.BLOCK_NOTE_PLING, "mxtune.gui.guiStaffOverlay.world_update_successful");
        }
        else
            notifyPlayer(playerIn, playlist, SoundEvents.BLOCK_GLASS_BREAK, "mxtune.gui.guiStaffOverlay.world_update.cannot_apply_empty_playlist_to_worlds");
    }

    private void notifyPlayer(EntityPlayer playerIn, GUID playlist, SoundEvent soundEvent, String translationKey)
    {
        ModLogger.debug("Playlist name:", ServerFileManager.getArea(playlist));
        ModLogger.debug("Playlist GUID: %s", playlist);
        Miscellus.audiblePingPlayer(playerIn, soundEvent);
        playerIn.sendStatusMessage(new TextComponentTranslation(translationKey), true);
    }

    @Override
    public boolean getShareTag() {return true;}

    @Override
    public int getMaxItemUseDuration(ItemStack itemstack) {return 1;}

    @Override
    public void addInformation(ItemStack stackIn, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(TextFormatting.RESET + I18n.format("item.mxtune:staff_of_music.help"));
    }
}
