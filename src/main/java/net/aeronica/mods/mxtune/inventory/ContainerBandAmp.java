package net.aeronica.mods.mxtune.inventory;

import net.aeronica.mods.mxtune.blocks.TileBandAmp;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ContainerBandAmp extends Container
{
    private TileBandAmp tileBandAmp;

    public ContainerBandAmp(InventoryPlayer playerInv, @Nonnull final TileBandAmp tileBandAmp) {
        this.tileBandAmp = tileBandAmp;
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
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        if(playerIn.world.getTileEntity(tileBandAmp.getPos()) != tileBandAmp) {
            return false;
        } else {
            return playerIn.getDistanceSq((double) tileBandAmp.getPos().getX() + 0.5D, (double) tileBandAmp.getPos().getY() + 0.5D, (double) tileBandAmp.getPos().getZ() + 0.5D) <= 64.0D;
        }
    }
}
