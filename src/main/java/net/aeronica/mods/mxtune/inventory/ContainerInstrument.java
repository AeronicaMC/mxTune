/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.inventory;

import net.aeronica.mods.mxtune.items.ItemMusicPaper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerInstrument extends net.minecraft.inventory.container.Container
{
    public ContainerInstrument(PlayerEntity playerIn)
    {
        // The Item Inventory for this Container
        InventoryInstrument inventoryInstrument = new InventoryInstrument(playerIn.getHeldItemMainhand());
        PlayerInventory inventoryPlayer = playerIn.inventory;

        // ItemInventory
        addSlotToContainer(new SlotInstrument(inventoryInstrument, 0, 12, 8 + 2 * 18));

        int slotIndex = -1;
        //Player Hotbar
        for (int i = 0; i < 9; i++)
        {
            this.addSlotToContainer(new SlotHotBar(inventoryPlayer, ++slotIndex, 18 * i + 12, 142));
        }
        //Player Inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new Slot(inventoryPlayer, ++slotIndex, 18 * j + 12, 18 * i + 84));
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotIndex)
    {
        Slot slot = this.getSlot(slotIndex);
        
        if (!slot.getHasStack())
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack newStack = stack.copy();

        if (!(stack.getItem() instanceof IMusic) && !((stack.getItem() instanceof ItemMusicPaper) && stack.hasDisplayName())) return ItemStack.EMPTY;

        if (slotIndex == 0)
        {
            if (!this.mergeItemStack(stack, 1, this.inventorySlots.size(), false))
                return ItemStack.EMPTY;
            slot.onSlotChanged();
        }
        else if (!this.mergeItemStack(stack, 0, 1, false))
        {
            return ItemStack.EMPTY;
        }
        if (stack.getCount() == 0)
        {
            slot.putStack(ItemStack.EMPTY);
        }
        else
        {
            slot.onSlotChanged();
        }

        slot.onSlotChanged();
        return newStack;
    }
    
    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {return true;}
}
