package aeronicamc.mods.mxtune.inventory;


import aeronicamc.mods.mxtune.blocks.ILockable;
import aeronicamc.mods.mxtune.blocks.MusicBlockEntity;
import aeronicamc.mods.mxtune.init.ModContainers;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.items.CapabilityItemHandler;

public class MusicBlockContainer extends Container
{
    private static final int CONTAINER_ROWS = 4;
    private static final int CONTAINER_SLOTS_PER_ROW = 4;
    public static final int CONTAINER_SIZE = CONTAINER_ROWS * CONTAINER_SLOTS_PER_ROW;

    private final TileEntity blockEntity;
    private final PlayerEntity playerEntity;
    private final BlockPos blockPos;

    public MusicBlockContainer(int windowId, World world, BlockPos pos, PlayerInventory playerInventory , PlayerEntity playerEntity)
    {
        super(ModContainers.MUSIC_BLOCK_CONTAINER.get(), windowId);
        final int guiConX = 58;
        final int guiConY = 17;
        final int guiInvX = 13;
        final int guiInvY = 102;
        this.playerEntity = playerEntity;
        blockEntity = world.getBlockEntity(pos);
        blockPos = pos;

        if (blockEntity != null)
        {
            blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(
                    h ->
                    {
                        for (int i = 0; i < CONTAINER_ROWS; i++) {
                            for (int j = 0; j < CONTAINER_SLOTS_PER_ROW; j++) {
                                addSlot(new SlotMusicBlock(h, (MusicBlockEntity) blockEntity, j + i * CONTAINER_ROWS, j * 18 + guiConX, i * 18 + guiConY));
                            }
                        }
                    });
        } else
            throw new IllegalStateException("Invalid tile entity at " + pos);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, j * 18 + guiInvX, i * 18 + guiInvY));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, i * 18 + guiInvX, guiInvY + 58));
        }

        trackSignals();
        scrapCheck(world, playerEntity, pos);
    }

    public BlockPos getPosition()
    {
        return blockPos;
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
        if (blockEntity instanceof MusicBlockEntity)
        {
            signals += ((MusicBlockEntity) blockEntity).isRearRedstoneInputEnabled() ? 1 : 0;
            signals += ((MusicBlockEntity) blockEntity).isLeftRedstoneOutputEnabled() ? 2 : 0;
            signals += ((MusicBlockEntity) blockEntity).isRightRedstoneOutputEnabled() ? 4 : 0;
            signals += ((MusicBlockEntity) blockEntity).isLocked() ? 8 : 0;
        }
        return signals;
    }

    public void setSignals(int signals)
    {
        if (blockEntity instanceof MusicBlockEntity)
        {
            ((MusicBlockEntity) blockEntity).setRearRedstoneInputEnabled((signals & 0x0001) > 0);
            ((MusicBlockEntity) blockEntity).setLeftRedstoneOutputEnabled((signals & 0x0002) > 0);
            ((MusicBlockEntity) blockEntity).setRightRedstoneOutputEnabled((signals & 0x0004) > 0);
           // if (LockableHelper.canLock(playerEntity, (ILockable) blockEntity))
                ((ILockable) blockEntity).setLock((signals & 0x0008) > 0);
        }
    }

    public int getDuration()
    {
        return blockEntity instanceof MusicBlockEntity ? ((MusicBlockEntity) blockEntity).getDuration() : 0;
    }

    private void scrapCheck(World pLevel, PlayerEntity pEntity, BlockPos blockPos)
    {
        if (!pLevel.isClientSide())
        {
            slots.stream().filter(
                    p -> p.index < CONTAINER_SIZE).forEach(
                            p -> SheetMusicHelper.scrapSheetMusicInInstrumentIfExpired(p, p.getItem(), pLevel, pEntity, blockPos));

        }
    }

    public ITextComponent getName()
    {
        if ((blockEntity != null) && (blockEntity.getLevel() != null) && blockEntity.getLevel().isClientSide)
            return ((MusicBlockEntity) blockEntity).getName();
        return new StringTextComponent("");
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        boolean tileNotNull = blockEntity != null && blockEntity.getLevel() != null;
        return tileNotNull && stillValid(IWorldPosCallable.create(blockEntity.getLevel(), blockEntity.getBlockPos()), playerIn, blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getBlock());
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        // *** only allow owners to manage instruments  when locked ***
        if (((ILockable) blockEntity).isLocked() && !((ILockable) blockEntity).isOwner(playerEntity.getUUID())) return itemstack;

        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            // From Container Inventory to Player Inventory - low to high slot index (reverse == true)
            if (pIndex < CONTAINER_SIZE) {
                if (!this.moveItemStackTo(itemstack1, CONTAINER_SIZE, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            // From Player Inventory to Container Inventory - low to high slot index (reverse == false)
            } else if (!this.moveItemStackTo(itemstack1, 0, CONTAINER_SIZE, false))
                return ItemStack.EMPTY;

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public ItemStack clicked(int pSlotId, int pDragType, ClickType pClickType, PlayerEntity pPlayer)
    {
        // *** only allow owners to manage instruments when locked ***
        ItemStack stack = ItemStack.EMPTY;
        if (!(((ILockable) blockEntity).isLocked() && !((ILockable) blockEntity).isOwner(playerEntity.getUUID())) || pSlotId >= CONTAINER_SIZE)
        {
            stack = super.clicked(pSlotId, pDragType, pClickType, pPlayer);
        }
        return stack;
    }

    public static class Factory implements IContainerFactory<MusicBlockContainer>
    {
        @Override
        public MusicBlockContainer create(final int windowId, final PlayerInventory inv, final PacketBuffer data) {
            final BlockPos pos = data.readBlockPos();
            final World world = inv.player.getCommandSenderWorld();
            final TileEntity tileEntity = world.getBlockEntity(pos);
            final PlayerEntity player = inv.player;

            if (!(tileEntity instanceof MusicBlockEntity)) {
                throw new IllegalStateException("Invalid block at " + pos);
            }

            return new MusicBlockContainer(windowId, world, pos, inv, player);
        }
    }

    public TileEntity getBlockEntity()
    {
        return blockEntity;
    }

    public PlayerEntity getPlayerEntity()
    {
        return playerEntity;
    }

    public BlockPos getBlockPos()
    {
        return blockPos;
    }
}
