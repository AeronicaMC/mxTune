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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static net.aeronica.mods.mxtune.util.SheetMusicUtil.*;

public class ContainerBandAmp extends Container
{
    private TileBandAmp tileBandAmp;

    public ContainerBandAmp(PlayerInventory playerInv, World worldIn, int x, int y, int z) {
        this.tileBandAmp = (TileBandAmp) worldIn.getTileEntity(new BlockPos(x, y, z));

        IItemHandler inventory = Objects.requireNonNull(tileBandAmp).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 0, 52, 17));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 1, 70, 17));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 2, 88, 17));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 3, 106, 17));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 4, 52, 35));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 5, 70, 35));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 6, 88, 35));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 7, 106, 35));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 8, 52, 53));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 9, 70, 53));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 10, 88, 53));
        addSlotToContainer(new SlotBandAmp(inventory, this.tileBandAmp, 11, 106, 53));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            addSlotToContainer(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {

        ItemStack itemStackCopy = ItemStack.EMPTY;
        // *** only allow owners to manage instruments ***
        if (!tileBandAmp.isOwner(playerIn)) return itemStackCopy;

        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStackSlot = slot.getStack();
            itemStackCopy = itemStackSlot.copy();

            int containerSlots = inventorySlots.size() - playerIn.inventory.mainInventory.size();

            if (index < containerSlots) {
                if (!this.mergeItemStack(itemStackSlot, containerSlots, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemStackSlot, 0, containerSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStackSlot.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemStackSlot.getCount() == itemStackCopy.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemStackSlot);
        }

        return itemStackCopy;
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity playerIn)
    {
        return this.tileBandAmp.isUsableByPlayer(playerIn);
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player)
    {
        // *** only allow owners to manage instruments ***
        ItemStack stack = ItemStack.EMPTY;
        if (tileBandAmp.isOwner(player) || slotId >= TileBandAmp.MAX_SLOTS)
            stack = super.slotClick(slotId, dragType, clickTypeIn, player);

        setDuration();
        return stack;
    }

    @Override
    public void detectAndSendChanges()
    {
        setDuration();
        super.detectAndSendChanges();
    }

    private void setDuration()
    {
        int dur = (getDuration(this.inventorySlots));
        this.tileBandAmp.setDuration(dur);
    }

    private static int getDuration(List<net.minecraft.inventory.container.Slot> inventory)
    {
        int duration = 0;
        for (net.minecraft.inventory.container.Slot slot: inventory)
        {
            ItemStack stackInSlot = slot.getStack();
            if((slot instanceof SlotBandAmp) && (stackInSlot.getItem() instanceof ItemInstrument))
               duration = getDuration(stackInSlot, duration);
        }
        return duration;
    }

    private static int getDuration(ItemStack itemStack, int durationIn)
    {
        int duration = durationIn;
        ItemStack sheetMusic = getSheetMusic(itemStack);
        if (!sheetMusic.isEmpty() && sheetMusic.getTagCompound() != null)
        {
            CompoundNBT contents = (CompoundNBT) sheetMusic.getTagCompound().getTag(KEY_SHEET_MUSIC);
            if (contents.hasKey(KEY_MML))
            {
                int durationSheet = contents.getInteger(KEY_DURATION);
                if (durationSheet > duration) duration = durationSheet;
            }
        }
        return duration;
    }
}
