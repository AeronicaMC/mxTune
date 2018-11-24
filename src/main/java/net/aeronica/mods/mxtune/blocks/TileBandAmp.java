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
    private boolean previousRedStoneState;
    private Integer playID = -1;
    private LockCode code = LockCode.EMPTY_CODE;
    private OwnerUUID ownerUUID = OwnerUUID.EMPTY_UUID;
    private String bandAmpCustomName;
    private int duration = 0;

    public TileBandAmp() { /* NOP */ }

    public TileBandAmp(EnumFacing facing)
    {
        this.inventory =  new InstrumentStackHandler(MAX_SLOTS);
        this.facing = facing;
        this.playID = -1;
    }

    public Integer getPlayID()
    {
        return playID;
    }

    public void setPlayID(Integer playID)
    {
        this.playID = playID;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        inventory = new InstrumentStackHandler(MAX_SLOTS);
        inventory.deserializeNBT(tag);
        duration = tag.getInteger("Duration");
        previousRedStoneState = tag.getBoolean("powered");
        this.code = LockCode.fromNBT(tag);
        this.ownerUUID = OwnerUUID.fromNBT(tag);

        if (tag.hasKey("CustomName", 8))
        {
            this.bandAmpCustomName = tag.getString("CustomName");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag.merge(inventory.serializeNBT());
        tag.setBoolean("powered", this.previousRedStoneState);
        tag.setInteger("Duration", duration);

        if (this.code != null)
        {
            this.code.toNBT(tag);
        }
        if (this.ownerUUID != null)
        {
            this.ownerUUID.toNBT(tag);
        }
        if (this.hasCustomName())
        {
            tag.setString("CustomName", this.bandAmpCustomName);
        }
        return super.writeToNBT(tag);
    }

    public int getDuration()
    {
        return this.duration;
    }

    public void setDuration(int duration)
    {
            this.duration = duration;
            markDirty();
    }

    void setPowered(BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        Vec3i vec3i = pos.subtract(fromPos);
        ModLogger.info("TileBandAmp: Powered from %s's %s face",
                       blockIn.getBlockState().getBlock().getLocalizedName(),
                       EnumFacing.getFacingFromVector(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
    }

    /**
     * @return the previousRedStoneState
     */
    boolean getPreviousRedStoneState()
    {
        return previousRedStoneState;
    }

    /**
     * @param previousRedStoneState the previousRedStoneState to set
     */
    void setPreviousRedStoneState(boolean previousRedStoneState)
    {
        this.previousRedStoneState = previousRedStoneState;
        markDirty();
    }

    class InstrumentStackHandler extends ItemStackHandler
    {
        InstrumentStackHandler(int size) {super(size);}

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if (stack.getItem() instanceof ItemInstrument)
                return super.insertItem(slot, stack, simulate);
            else
                return stack;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> cap, @Nullable EnumFacing side)
    {
        EnumRelativeSide enumRelativeSide = EnumRelativeSide.getRelativeSide(side, getFacing());
        return ((enumRelativeSide != EnumRelativeSide.FRONT) && (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) || super.hasCapability(cap, side);
    }

    @Override
    public <T> T getCapability(Capability<T> cap, @Nullable EnumFacing side)
    {
        EnumRelativeSide enumRelativeSide = EnumRelativeSide.getRelativeSide(side, getFacing());
        if ((enumRelativeSide != EnumRelativeSide.FRONT) && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
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
        this.code = code;
        markDirty();
    }

    @Override
    public LockCode getLockCode() { return this.code; }

    @Override
    public boolean isOwner()
    {
        return this.ownerUUID != null && !this.ownerUUID.isEmpty();
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
