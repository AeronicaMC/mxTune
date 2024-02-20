package aeronicamc.mods.mxtune.util;

import net.minecraft.item.ItemStack;

public interface ISlotChangedCallback
{
    enum Type {Inserted, Removed}
    /**
     * Called with the slot index and itemStack. The receiving class may ignore.
     * @param slotIndex when called
     * @param itemStack when called
     * @param operation type of Inserted or Removed
     */
    void onSlotChanged(int slotIndex, ItemStack itemStack, Type operation);
}
