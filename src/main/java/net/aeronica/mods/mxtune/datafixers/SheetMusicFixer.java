package net.aeronica.mods.mxtune.datafixers;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class SheetMusicFixer implements IFixableData
{
    @Override
    public int getFixVersion()
    {
        return MXTuneMain.MXTUNE_DATA_FIXER_VERSION;
    }

    @Nonnull
    @Override
    public NBTTagCompound fixTagCompound(@SuppressWarnings("NullableProblems") NBTTagCompound compound)
    {
        boolean isSheetMusic = compound.getString("id").equalsIgnoreCase("mxtune:sheet_music");
        if (isSheetMusic)
        {
            NBTTagCompound tag = compound.getCompoundTag("tag");
            if (tag.hasKey("MusicBook", Constants.NBT.TAG_COMPOUND))
            {
                NBTTagCompound musicBook = tag.getCompoundTag("MusicBook");
                String mml = musicBook.getString("MML");

                NBTTagCompound sheetMusic = new NBTTagCompound();
                sheetMusic.setString("MML", mml);

                Tuple<Boolean, Integer> validTime = SheetMusicUtil.validateMML(mml);
                sheetMusic.setInteger("Duration", validTime.getSecond());
                tag.setTag("SheetMusic", sheetMusic);

                tag.removeTag("MusicBook");
                ModLogger.info("SheetMusicFixer changed  \"MusicBook\" to \"SheetMusic\" Valid MML: %s, Duration: %s", validTime.getFirst(), SheetMusicUtil.formatDuration(validTime.getSecond()));
            }
        }
        return compound;
    }
}
