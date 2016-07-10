/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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

import net.aeronica.mods.mxtune.init.StartupItems;
import net.aeronica.mods.mxtune.items.ItemMusicPaper;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContainerSheetMusic extends Container
{
private static IItemHandler itemHandler;
// ItemHandlerHelper InvWrapper
	public ContainerSheetMusic(InventoryPlayer inventoryIn, ItemStack stackIn)
	{
		itemHandler = stackIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		
		//Item Inventory
		this.addSlotToContainer(new SlotItemHandlerSheetMusic(itemHandler, 0, 12, 8 + 2 * 18));
		
		int slotIndex = -1;
		//Player Hotbar
		for (int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new SlotHotBarSticky(inventoryIn, ++slotIndex, 18 * i + 12, 142));
		}
		//Player Inventory
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(inventoryIn, ++slotIndex, 18 * j + 12, 18 * i + 84));
			}
		}
		/** The Alpha and the Omega of IInventory */
		StartupItems.item_converter.destroyIInventory(itemHandler, stackIn);
	}

    @Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
	{
	    ModLogger.logInfo("transferStackInSlot slotIndex: " + slotIndex);
		Slot slot = this.getSlot(slotIndex);
		
		if (slot == null || !slot.getHasStack())
		{
			return null;
		}

		ItemStack stack = slot.getStack();
		ItemStack newStack = stack.copy();

		if (!(stack.getItem() instanceof IMusic) && !((stack.getItem() instanceof ItemMusicPaper) && stack.hasDisplayName())) return null;

		ModLogger.logInfo("stackSize: " + stack.stackSize + ", getSlotStackLimit: " + slot.getSlotStackLimit());
		if (slotIndex == 0)
		{
			if (!this.mergeItemStack(stack, 1, this.inventorySlots.size(), false))
				return null;
			slot.onSlotChanged();
		}
		else if (!this.mergeItemStack(stack, 0, 1, false))
		{
			return null;
		}
		if (stack.stackSize == 0)
		{
			slot.putStack(null);
		}
		else
		{
			slot.onSlotChanged();
		}

		slot.onPickupFromSlot(player, newStack);
		return newStack;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return true;
	}	
}
