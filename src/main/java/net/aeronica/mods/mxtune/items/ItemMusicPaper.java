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
import net.aeronica.mods.mxtune.gui.GuiMusicPaperParse;
import net.aeronica.mods.mxtune.inventory.IMusic;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** TODO: Swapping the icon is a nice trick, but I think a distinct item for Sheet Music would make dealing with item testing easier */
public class ItemMusicPaper extends Item implements IMusic
{
    public ItemMusicPaper()
    {
        this.setMaxStackSize(16);
        this.setCreativeTab(MXTuneMain.TAB_MUSIC);
        this.addPropertyOverride(new ResourceLocation("written"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return (stack != null) && isItemMusicPaper(stack) && stack.hasDisplayName() ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (worldIn.isRemote)
        {
            /** Client side */
            if (!itemStackIn.hasDisplayName() && hand.equals(EnumHand.MAIN_HAND))
            {
                playerIn.openGui(MXTuneMain.instance, GuiMusicPaperParse.GUI_ID, worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
            }
        }
        playerIn.setActiveHand(EnumHand.MAIN_HAND);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    }

    ItemStack getHeldItemStack(EntityPlayer player)
    {
        if (this.isItemMusicPaper(player.getHeldItem(EnumHand.OFF_HAND)))
        {
            return player.getHeldItem(EnumHand.OFF_HAND);
        } else if (this.isItemMusicPaper(player.getHeldItem(EnumHand.MAIN_HAND)))
        {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        } else
            return null;
    }

    @Override
    public boolean getShareTag() {return true;}

    /** Check of the item stack we are holding is the type we are interested in */
    protected boolean isItemMusicPaper(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemMusicPaper;
    }

    /**
     * NOTE: If you want to open your gui on right click and your ItemStore, you
     * MUST override getMaxItemUseDuration to return a value of at least 1,
     * otherwise you won't be able to open the Gui. That's just how it works.
     */
    @Override
    public int getMaxItemUseDuration(ItemStack itemstack) {return 1;}

    @Override
    public boolean hasMML(ItemStack itemStackIn)
    {
        if (itemStackIn != null)
        {
            if (itemStackIn.hasTagCompound())
            {
                NBTTagCompound contents = itemStackIn.getTagCompound();
                if (contents.hasKey("MusicBook"))
                {
                    NBTTagCompound mml = contents.getCompoundTag("MusicBook");
                    return mml.getString("MML").contains("MML@");
                }
            }
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addInformation(ItemStack stackIn, EntityPlayer playerIn, List tooltip, boolean advanced)
    {
        if (stackIn == null) return;
        /** Display the contents of the sheet music. */
        if (stackIn.hasTagCompound())
        {
            NBTTagCompound contents = stackIn.getTagCompound();
            if (contents.hasKey("MusicBook"))
            {
                NBTTagCompound mml = contents.getCompoundTag("MusicBook");
                if (mml.getString("MML").contains("MML@"))
                {
                    tooltip.add(TextFormatting.RED + "The old Sheet Music Item has been Depricated!");
                    tooltip.add(TextFormatting.RED + "Please convert to the New Sheet Music Item using the " + TextFormatting.YELLOW + "\'Sheet Music Converter\'");
                    tooltip.add(TextFormatting.RED + "Enclosed MML: ");
                    tooltip.add(TextFormatting.RED + mml.getString("MML").substring(0, mml.getString("MML").length() > 25 ? 25 : mml.getString("MML").length()));
                }
            }
        }
    }
}
