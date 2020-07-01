/*
 * Aeronica's mxTune MOD
 * Copyright {2020} Paul Boese a.k.a. Aeronica
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

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.advancements.ModCriteriaTriggers;
import net.aeronica.mods.mxtune.blocks.IPlacedInstrument;
import net.aeronica.mods.mxtune.gui.GuiGuid;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.managers.PlayManager;
import net.aeronica.mods.mxtune.status.ServerCSDManager;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.aeronica.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static net.aeronica.mods.mxtune.managers.PlayIdSupplier.PlayType;

/**
 * @author Paul Boese a.k.a Aeronica
 *
 */
public class ItemMultiInst extends Item implements IInstrument
{
    public ItemMultiInst()
    {
        setHasSubtypes(false);
        setMaxStackSize(1);
        setCreativeTab(MXTune.TAB_MUSIC);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + "." + SoundFontProxyManager.getName(stack.getItemDamage());
    }

    @Override
    public boolean getShareTag() {return true;}

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote)
        {
            // Server Side - Open the instrument inventory GuiInstInvAdjustRotations
            if (playerIn.isSneaking() && handIn.equals(EnumHand.MAIN_HAND))
            {
                playerIn.openGui(MXTune.instance, GuiGuid.GUI_MULTI_INST_INVENTORY, worldIn, 0, 0, 0);
            }
            if (!playerIn.isSneaking() && itemStackIn.hasTagCompound() && handIn.equals(EnumHand.MAIN_HAND))
            {
                if (ServerCSDManager.canMXTunesPlay(playerIn))
                {
                    if (!PlayManager.isPlayerPlaying(playerIn))
                    {
                        int playID = PlayManager.playMusic(playerIn);
                        itemStackIn.setRepairCost(playID);
                        if (playID != PlayType.INVALID)
                            ModCriteriaTriggers.PLAY_INSTRUMENT.trigger((EntityPlayerMP) playerIn, SoundFontProxyManager.getName(itemStackIn.getMetadata()));
                    }
                } 
                else
                {
                    ServerCSDManager.sendErrorViaChat(playerIn);
                }
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
    }

    /**
     * Off-hand (shield-slot) instrument will allow sneak-right click to remove music from a placed instrument.
     */
    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos, EntityPlayer player)
    {   
        return world.getBlockState(pos).getBlock() instanceof IPlacedInstrument;
    }
    
    /* 
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    @Override
    public void onUpdate(ItemStack stackIn, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (!worldIn.isRemote)
        {         
            int playID = stackIn.getRepairCost();
            if (!isSelected && (PlayManager.hasPlayID(playID)||PlayManager.isActivePlayID(playID)))
            {
                PlayManager.stopPlayID(playID);
                stackIn.setRepairCost(PlayType.INVALID);
            }
        }
    }
    
    /*
     * Called if moved from inventory into the world.
     * This is distinct from onDroppedByPlayer method
     * 
     */
    @Override
    public int getEntityLifespan(ItemStack stackIn, World worldIn)
    {
        if (!worldIn.isRemote)
        {
            int playID = stackIn.getRepairCost();
            if (PlayManager.hasPlayID(playID)||PlayManager.isActivePlayID(playID))
            {
                PlayManager.stopPlayID(playID);
                stackIn.setRepairCost(PlayType.INVALID);
            }
        }
        return super.getEntityLifespan(stackIn, worldIn);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack stackIn, EntityPlayer playerIn)
    {
        if (!playerIn.getEntityWorld().isRemote)
        {
            int playID = stackIn.getRepairCost();
            if (PlayManager.hasPlayID(playID)||PlayManager.isActivePlayID(playID))
            {
                PlayManager.stopPlayID(playID);
                stackIn.setRepairCost(PlayType.INVALID);
            }
        }
        return true;
    }

    /**
     * This is where we decide how our item interacts with other entities
     */
    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand)
    {
        return true;
    }

    /**
     * NOTE: If you want to open your GUI on right click and your ItemStore, you
     * MUST override getMaxItemUseDuration to return a value of at least 1,
     * otherwise you won't be able to open the GUI. That's just how it works.
     */
    @Override
    public int getMaxItemUseDuration(ItemStack itemstack)
    {
        return 72000;
    }

    @Override
    public void addInformation(ItemStack stackIn, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        String musicTitle = SheetMusicUtil.getMusicTitle(stackIn);
        if (!musicTitle.isEmpty())
            tooltip.add(TextFormatting.GREEN + I18n.format("item.mxtune:instrument.title") + ": " + musicTitle);
        
        tooltip.add(TextFormatting.RESET + I18n.format("item.mxtune:instrument.help"));
    }

    @Override
    public int getPatch(ItemStack itemStack)
    {
        return SoundFontProxyManager.getPackedPreset(itemStack.getItemDamage());
    }
}
