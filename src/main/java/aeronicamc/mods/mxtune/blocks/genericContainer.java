package aeronicamc.mods.mxtune.blocks;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.IContainerFactory;

public abstract class genericContainer extends Container
{
    protected TileEntity tileEntity;
    protected PlayerEntity playerEntity;
    protected BlockPos blockPos;

    public genericContainer(ContainerType containerType, int windowId, World world, BlockPos pos, PlayerInventory playerInventory , PlayerEntity playerEntity)
    {
        super(containerType, windowId);
        blockPos = pos;
    }

    public abstract ITextComponent getName();

    // Transfer the item in test slot to-from any other open slot
    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index == 0)
            {
                if (!this.moveItemStackTo(stack, 1, 37, false))
                    return ItemStack.EMPTY;
                slot.onQuickCraft(stack, itemstack);
            }
            else if (!this.moveItemStackTo(stack, 0, 1, false))
            {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    public abstract static class Factory implements IContainerFactory<genericContainer> {}
}
