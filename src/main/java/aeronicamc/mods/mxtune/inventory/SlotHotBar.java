package aeronicamc.mods.mxtune.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotHotBar extends Slot
{
    public SlotHotBar(IInventory pContainer, int pIndex, int pX, int pY)
    {
        super(pContainer, pIndex, pX, pY);
    }

    /**
     * Prevent the held item from being taken or moved on the player HotBar
     * Useful to prevent the item from being placed into
     * its own inventory and crashing the game
     */
    @Override
    public boolean mayPickup(PlayerEntity pPlayer)
    {
        ItemStack itemStack = pPlayer.getMainHandItem();
        if (!itemStack.isEmpty())
            return (this.getSlotIndex() != pPlayer.inventory.selected);

        return super.mayPickup(pPlayer);
    }
}
