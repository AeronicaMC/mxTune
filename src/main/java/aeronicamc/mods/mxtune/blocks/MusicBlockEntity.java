package aeronicamc.mods.mxtune.blocks;

import aeronicamc.mods.mxtune.init.ModBlockEntities;
import aeronicamc.mods.mxtune.inventory.MusicBlockContainer;
import aeronicamc.mods.mxtune.util.MusicProperties;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
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

public class MusicBlockEntity extends TileEntity implements INamedContainerProvider, ILockable, IMusicPlayer, INameable, ITickableTileEntity
{
    public static final UUID EMPTY_OWNER = new UUID(0,0);
    private static final Logger LOGGER = LogManager.getLogger(MusicBlockEntity.class);
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler);
    public static final String KEY_CUSTOM_NAME = "CustomName";
    public static final String KEY_INVENTORY = "Inventory";
    public static final String KEY_BUTTON_STATE = "ButtonState";

    // TODO: Remove after next snapshot
    public static final String KEY_LEFT_RS_OUTPUT_ENABLED = "leftRsOutputEnabled";
    public static final String KEY_REAR_RS_INPUT_ENABLED = "rearRsInputEnabled";
    public static final String KEY_RIGHT_RS_OUTPUT_ENABLED = "rightRsOutputEnabled";
    public static final String KEY_LOCK = "Lock";

    public static final String KEY_OWNER = "Owner";

    // stored in nbt
    private ITextComponent customName;
    private UUID ownerUUID = EMPTY_OWNER;
    private byte buttonSignals; // boolean bit OR from redstone signals and lock

    // Button state/signals stored in nbt via the buttonSignals field
    private boolean rearRsInputEnabled;
    private boolean leftRsOutputEnabled;
    private boolean rightRsOutputEnabled;
    private boolean lock;

    // not stored in nbt
    private boolean previousInputPowerState;
    private boolean LastPlay;
    private int counter;
    private int useHeldCounter;
    private int fastRSCounter;

    public MusicBlockEntity()
    {
        super(ModBlockEntities.INV_MUSIC_BLOCK.get());
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
        if (level != null && counter++ % 2 == 0)
        {
            useHeldCounterUpdate(false);
            fastRSCounterUpdate(false);
        }
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

        if (nbt.contains(KEY_BUTTON_STATE, Constants.NBT.TAG_BYTE))
            this.setButtonSignals(nbt.getByte(KEY_BUTTON_STATE));

        // TODO: remove after next snapshot
            if (nbt.contains(KEY_LEFT_RS_OUTPUT_ENABLED, Constants.NBT.TAG_BYTE))
                this.leftRsOutputEnabled = nbt.getBoolean(KEY_LEFT_RS_OUTPUT_ENABLED);
            if (nbt.contains(KEY_RIGHT_RS_OUTPUT_ENABLED, Constants.NBT.TAG_BYTE))
                this.rightRsOutputEnabled = nbt.getBoolean(KEY_RIGHT_RS_OUTPUT_ENABLED);
            if (nbt.contains(KEY_REAR_RS_INPUT_ENABLED, Constants.NBT.TAG_BYTE))
                this.rearRsInputEnabled = nbt.getBoolean(KEY_REAR_RS_INPUT_ENABLED);
            if (nbt.contains(KEY_LOCK))
                this.lock = nbt.getBoolean(KEY_LOCK);

        if (nbt.hasUUID(KEY_OWNER))
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
        tag.putUUID(KEY_OWNER, ownerUUID);
        tag.putByte(KEY_BUTTON_STATE,  getButtonSignals());
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
        int[] duration = new int [1];
        getItemHandler().ifPresent(
                itemHandler ->
                {
                   for (int i = 0; i < itemHandler.getSlots(); i++)
                   {
                       ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                       duration[0] = getDuration(stackInSlot, duration[0]);
                   }
               });
        return duration[0];
    }

    private int getDuration(ItemStack itemStack, int durationIn)
    {
        int duration = durationIn;
        ItemStack sheetMusic = SheetMusicHelper.getIMusicFromIInstrument(itemStack);
        if (!sheetMusic.isEmpty())
        {
            int durationSheet = SheetMusicHelper.getMusicDuration(sheetMusic);
            if (durationSheet > duration) duration = durationSheet;
        }
        return duration;
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

    private byte getButtonSignals()
    {
        updateButtonSignals();
        return buttonSignals;
    }

    private void setButtonSignals(byte buttonSignalsIn)
    {
        rearRsInputEnabled = ((buttonSignalsIn & 0x1) > 0);
        leftRsOutputEnabled = ((buttonSignalsIn & 0x2) > 0);
        rightRsOutputEnabled = ((buttonSignalsIn & 0x4) > 0);
        lock = ((buttonSignalsIn & 0x8) > 0);
    }

    private void updateButtonSignals()
    {
        buttonSignals = 0;
        buttonSignals |= isRearRedstoneInputEnabled() ? 0x1 : 0x0;
        buttonSignals |= isLeftRedstoneOutputEnabled() ? 0x2 : 0x0;
        buttonSignals |= isRightRedstoneOutputEnabled() ? 0x4 : 0x0;
        buttonSignals |= isLocked() ? 0x8 : 0x0;
    }

    private void syncClient()
    {
        if (level != null && !level.isClientSide())
            level.getBlockState(worldPosition).updateNeighbourShapes(level, worldPosition, 3);
    }

    private void markDirtySyncClient()
    {
        updateButtonSignals();
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

    public boolean notHeld()
    {
        return useHeldCounter <= 0;
    }

    public void useHeldCounterUpdate(boolean countUp)
    {
        if (countUp)
            useHeldCounter = (useHeldCounter += 5) > 1 ? 5 : useHeldCounter;
        else
            useHeldCounter = (--useHeldCounter < -1) ? -1 : useHeldCounter;
    }

    public boolean notFastRS()
    {
        return fastRSCounter <= 0;
    }

    public void fastRSCounterUpdate(boolean countUp)
    {
        if (countUp)
            fastRSCounter = (fastRSCounter += 5) > 1 ? 5 : fastRSCounter;
        else
            fastRSCounter = (--fastRSCounter < -1) ? -1 : fastRSCounter;
    }

    @Override
    public boolean isLocked()
    {
        return lock;
    }

    @Override
    public void setLock(boolean lock)
    {
        this.lock = lock;
        markDirtySyncClient();
    }

    @Override
    public boolean isOwner(UUID owner)
    {
        return this.ownerUUID.equals(owner);
    }

    @Override
    public void setOwner(UUID owner)
    {
        this.ownerUUID = owner;
        markDirtySyncClient();
    }

    @Override
    public UUID getOwner()
    {
        return this.ownerUUID;
    }
}
