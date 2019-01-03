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
package net.aeronica.mods.mxtune.items;

import net.aeronica.libs.mml.core.MMLUtil;
import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.advancements.ModCriteriaTriggers;
import net.aeronica.mods.mxtune.blocks.IPlacedInstrument;
import net.aeronica.mods.mxtune.groups.PlayManager;
import net.aeronica.mods.mxtune.gui.GuiInstrumentInventory;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.status.ServerCSDManager;
import net.aeronica.mods.mxtune.util.IVariant;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Paul Boese a.k.a Aeronica
 *
 */
public class ItemInstrument extends Item implements IInstrument
{
    public ItemInstrument()
    {
        setHasSubtypes(true);
        setMaxStackSize(1);
        setMaxDamage(0);
        setCreativeTab(MXTune.TAB_MUSIC);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + "." + EnumType.byMetadata(stack.getMetadata()).getName();
    }

    @Override
    public boolean getShareTag() {return true;}

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if (isInCreativeTab(tab)) {
            final List<ItemStack> items = Stream.of(EnumType.values())
                    .map(enumType -> new ItemStack(this, 1, enumType.getMeta()))
                    .collect(Collectors.toList());

            subItems.addAll(items);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote)
        {
            // Server Side - Open the instrument inventory GuiInstInvAdjustRotations
            if (playerIn.isSneaking() && handIn.equals(EnumHand.MAIN_HAND))
            {
                playerIn.openGui(MXTune.instance, GuiInstrumentInventory.GUI_ID, worldIn, 0, 0, 0);
            }
            if (!playerIn.isSneaking() && itemStackIn.hasTagCompound() && handIn.equals(EnumHand.MAIN_HAND))
            {
                if (ServerCSDManager.canMXTunesPlay(playerIn))
                {
                    if (!PlayManager.isPlayerPlaying(playerIn))
                    {
                        Integer playID = PlayManager.playMusic(playerIn);
                        itemStackIn.setRepairCost(playID != null ? playID : -1);
                        if (playID != null)
                            ModCriteriaTriggers.PLAY_INSTRUMENT.trigger((EntityPlayerMP) playerIn, EnumType.byMetadata(itemStackIn.getMetadata()).getName());
                    }
                } 
                else
                {
                    ServerCSDManager.sendErrorViaChat(playerIn);
                }
            }
        }
        // return EnumActionResult.SUCCESS to activate on AIR only
        // return EnumActionResult.FAIL to activate unconditionally and skip vanilla processing
        // return EnumActionResult.PASS to activate on AIR, or let Vanilla process
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
            Integer playID = stackIn.getRepairCost();
            if (!isSelected && (PlayManager.hasPlayID(playID)||PlayManager.isActivePlayID(playID)))
            {
                PlayManager.stopPlayID(playID);
                stackIn.setRepairCost(-1);
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
            Integer playID = stackIn.getRepairCost();
            if (PlayManager.hasPlayID(playID)||PlayManager.isActivePlayID(playID))
            {
                PlayManager.stopPlayID(playID);
                stackIn.setRepairCost(-1);
            }
        }
        return super.getEntityLifespan(stackIn, worldIn);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack stackIn, EntityPlayer playerIn)
    {
        if (!playerIn.getEntityWorld().isRemote)
        {
            Integer playID = stackIn.getRepairCost();
            if (PlayManager.hasPlayID(playID)||PlayManager.isActivePlayID(playID))
            {
                PlayManager.stopPlayID(playID);
                stackIn.setRepairCost(-1);
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
        return EnumType.byMetadata(this.getMetadata(itemStack)).getPatch();
    }

    public enum EnumType implements IVariant
    {
        LUTE(0, "lute", 0),
        UKULELE(1, "ukulele", 1),
        MANDOLIN(2, "mandolin", 2),
        WHISTLE(3, "whistle", 3),
        RONCADORA(4, "roncadora", 4),
        FLUTE(5, "flute", 5),
        CHALUMEAU(6, "chalumeau", 6),
        TUBA(7, "tuba", 18),
        LYRE(8, "lyre", 19),
        ELECTRIC_GUITAR(9, "electric_guitar", 20),
        VIOLIN(10, "violin", 22),
        CELLO(11, "cello", 23),
        HARP(12, "harp", 24),
        TUNED_FLUTE(13, "tuned_flute", 55),
        TUNED_WHISTLE(14, "tuned_whistle", 56),
        BASS_DRUM(15, "bass_drum", 66),
        SNARE_DRUM(16, "snare_drum", 67),
        CYMBELS(17, "cymbels", 68),
        HAND_CHIMES(18, "hand_chimes", 77),
        RECORDER(19, "recorder", MMLUtil.preset2PackedPreset(16, 74)),
        TRUMPET(20, "trumpet", MMLUtil.preset2PackedPreset(16, 56)),
        HARPSICHORD(21, "harpsichord", MMLUtil.preset2PackedPreset(16, 6)),
        HARPSICHORD_COUPLED(22, "harpsichord_coupled", MMLUtil.preset2PackedPreset(16, 7)),
        STANDARD_SET(23, "standard_set", MMLUtil.preset2PackedPreset(128, 0)),
        ORCHESTRA_SET(24, "orchestra_set", MMLUtil.preset2PackedPreset(128, 48)),
        PIANO(25, "piano", 21),
        ;

        @Override
        public String toString() {return this.name;}

        public static EnumType byMetadata(int metaIn)
        {
            int metaLocal = metaIn;
            if (metaLocal < 0 || metaLocal >= META_LOOKUP.length) {metaLocal = 0;}
            return META_LOOKUP[metaLocal];
        }

        public String getName() {return this.name;}

        public int getPatch() {return this.patch;}

        private final int meta;
        private final String name;
        private final int patch;
        private static final EnumType[] META_LOOKUP = new EnumType[values().length];

        EnumType(int metaIn, String nameIn, int patchIn)
        {
            this.meta = metaIn;
            this.name = nameIn;
            this.patch = patchIn;
        }

        static
        {
            for (EnumType value : values())
            {
                META_LOOKUP[value.getMeta()] = value;
            }
        }

        @Override
        public int getMeta()
        {
            return meta;
        }
    }
}
