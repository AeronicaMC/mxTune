package net.aeronica.mods.mxtune.datafixers;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.aeronica.mods.mxtune.util.ValidDuration;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class SheetMusicFixer implements IFixableData
{
    @Override
    public int getFixVersion()
    {
        return Reference.MXTUNE_DATA_FIXER_VERSION;
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
                ValidDuration validDuration = fixSheetMusic(tag);
                ModLogger.info("SheetMusicFixer changed  \"MusicBook\" to \"SheetMusic\" Valid MML: %s, Duration: %s", validDuration.isValidMML(), SheetMusicUtil.formatDuration(validDuration.getDuration()));
            }
        }
        return compound;
    }

    public static ValidDuration fixSheetMusic(@Nonnull NBTTagCompound tag)
    {
        NBTTagCompound musicBook = tag.getCompoundTag("MusicBook");
        String mml = musicBook.getString("MML");

        NBTTagCompound sheetMusic = new NBTTagCompound();
        sheetMusic.setString("MML", mml);

        ValidDuration validDuration = SheetMusicUtil.validateMML(mml);
        sheetMusic.setInteger("Duration", validDuration.getDuration());
        tag.setTag("SheetMusic", sheetMusic);

        tag.removeTag("MusicBook");
        return validDuration;
    }
}
