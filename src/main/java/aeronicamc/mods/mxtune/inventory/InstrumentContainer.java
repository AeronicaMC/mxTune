package aeronicamc.mods.mxtune.inventory;


import aeronicamc.mods.mxtune.init.ModContainers;
import aeronicamc.mods.mxtune.util.IInstrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.IContainerFactory;

public class InstrumentContainer extends Container
{
    public InstrumentContainer(int windowId, World world, BlockPos pos, PlayerInventory playerInventory ,PlayerEntity playerEntity)
    {
        super(ModContainers.INSTRUMENT_CONTAINER.get(), windowId);
        InstrumentInventory instrumentInventory = new InstrumentInventory(playerEntity.getItemInHand(Hand.MAIN_HAND));

        this.addSlot(new SlotInstrument(instrumentInventory, 0, 12, 8 + 2 * 18));

        // Player Inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, j * 18 + 12, i * 18 + 84));
            }
        }

        // Player HotBar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new SlotHotBar(playerInventory, i, i * 18 + 12, 142));
        }

    }

    public ITextComponent getName()
    {
        return new StringTextComponent("The Inventory");
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return !playerIn.isHurt();
    }

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

    public static class Factory implements IContainerFactory<InstrumentContainer>
    {
        @Override
        public InstrumentContainer create(final int windowId, final PlayerInventory inv, final PacketBuffer data) {
            final BlockPos pos = data.readBlockPos();
            final World world = inv.player.getCommandSenderWorld();
            final PlayerEntity player = inv.player;

            if (!(player.getItemInHand(Hand.MAIN_HAND).getItem() instanceof IInstrument)) {
                throw new IllegalStateException("Invalid item at " + player.getDisplayName().getString());
            }

            return new InstrumentContainer(windowId, world, pos, inv, player);
        }
    }
}
