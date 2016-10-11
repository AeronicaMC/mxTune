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
package net.aeronica.mods.mxtune.items;

import java.util.List;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.gui.GuiInstInvExp;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.sound.IPlayStatus;
import net.aeronica.mods.mxtune.sound.NewPlayManager;
import net.aeronica.mods.mxtune.sound.PlayStatusUtil;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BasicItem extends ItemBase
{
    public BasicItem(String itemName)
    {
        super(itemName);
        setCreativeTab(MXTuneMain.TAB_MUSIC);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        BlockPos pos = new BlockPos((int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
        if (!worldIn.isRemote)
        {
            /** Server Side - Open the instrument inventory GuiInstInvAdjustRotations */
            IPlayStatus playing = playerIn.getCapability(PlayStatusUtil.PLAY_STATUS, null);
            if (playerIn.isSneaking() && hand.equals(EnumHand.MAIN_HAND))
            {
                playerIn.openGui(MXTuneMain.instance, GuiInstInvExp.GUI_ID, worldIn, 0,0,0);
            }
            else
            {
                if (playing.isPlaying() == false)
                {
                    playing.setPlaying(playerIn, true);
                    itemStackIn.setRepairCost(playerIn.inventory.currentItem+1000);
                    MusicOptionsUtil.setSParams(playerIn, "76", "", "");
                    NewPlayManager.playMusic(playerIn, pos, false);
                }
            }
        } else
        {
            // Client Side - nothing to do
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    }

    /* 
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (!worldIn.isRemote)
        {
            if (entityIn.hasCapability(PlayStatusUtil.PLAY_STATUS, null))
            {
                IPlayStatus playing = entityIn.getCapability(PlayStatusUtil.PLAY_STATUS, null);
                if (stack.getRepairCost() == (itemSlot + 1000) && !isSelected)
                {
                    stack.setRepairCost(0);
                    playing.setPlaying((EntityPlayer) entityIn, false);
                }
            }
        }
    }
    
    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        // return EnumActionResult.SUCCESS to activate on AIR only
        // return EnumActionResult.FAIL to activate unconditionally and skip
        // vanilla processing
        // return EnumActionResult.PASS to activate on AIR, or let Vanilla
        // process
        return EnumActionResult.PASS;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addInformation(ItemStack stackIn, EntityPlayer playerIn, List tooltip, boolean advanced)
    {
        String musicTitle = SheetMusicUtil.getMusicTitle(stackIn);
        if (!musicTitle.isEmpty())
        {
            tooltip.add(TextFormatting.GREEN + "Title: " + musicTitle);
        }
    }

    /** In order for clicking to work this needs to be at least 1. */
    @Override
    public int getMaxItemUseDuration(ItemStack itemstack) {return 1;}
}
