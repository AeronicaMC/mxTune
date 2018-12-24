/*
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.sound.SoundRange;
import net.aeronica.mods.mxtune.util.EnumRelativeSide;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.world.IModLockableContainer;
import net.aeronica.mods.mxtune.world.OwnerUUID;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.LockCode;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileBandAmp extends TileInstrument implements IModLockableContainer
{
    public static final int MAX_SLOTS = 8;
    private static final String KEY_CUSTOM_NAME = "CustomName";
    private static final String KEY_DURATION = "Duration";
    private static final String KEY_POWERED = "powered";
    private static final String KEY_LEFT_RS_OUTPUT_ENABLED = "leftRedstoneOutputEnabled";
    private static final String KEY_REAR_RS_INPUT_ENABLED = "rearRedstoneInputEnabled";
    private static final String KEY_RIGHT_RS_OUTPUT_ENABLED = "rightRedstoneOutputEnabled";
    private static final String KEY_UPDATE_COUNT = "updateCount";
    private boolean previousInputPowerState;
    private Integer playID;
    private Integer lastPlayID;
    private LockCode code = LockCode.EMPTY_CODE;
    private OwnerUUID ownerUUID = OwnerUUID.EMPTY_UUID;
    private SoundRange soundRange = SoundRange.NORMAL;
    private String bandAmpCustomName;
    private int duration;
    private boolean rearRedstoneInputEnabled = true;
    private boolean leftRedstoneOutputEnabled = true;
    private boolean rightRedstoneOutputEnabled = true;
    private int updateCount;
    private int prevUpdateCount;

    public TileBandAmp() { /* NOP */ }

    public TileBandAmp(EnumFacing facing)
    {
        this.inventory =  new InstrumentStackHandler(MAX_SLOTS);
        this.facing = facing;
        this.playID = -1;
    }

    @Override
    public void onLoad()
    {
        clearLastPlayID();
        setPlayID(-1);
    }

    public Integer getPlayID() { return playID; }

    public void setPlayID(@Nullable Integer playID)
    {
        this.playID = playID;
        if (isPlaying()) this.lastPlayID = this.playID;
    }

    boolean lastPlayIDSuccess() { return this.lastPlayID > 0; }

    void clearLastPlayID() { this.lastPlayID = -1; }

    private boolean isPlaying() { return (this.playID != null) && (this.playID > 0); }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        inventory = new InstrumentStackHandler(MAX_SLOTS);
        inventory.deserializeNBT(tag);
        duration = tag.getInteger(KEY_DURATION);
        previousInputPowerState = tag.getBoolean(KEY_POWERED);
        code = LockCode.fromNBT(tag);
        ownerUUID = OwnerUUID.fromNBT(tag);
        rearRedstoneInputEnabled = tag.getBoolean(KEY_REAR_RS_INPUT_ENABLED);
        leftRedstoneOutputEnabled = tag.getBoolean(KEY_LEFT_RS_OUTPUT_ENABLED);
        rightRedstoneOutputEnabled = tag.getBoolean(KEY_RIGHT_RS_OUTPUT_ENABLED);
        updateCount = tag.getInteger(KEY_UPDATE_COUNT);
        soundRange = SoundRange.fromNBT(tag);

        if (tag.hasKey(KEY_CUSTOM_NAME, 8))
        {
            bandAmpCustomName = tag.getString(KEY_CUSTOM_NAME);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag.merge(inventory.serializeNBT());
        tag.setBoolean(KEY_POWERED, previousInputPowerState);
        tag.setInteger(KEY_DURATION, duration);
        tag.setBoolean(KEY_REAR_RS_INPUT_ENABLED, rearRedstoneInputEnabled);
        tag.setBoolean(KEY_LEFT_RS_OUTPUT_ENABLED, leftRedstoneOutputEnabled);
        tag.setBoolean(KEY_RIGHT_RS_OUTPUT_ENABLED, rightRedstoneOutputEnabled);
        tag.setInteger(KEY_UPDATE_COUNT, updateCount);
        soundRange.toNBT(tag);
        ownerUUID.toNBT(tag);

        if (code != null)
        {
            code.toNBT(tag);
        }
        if (hasCustomName())
        {
            tag.setString(KEY_CUSTOM_NAME, bandAmpCustomName);
        }
        return super.writeToNBT(tag);
    }

    public int getDuration()
    {
        return this.duration;
    }

    public void setDuration(int duration)
    {
        if(this.duration != duration)
        {
            this.duration = duration;
            markDirtySyncClient();
        }
    }

    /** This does nothing but log the side that's powered */
    @SuppressWarnings("unused")
    void logInputPower(BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        Vec3i vec3i = pos.subtract(fromPos);
        ModLogger.info("TileBandAmp: Powered from %s's %s face",
                       blockIn.getBlockState().getBlock().getLocalizedName(),
                       EnumFacing.getFacingFromVector(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
    }

    /**
     * @return the previousInputPowerState
     */
    boolean getPreviousInputState()
    {
        return previousInputPowerState;
    }

    /**
     * @param previousRedStoneState the previousInputPowerState to set
     */
    void setPreviousInputState(boolean previousRedStoneState)
    {
        this.previousInputPowerState = previousRedStoneState;
        markDirty();
    }

    public boolean isRearRedstoneInputEnabled()
    {
        return rearRedstoneInputEnabled;
    }

    public void setRearRedstoneInputEnabled(boolean rearRedstoneInputEnabled)
    {
        if(this.rearRedstoneInputEnabled != rearRedstoneInputEnabled)
        {
            this.rearRedstoneInputEnabled = rearRedstoneInputEnabled;
            markDirtySyncClient();
        }
    }

    public boolean isLeftRedstoneOutputEnabled()
    {
        return leftRedstoneOutputEnabled;
    }

    public void setLeftRedstoneOutputEnabled(boolean leftRedstoneOutputEnabled)
    {
        if(this.leftRedstoneOutputEnabled != leftRedstoneOutputEnabled)
        {
            this.leftRedstoneOutputEnabled = leftRedstoneOutputEnabled;
            markDirtySyncClient();
        }
    }

    public boolean isRightRedstoneOutputEnabled()
    {
        return rightRedstoneOutputEnabled;
    }

    public void setRightRedstoneOutputEnabled(boolean rightRedstoneOutputEnabled)
    {
        if(this.rightRedstoneOutputEnabled != rightRedstoneOutputEnabled)
        {
            this.rightRedstoneOutputEnabled = rightRedstoneOutputEnabled;
            markDirtySyncClient();
        }
    }

    public SoundRange getSoundRange()
    {
        return soundRange;
    }

    public void setSoundRange(SoundRange soundRange)
    {
        if(this.soundRange != soundRange)
        {
            this.soundRange = soundRange;
            markDirtySyncClient();
        }
    }

    public int getUpdateCount()
    {
        return updateCount;
    }

    public void setUpdateCount(int updateCount)
    {
        this.updateCount = updateCount;
    }

    private void incrementCount()
    {
        updateCount = ((++updateCount) %16);

        if (world != null && !world.isRemote)
        {
            ModLogger.info("TileBandAmp: %s, SEND updateCount: %02d", getPos(), updateCount);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    private void syncClient()
    {
        if (world != null && !world.isRemote)
        {
            incrementCount();
            world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockBandAmp.UPDATE_COUNT, updateCount), 2);
            world.notifyBlockUpdate(getPos(), world.getBlockState(pos), world.getBlockState(pos), 2);
            world.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
        }
    }

    private void markDirtySyncClient()
    {
        syncClient();
        markDirty();
    }

    /**
     * Intended to be called from your Block#randomDisplayTick method.
     * Useful for ensuing Redstone Dust connects/disconnects from your block when it's side(s) connection
     * properties are changed. Relies on seeing a change in the UPDATE_COUNT property
     */
    void clientSideNotify()
    {
        if(world.isRemote)
        {
            int count = world.getBlockState(pos).getActualState(world, pos).getValue(BlockBandAmp.UPDATE_COUNT);
            if(prevUpdateCount != updateCount)
            {
                ModLogger.info("TileBandAmp: %s, RECV updateCount: %02d, UPDATE_COUNT: %02d",this.getPos(), updateCount, count);
                world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockBandAmp.UPDATE_COUNT, updateCount), 1);
                world.markBlockRangeForRenderUpdate(pos, pos);
                prevUpdateCount = updateCount;
            }
        }
    }

    class InstrumentStackHandler extends ItemStackHandler
    {
        InstrumentStackHandler(int size) {super(size);}

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if ((stack.getItem() instanceof ItemInstrument))
                return super.insertItem(slot, stack, simulate);
            else
                return stack;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> cap, @Nullable EnumFacing side)
    {
        EnumRelativeSide enumRelativeSide = EnumRelativeSide.getRelativeSide(side, getFacing());
        return (((enumRelativeSide == EnumRelativeSide.TOP) && !isPlaying()) || ((enumRelativeSide == EnumRelativeSide.BOTTOM) && isPlaying()) && (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) || super.hasCapability(cap, side);
    }

    @Override
    public <T> T getCapability(Capability<T> cap, @Nullable EnumFacing side)
    {
        EnumRelativeSide enumRelativeSide = EnumRelativeSide.getRelativeSide(side, getFacing());
        if ((((enumRelativeSide == EnumRelativeSide.TOP) && !isPlaying()) || ((enumRelativeSide == EnumRelativeSide.BOTTOM) && isPlaying())) && (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        return super.getCapability(cap, side);
    }

    /*
     Lockable
     */
    @Override
    public boolean isLocked()
    {
        return this.code != null && !this.code.isEmpty();
    }

    @Override
    public void setLockCode(LockCode code)
    {
        if(!this.code.getLock().equals(code.getLock()))
        {
            this.code = code;
            markDirtySyncClient();
        }
    }

    @Override
    public LockCode getLockCode() { return this.code; }

    @Override
    public boolean isOwner(OwnerUUID ownerUUID)
    {
        return this.ownerUUID != null && this.ownerUUID.equals(ownerUUID);
    }

    @Override
    public boolean isOwner(EntityPlayer entityPlayer)
    {
        return ownerUUID.getUUID().equals(entityPlayer.getPersistentID());
    }

    @Override
    public void setOwner(OwnerUUID ownerUUID)
    {
        this.ownerUUID = ownerUUID;
        markDirty();
    }

    @Override
    public OwnerUUID getOwner() { return ownerUUID; }

    /*
     Nameable
    */
    @Override
    public String getName()
    {
        return this.hasCustomName() ? this.bandAmpCustomName : "tile.mxtune:band_amp.name";
    }

    @Override
    public boolean hasCustomName()
    {
        return this.bandAmpCustomName != null && !this.bandAmpCustomName.isEmpty();
    }

    void setCustomInventoryName(String customInventoryName)
    {
        this.bandAmpCustomName = customInventoryName;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }

    /**
     * @param player to be evaluated
     * @return true only for the owner of the TE
     */
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        if (this.world.getTileEntity(this.pos) != this)
        {
            return false;
        }
        else
        {
            return player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }
}
