package net.aeronica.mods.mxtune.blocks;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMusicPlayer
{
    /**
     * Get the TE associated with the block
     * @param worldIn
     * @param pos
     * @return
     */
    @SuppressWarnings("unchecked")
    default public <T extends TileInstrument> T getTE(World worldIn, BlockPos pos) {return (T) worldIn.getTileEntity(pos);}

    String getMML(World worldIn, BlockPos blockPos);
}
