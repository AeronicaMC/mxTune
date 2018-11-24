/*
 * Aeronica's mxTune MOD
 * Copyright {2018} Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.inventory;

import net.aeronica.mods.mxtune.blocks.TileBandAmp;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class ContainerBandAmp extends Container
{
    private TileBandAmp tileBandAmp;

    public ContainerBandAmp(InventoryPlayer playerInv, World worldIn, int x, int y, int z) {
        this.tileBandAmp = (TileBandAmp) worldIn.getTileEntity(new BlockPos(x, y, z));
        IItemHandler inventory = tileBandAmp.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 0, 52, 26));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 1, 70, 26));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 2, 88, 26));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 3, 106, 26));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 4, 52, 44));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 5, 70, 44));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 6, 88, 44));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 7, 106, 44));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            addSlotToContainer(new Slot(playerInv, k, 8 + k * 18, 142));
        }
        //setDuration();
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            int containerSlots = inventorySlots.size() - playerIn.inventory.mainInventory.size();

            if (index < containerSlots) {
                if (!this.mergeItemStack(itemstack1, containerSlots, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, containerSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn)
    {
        return this.tileBandAmp.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        ItemStack stack = super.slotClick(slotId, dragType, clickTypeIn, player);
        setDuration();
        return stack;
    }

    private void setDuration()
    {
        int dur = (getDuration(this.inventorySlots));
        this.tileBandAmp.setDuration(dur);
    }

    private static int getDuration(List<Slot> inventory)
    {
        int duration = 0;

        for (Slot slot: inventory)
        {
            ItemStack stackInSlot = slot.getStack();
            if((slot instanceof SlotBandAmp) && (stackInSlot.getItem() instanceof ItemInstrument))
            {
                ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(stackInSlot);
                if (!sheetMusic.isEmpty() && sheetMusic.getTagCompound() != null)
                {
                    NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("SheetMusic");
                    if (contents.hasKey("MML"))
                    {
                        int durationSheet = contents.getInteger("Duration");
                        if (durationSheet > duration) duration = durationSheet;
                    }
                }
            }
        }
        return duration;
    }
}
