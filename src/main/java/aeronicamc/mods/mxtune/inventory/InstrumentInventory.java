package aeronicamc.mods.mxtune.inventory;

import aeronicamc.mods.mxtune.util.IInstrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

public class InstrumentInventory implements IInventory
{
    public final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final ItemStack stack;

    public InstrumentInventory(ItemStack stack)
    {
        this.stack = stack;
        if (!this.stack.hasTag())
            this.stack.setTag(new CompoundNBT());

        assert this.stack.getTag() != null;
        ItemStackHelper.loadAllItems(stack.getTag(), items);
    }

    @Override
    public int getContainerSize()
    {
        return items.size();
    }

    @Override
    public boolean isEmpty()
    {
        int notEmptyCount = 0;
        for (int i = 0; i < this.getContainerSize(); ++i)
        {
            if (!getItem(i).isEmpty())
                notEmptyCount++;
        }
        return notEmptyCount == 0;
    }

    @Override
    public ItemStack getItem(int slot)
    {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int pIndex, int pCount)
    {
        return ItemStackHelper.removeItem(items, pIndex, pCount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pIndex)
    {
        ItemStack itemstack = this.items.get(pIndex);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else
        {
            this.items.set(pIndex, ItemStack.EMPTY);
            return itemstack;
        }
    }

    @Override
    public void setItem(int slot, ItemStack pStack)
    {
        items.set(slot, pStack);
        if (!pStack.isEmpty() && pStack.getCount() > this.getMaxStackSize())
        {
            pStack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public void setChanged()
    {
        for (int i = 0; i < this.getContainerSize(); ++i)
        {
            if(!this.getItem(i).isEmpty() && this.getItem(i).getCount() == 0)
                this.setItem(i, ItemStack.EMPTY);
        }
        assert this.stack.getTag() != null;
        ItemStackHelper.saveAllItems(stack.getTag(), items);
    }

    @Override
    public boolean stillValid(PlayerEntity pPlayer)
    {
        return pPlayer.inventory.getSelected().getItem() instanceof IInstrument;
    }

    @Override
    public void clearContent()
    {
        this.items.clear();
        this.setChanged();
    }
}
