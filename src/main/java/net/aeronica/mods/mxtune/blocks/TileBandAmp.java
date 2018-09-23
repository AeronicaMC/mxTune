package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.util.EnumRelativeSide;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class TileBandAmp extends TileInstrument
{
    public static final int MAX_SLOTS = 8;
    private boolean previousRedStoneState;
    private Integer playID = -1;

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
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag.merge(inventory.serializeNBT());
        tag.setBoolean("powered", this.previousRedStoneState);
        return super.writeToNBT(tag);
    }

    public void setPowered(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        ModLogger.info("Powered facing: %s, name: %s", facing.getName(),  blockIn.getBlockState().getBlock().getLocalizedName());
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
        ModLogger.info("hasCap side: %s, facing: %s", side, getFacing());
        return ((enumRelativeSide != EnumRelativeSide.FRONT) && (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) || super.hasCapability(cap, side);
    }

    @Override
    public <T> T getCapability(Capability<T> cap, EnumFacing side)
    {
        EnumRelativeSide enumRelativeSide = EnumRelativeSide.getRelativeSide(side, getFacing());
        ModLogger.info("getCap side: %s, facing: %s", side, getFacing());
        if ((enumRelativeSide != EnumRelativeSide.FRONT) && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        return super.getCapability(cap, side);
    }
}
