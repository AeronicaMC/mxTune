package aeronicamc.mods.mxtune.blocks;

import aeronicamc.mods.mxtune.init.ModTileEntities;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MusicBlockTile extends TileEntity implements INamedContainerProvider, INameable, IMusicPlayer
{
    private ITextComponent customName;
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler);

    private int playId;
    private int lastPlayId;
    private int durationSeconds;

    public MusicBlockTile()
    {
        super(ModTileEntities.INV_MUSIC_BLOCK.get());
    }

    private IItemHandler createHandler() {
        return new ItemStackHandler(1) {

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

//            @Override
//            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
//                return stack.getItem() == Items.DIAMOND;
//            }

//            @Nonnull
//            @Override
//            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
//                if (stack.getItem() != Items.DIAMOND) {
//                    return stack;
//                }
//                return super.insertItem(slot, stack, simulate);
//            }
        };
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT tag = super.getUpdateTag();
        return this.save(tag);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT cmp = new CompoundNBT();
        save(cmp);
        return new SUpdateTileEntityPacket(worldPosition, 1, cmp);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        load(getBlockState(), pkt.getTag());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load(BlockState state, CompoundNBT nbt)
    {
        CompoundNBT invTag = nbt.getCompound("Inventory");
        handler.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag));
        if (nbt.contains("CustomName", Constants.NBT.TAG_STRING)) {
            this.customName = ITextComponent.Serializer.fromJson(nbt.getString("CustomName"));
        }
        if (nbt.contains("duration", Constants.NBT.TAG_INT))
            this.durationSeconds = nbt.getInt("duration");
        super.load(state, nbt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompoundNBT save(CompoundNBT tag) {
        handler.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put("Inventory", compound);
        });
        if (this.customName != null) {
            tag.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
        }
        tag.putInt("duration", this.durationSeconds);
        return super.save(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.mxtune.music_block");
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        if (level == null) return null;
        return new MusicBlockContainer(i, level, worldPosition, playerInventory, playerEntity);
    }

    // INameable

    public ITextComponent getDefaultName() {
        return getDisplayName();
    }

    @Override
    public ITextComponent getName()
    {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    @Nullable
    @Override
    public ITextComponent getCustomName()
    {
        return this.customName;
    }

    public void setCustomName(ITextComponent name) {
        this.customName = name;
        setChanged();
    }

    @Nullable
    @Override
    public IItemHandler getInventory()
    {
        if (handler.resolve().isPresent())
        {
            return this.handler.resolve().get();
        }
        else
            return null;
    }

    @Override
    public String getMML()
    {
        return SheetMusicHelper.getMusicFromIMusicPlayer(this);
    }

    @Override
    public int getDuration()
    {
        return durationSeconds;
    }

    public int getPlayId()
    {
        return playId;
    }

    public void setPlayId(int playId)
    {
        this.playId = playId;
    }
}
