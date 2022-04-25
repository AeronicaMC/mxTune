package aeronicamc.mods.mxtune.blocks;

import aeronicamc.mods.mxtune.init.ModTileEntities;
import aeronicamc.mods.mxtune.inventory.MusicBlockContainer;
import aeronicamc.mods.mxtune.util.MusicProperties;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

import static aeronicamc.mods.mxtune.util.SheetMusicHelper.KEY_DURATION;

public class MusicBlockTile extends TileEntity implements INamedContainerProvider, INameable, IMusicPlayer, ITickableTileEntity
{
    public static final UUID EMPTY_OWNER = new UUID(0,0);
    private static final Logger LOGGER = LogManager.getLogger(MusicBlockTile.class);
    private ITextComponent customName;
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler);
    private static final String KEY_CUSTOM_NAME = "CustomName";
    private static final String KEY_INVENTORY = "Inventory";
    private static final String KEY_LEFT_RS_OUTPUT_ENABLED = "leftRsOutputEnabled";
    private static final String KEY_REAR_RS_INPUT_ENABLED = "rearRsInputEnabled";
    private static final String KEY_RIGHT_RS_OUTPUT_ENABLED = "rightRsOutputEnabled";
    public static final String KEY_OWNER = "Owner";

    // stored in nbt
    private UUID ownerUUID = EMPTY_OWNER;
    private int duration;
    private boolean rearRsInputEnabled = true;
    private boolean leftRsOutputEnabled = true;
    private boolean rightRsOutputEnabled = false;

    // not stored in nbt
    private boolean previousInputPowerState;
    private boolean LastPlay;
    private int counter;
    private int useHeldCounter;

    public MusicBlockTile()
    {
        super(ModTileEntities.INV_MUSIC_BLOCK.get());
    }

    private IItemHandler createHandler() {
        return new ItemStackHandler(MusicBlockContainer.CONTAINER_SIZE) {

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    @Override
    public void tick()
    {
        if (level != null && counter++ % 5 == 0)
            useHeldCounterUpdate(false);
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
        CompoundNBT invTag = nbt.getCompound(KEY_INVENTORY);
        handler.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag));
        if (nbt.contains(KEY_CUSTOM_NAME, Constants.NBT.TAG_STRING)) {
            this.customName = ITextComponent.Serializer.fromJson(nbt.getString(KEY_CUSTOM_NAME));
        }
        if (nbt.contains(KEY_DURATION, Constants.NBT.TAG_INT))
            this.duration = nbt.getInt(KEY_DURATION);
        if (nbt.contains(KEY_LEFT_RS_OUTPUT_ENABLED, Constants.NBT.TAG_BYTE))
            this.leftRsOutputEnabled = nbt.getBoolean(KEY_LEFT_RS_OUTPUT_ENABLED);
        if (nbt.contains(KEY_RIGHT_RS_OUTPUT_ENABLED, Constants.NBT.TAG_BYTE))
            this.rightRsOutputEnabled = nbt.getBoolean(KEY_RIGHT_RS_OUTPUT_ENABLED);
        if (nbt.contains(KEY_REAR_RS_INPUT_ENABLED, Constants.NBT.TAG_BYTE))
            this.rearRsInputEnabled = nbt.getBoolean(KEY_REAR_RS_INPUT_ENABLED);
        if(nbt.hasUUID(KEY_OWNER))
            this.ownerUUID = nbt.getUUID(KEY_OWNER);
        super.load(state, nbt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompoundNBT save(CompoundNBT tag) {
        handler.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put(KEY_INVENTORY, compound);
        });
        if (this.customName != null) {
            tag.putString(KEY_CUSTOM_NAME, ITextComponent.Serializer.toJson(this.customName));
        }
        tag.putInt(KEY_DURATION, this.duration);
        tag.putBoolean(KEY_LEFT_RS_OUTPUT_ENABLED, leftRsOutputEnabled);
        tag.putBoolean(KEY_RIGHT_RS_OUTPUT_ENABLED, rightRsOutputEnabled);
        tag.putBoolean(KEY_REAR_RS_INPUT_ENABLED, rearRsInputEnabled);
        tag.putUUID(KEY_OWNER, ownerUUID);
        return super.save(tag);
    }

    boolean getPreviousInputState()
    {
        return previousInputPowerState;
    }

    void setPreviousInputState(boolean previousRedStoneState)
    {
        this.previousInputPowerState = previousRedStoneState;
    }

    public boolean isLastPlay()
    {
        return LastPlay;
    }

    public void setLastPlay(boolean lastPlay)
    {
        LastPlay = lastPlay;
    }

    public int getDuration()
    {
        return duration;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
        setChanged();
    }

    public boolean isRearRedstoneInputEnabled()
    {
        return rearRsInputEnabled;
    }

    public void setRearRedstoneInputEnabled(boolean rearRsInputEnabled)
    {
        if(this.rearRsInputEnabled != rearRsInputEnabled)
        {
            this.rearRsInputEnabled = rearRsInputEnabled;
            markDirtySyncClient();
        }
    }

    public boolean isLeftRedstoneOutputEnabled()
    {
        return leftRsOutputEnabled;
    }

    public void setLeftRedstoneOutputEnabled(boolean leftRsOutputEnabled)
    {
        if(this.leftRsOutputEnabled != leftRsOutputEnabled)
        {
            this.leftRsOutputEnabled = leftRsOutputEnabled;
            markDirtySyncClient();
        }
    }

    public boolean isRightRedstoneOutputEnabled()
    {
        return rightRsOutputEnabled;
    }

    public void setRightRedstoneOutputEnabled(boolean rightRsOutputEnabled)
    {
        if(this.rightRsOutputEnabled != rightRsOutputEnabled)
        {
            this.rightRsOutputEnabled = rightRsOutputEnabled;
            markDirtySyncClient();
        }
    }

    private void syncClient()
    {
        if (level != null && !level.isClientSide())
            level.getBlockState(worldPosition).updateNeighbourShapes(level, worldPosition, 3);
    }

    private void markDirtySyncClient()
    {
        syncClient();
        setChanged();
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

    @Override
    public LazyOptional<IItemHandler> getItemHandler()
    {
        return handler;
    }

    @Override
    public MusicProperties getMusicProperties()
    {
        return SheetMusicHelper.getMusicFromIMusicPlayer(this);
    }

    public boolean isUseHeld()
    {
        return  useHeldCounter > 0;
    }

    public void useHeldCounterUpdate(boolean countUp)
    {
        if (countUp)
            useHeldCounter = (useHeldCounter += 5) > 1 ? 5 : useHeldCounter;
        else
            useHeldCounter = (--useHeldCounter < -1) ? -1 : useHeldCounter;
    }
}
