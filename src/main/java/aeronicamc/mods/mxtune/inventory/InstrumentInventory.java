package aeronicamc.mods.mxtune.inventory;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.util.IInstrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class InstrumentInventory implements IInventory
{
    private static final int INV_SIZE = 1;
    public final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private final ItemStack stack;

    public InstrumentInventory(ItemStack stack)
    {
        this.stack = stack;
        if (!this.stack.hasTag())
            this.stack.setTag(new CompoundNBT());

        assert this.stack.getTag() != null;
        this.writeToNBT(this.stack.getTag());
    }

    @Override
    public int getContainerSize()
    {
        return inventory.size();
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
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int pIndex, int pCount)
    {
        return ItemStackHelper.removeItem(inventory, pIndex, pCount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pIndex)
    {
        ItemStack itemstack = this.inventory.get(pIndex);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else
        {
            this.inventory.set(pIndex, ItemStack.EMPTY);
            return itemstack;
        }
    }

    @Override
    public void setItem(int slot, ItemStack pStack)
    {
        inventory.set(slot, pStack);
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
        this.writeToNBT(this.stack.getTag());
    }

    @Override
    public boolean stillValid(PlayerEntity pPlayer)
    {
        return pPlayer.getItemInHand(Hand.MAIN_HAND).getItem() instanceof IInstrument;
    }

    @Override
    public void clearContent()
    {
        this.inventory.clear();
        this.setChanged();
    }

    private void readFromNBT(@ Nonnull CompoundNBT compound)
    {
        ListNBT items = compound.getList(Reference.ITEM_INVENTORY, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < items.size(); ++i)
        {
            CompoundNBT item = items.getCompound(i);
            int slot = item.getInt("slot");
            if (slot >= 0 && slot < getContainerSize())
            {
                setItem(slot, ItemStack.of(item));
            }
        }
    }

    private void writeToNBT(CompoundNBT compound)
    {
        ListNBT items = new ListNBT();

        for (int i = 0; i < getContainerSize(); ++i)
        {
            if (!getItem(i).isEmpty())
            {
                CompoundNBT item = new CompoundNBT();
                item.putInt("slot", i);
                getItem(i).setTag(item);
                items.add(item);
            }
        }
        compound.put(Reference.ITEM_INVENTORY, items);
    }
}
