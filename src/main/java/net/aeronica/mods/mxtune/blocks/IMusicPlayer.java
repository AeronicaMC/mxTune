package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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

    default String getMML(World worldIn, BlockPos blockPos)
    {
        StringBuilder buildMML = new StringBuilder();
        TileEntity te = getTE(worldIn, blockPos);

        try
        {
            for (int slot = 0; slot < ((TileBandAmp) te).getInventory().getSlots(); slot++)
            {
                ItemStack instrument = ((TileBandAmp) te).getInventory().getStackInSlot(slot);
                if (!instrument.isEmpty())
                {
                    ItemInstrument ii = (ItemInstrument) instrument.getItem();
                    int patch = ii.getPatch(instrument.getMetadata());
                    ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(instrument);
                    if (!sheetMusic.isEmpty())
                    {
                        NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("MusicBook");
                        if (contents != null)
                        {
                            String mml = contents.getString("MML");
                            mml = mml.replace("MML@", "MML@I" + patch);
                            buildMML.append(slot).append("=").append(mml).append("|");
                        }
                    }
                }
            }
        } catch (Exception e)
        {
            ModLogger.error(e);
        }
        return buildMML.toString();
    }
}
