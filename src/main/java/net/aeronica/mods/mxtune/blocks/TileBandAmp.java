package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileBandAmp extends TileInstrument
{
    public static final int MAX_SLOTS = 8;
    private boolean previousRedStoneState;
    private Integer playID;

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
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        previousRedStoneState = tag.getBoolean("powered");
        playID = tag.getInteger("play_id");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag.setBoolean("powered", this.previousRedStoneState);
        tag.setInteger("play_id", this.playID);
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
}
