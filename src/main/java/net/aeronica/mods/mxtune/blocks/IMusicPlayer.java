package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IMusicPlayer
{
    /**
     * Get the TE associated with the block
     * @param worldIn
     * @param pos
     * @return
     */
    @SuppressWarnings("unchecked")
    @Nullable
    default public <T extends TileInstrument> T getTE(World worldIn, BlockPos pos) {return (T) worldIn.getTileEntity(pos);}

    default String getMML(World worldIn, BlockPos blockPos)
    {
        StringBuilder buildMML = new StringBuilder();
        TileEntity te = getTE(worldIn, blockPos);

        if (te != null)
        try
        {
            for (int slot = 0; slot < ((TileBandAmp) te).getInventory().getSlots(); slot++)
            {
                ItemStack stackInSlot = ((TileBandAmp) te).getInventory().getStackInSlot(slot);
                if (!stackInSlot.isEmpty() && stackInSlot.getItem() instanceof ItemInstrument)
                {
                    ItemInstrument ii = (ItemInstrument) stackInSlot.getItem();
                    int patch = ii.getPatch(stackInSlot.getMetadata());
                    ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(stackInSlot);
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
