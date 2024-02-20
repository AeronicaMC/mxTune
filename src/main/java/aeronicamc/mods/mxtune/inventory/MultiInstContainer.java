package aeronicamc.mods.mxtune.inventory;


import aeronicamc.mods.mxtune.init.ModContainers;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.ISlotChangedCallback;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.IContainerFactory;

import javax.annotation.Nullable;

public class MultiInstContainer extends Container
{
    private final IInstrument iInstrument;
    private final ItemStack instStack;
    private final MultiInstInventory multiInstInventory;

    PlayerEntity playerEntity;

    @SuppressWarnings("unused")
    public MultiInstContainer(int windowId, World world, @Nullable BlockPos pos, PlayerInventory playerInventory , PlayerEntity playerEntity)
    {
        super(ModContainers.INSTRUMENT_CONTAINER.get(), windowId);
        this.playerEntity = playerEntity;
        iInstrument = (IInstrument) playerEntity.getMainHandItem().getItem();
        instStack = playerEntity.getMainHandItem();
        multiInstInventory = new MultiInstInventory(instStack);
        this.addSlot(new SlotInstrument(multiInstInventory, 0, 12, 8 + 2 * 18) {
            @Override
            public void setChanged() {
                super.setChanged();
                MultiInstContainer.this.slotsChanged(this.container);
            }
        });

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
        trackSignals();
    }

    public void setSlotChangedCallback(ISlotChangedCallback slotChangedCallback)
    {
        this.multiInstInventory.setSlotChangedCallback(slotChangedCallback);
    }

    public void updateInstrument(int signals)
    {
        setSignals(signals);
    }

    private void trackSignals()
    {
        addDataSlot(new IntReferenceHolder() {
            @Override
            public int get()
            {
                return getSignals();
            }

            @Override
            public void set(int pValue)
            {
                setSignals(pValue);
            }
        });
    }

    public int getSignals()
    {
        int signals = 0;
        if (instStack != null)
        {
            signals += iInstrument.getPatch(instStack) & 0x00FF;
            signals += iInstrument.getAutoSelect(instStack) ? 0x2000 : 0;
            signals += !SheetMusicHelper.getIMusicFromIInstrument(instStack).isEmpty() ? 0x4000 : 0;
        }
        return signals;
    }

    public void setSignals(int signals)
    {
        if (iInstrument != null && !instStack.isEmpty())
        {
            iInstrument.setPatch(instStack, (signals & 0x00FF));
            iInstrument.setAutoSelect(instStack,(signals & 0x2000) > 0);
            multiInstInventory.setChanged();
        }
    }

    public ITextComponent getName()
    {
        return new StringTextComponent("The Inventory");
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return playerIn.isAlive();
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

    public static class Factory implements IContainerFactory<MultiInstContainer>
    {
        @Override
        public MultiInstContainer create(final int windowId, final PlayerInventory inv, final PacketBuffer data) {
            final World world = inv.player.getCommandSenderWorld();
            final PlayerEntity player = inv.player;

            if (!(inv.getSelected().getItem() instanceof IInstrument)) {
                throw new IllegalStateException("Invalid item at " + player.getDisplayName().getString());
            }
            return new MultiInstContainer(windowId, world, null, inv, player);
        }
    }
}
