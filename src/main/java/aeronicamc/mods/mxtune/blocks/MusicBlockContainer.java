package aeronicamc.mods.mxtune.blocks;


import aeronicamc.mods.mxtune.init.ModBlocks;
import aeronicamc.mods.mxtune.init.ModContainers;
import aeronicamc.mods.mxtune.inventory.SlotMusicBlock;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.items.CapabilityItemHandler;

public class MusicBlockContainer extends genericContainer
{
    private static final int CONTAINER_ROWS = 4;
    private static final int CONTAINER_SLOTS_PER_ROW = 4;
    public static final int CONTAINER_SIZE = CONTAINER_ROWS * CONTAINER_SLOTS_PER_ROW;

    public MusicBlockContainer(int windowId, World world, BlockPos pos, PlayerInventory playerInventory , PlayerEntity playerEntity)
    {
        super(ModContainers.MUSIC_BLOCK_CONTAINER.get(), windowId, world, pos, playerInventory, playerEntity);
        final int guiConX = 55;
        final int guiConY = 13;
        final int guiInvX = 10;
        final int guiInvY = 94;
        this.playerEntity = playerEntity;
        tileEntity = world.getBlockEntity(pos);

        if (tileEntity != null)
        {
            tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(
                    h ->
                    {
                        for (int i = 0; i < CONTAINER_ROWS; i++) {
                            for (int j = 0; j < CONTAINER_SLOTS_PER_ROW; j++) {
                                addSlot(new SlotMusicBlock(h, (MusicBlockTile) tileEntity, j + i * CONTAINER_ROWS, j * 18 + guiConX, i * 18 + guiConY));
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

        scrapCheck(world, playerEntity, pos);
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

    @Override
    public ITextComponent getName()
    {
        if ((tileEntity != null) && (tileEntity.getLevel() != null) && tileEntity.getLevel().isClientSide)
            return ((MusicBlockTile)tileEntity).getName();
        return new StringTextComponent("");
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        boolean tileNotNull = tileEntity != null && tileEntity.getLevel() != null;
        return tileNotNull && stillValid(IWorldPosCallable.create(tileEntity.getLevel(), tileEntity.getBlockPos()), playerEntity, ModBlocks.MUSIC_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            // From Player Inventory to Container Inventory - low to high slot index (reverse == false)
            if (pIndex < CONTAINER_SIZE) {
                if (!this.moveItemStackTo(itemstack1, CONTAINER_SIZE, slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
                // From Container Inventory to Player Inventory - low to high slot index (reverse == false)
            } else if (!this.moveItemStackTo(itemstack1, 0, CONTAINER_SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public static class Factory implements IContainerFactory<MusicBlockContainer>
    {
        @Override
        public MusicBlockContainer create(final int windowId, final PlayerInventory inv, final PacketBuffer data) {
            final BlockPos pos = data.readBlockPos();
            final World world = inv.player.getCommandSenderWorld();
            final TileEntity tileEntity = world.getBlockEntity(pos);
            final PlayerEntity player = inv.player;

            if (!(tileEntity instanceof MusicBlockTile)) {
                throw new IllegalStateException("Invalid block at " + pos);
            }

            return new MusicBlockContainer(windowId, world, pos, inv, player);
        }
    }
}
