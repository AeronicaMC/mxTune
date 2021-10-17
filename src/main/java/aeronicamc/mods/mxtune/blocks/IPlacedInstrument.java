package aeronicamc.mods.mxtune.blocks;

public interface IPlacedInstrument
{
    /**
     * Get the TE associated with the block
     * @param worldIn a world instance
     * @param pos position of the TE
     * @return instance of the TE
     */
//    @SuppressWarnings("unchecked")
//    default <T extends TileInstrument> T getTE(World worldIn, BlockPos pos) {return (T) worldIn.getTileEntity(pos);}
    
    /**
     * Get the patch for this placed instrument
     * @return GM 1 Patch in the range of 1-127
     */
    int getPatch();
}
