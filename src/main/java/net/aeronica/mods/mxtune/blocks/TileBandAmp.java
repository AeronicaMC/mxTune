package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.util.EnumRelativeSide;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.world.IModLockableContainer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class TileBandAmp extends TileInstrument implements IModLockableContainer
{
    public static final int MAX_SLOTS = 8;
    private boolean previousRedStoneState;
    private Integer playID = -1;
    private LockCode code = LockCode.EMPTY_CODE;
    private String bandAmpCustomName;

    public TileBandAmp() { /* NOP */ }

    public TileBandAmp(EnumFacing facing)
    {
        this.inventory =  new StackHandler(MAX_SLOTS);
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
        inventory = new StackHandler(MAX_SLOTS);
        inventory.deserializeNBT(tag);
        previousRedStoneState = tag.getBoolean("powered");
        this.code = LockCode.fromNBT(tag);

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

        if (this.code != null)
        {
            this.code.toNBT(tag);
        }
        if (this.hasCustomName())
        {
            tag.setString("CustomName", this.bandAmpCustomName);
        }
        return super.writeToNBT(tag);
    }

    public void setPowered(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        Vec3i vec3i = pos.subtract(fromPos);
        ModLogger.info("TileBandAmp: Powered facing: %s by %s", EnumFacing.getFacingFromVector(vec3i.getX(), vec3i.getY(), vec3i.getZ()), blockIn.getBlockState().getBlock().getLocalizedName());
    }

    /**
     * @return the previousRedStoneState
     */
    public boolean getPreviousRedStoneState()
    {
        return previousRedStoneState;
    }

    /**
     * @param previousRedStoneState the previousRedStoneState to set
     */
    public void setPreviousRedStoneState(boolean previousRedStoneState)
    {
        this.previousRedStoneState = previousRedStoneState;
        markDirty();
    }

    class StackHandler extends ItemStackHandler
    {
        protected StackHandler(int size) {super(size);}

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
    public boolean hasCapability(Capability<?> cap, EnumFacing side)
    {
        EnumRelativeSide enumRelativeSide = EnumRelativeSide.getRelativeSide(side, getFacing());
        return ((enumRelativeSide != EnumRelativeSide.FRONT) && (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) || super.hasCapability(cap, side);
    }

    @Override
    public <T> T getCapability(Capability<T> cap, EnumFacing side)
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
    }

    @Override
    public LockCode getLockCode()
    {
        return this.code;
    }

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

    public void setCustomInventoryName(String customInventoryName)
    {
        this.bandAmpCustomName = customInventoryName;
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
