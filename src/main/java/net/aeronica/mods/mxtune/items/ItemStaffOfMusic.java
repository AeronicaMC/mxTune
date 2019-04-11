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
import net.aeronica.mods.mxtune.gui.GuiGuid;
import net.aeronica.mods.mxtune.managers.ServerFileManager;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.world.chunk.ModChunkDataHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
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
            if (playerIn.isSneaking())
                playerIn.openGui(MXTune.instance, GuiGuid.GUI_AREA_MANAGER, worldIn, 0, 0, 0);

            return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
        }
        else if (!playerIn.isSneaking() && MusicOptionsUtil.isCtrlKeyDown(playerIn))
        {
            BlockPos pos = playerIn.getPosition();
            Chunk chunk = worldIn.getChunk(pos);
            GUID guidArea = MusicOptionsUtil.getSelectedAreaGuid(playerIn);
            if (chunk.hasCapability(ModChunkDataHelper.MOD_CHUNK_DATA, null) && MusicOptionsUtil.isMxTuneServerUpdateAllowed(playerIn))
            {
                ModChunkDataHelper.setAreaGuid(chunk, guidArea);
                ModChunkDataHelper.sync(playerIn, chunk);

                ModLogger.debug("Area name:", ServerFileManager.getArea(guidArea));
                ModLogger.debug("Area UUID: %s", guidArea);
                Miscellus.audiblePingPlayer(playerIn, SoundEvents.BLOCK_NOTE_PLING);
                playerIn.sendStatusMessage(new TextComponentTranslation("mxtune.gui.guiStaffOverlay.area_update_successful"), true);
            }
            else if (chunk.hasCapability(ModChunkDataHelper.MOD_CHUNK_DATA, null))
            {
                Miscellus.audiblePingPlayer(playerIn, SoundEvents.BLOCK_GLASS_BREAK);
                playerIn.sendStatusMessage(new TextComponentTranslation("commands.mxtune.mxtune_server_update_not_allowed"), true);
                ModLogger.debug("Player does not have rights to update Areas");
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }
        else
        {
            return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
        }
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
