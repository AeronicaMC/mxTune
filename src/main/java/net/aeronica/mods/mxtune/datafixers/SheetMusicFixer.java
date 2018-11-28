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
    private static final String TAG_MUSIC_BOOK = "MusicBook";
    private static final String TAG_SHEET_MUSIC = "SheetMusic";
    private static final String KEY_MML = "MML";
    private static final String KEY_DURATION = "Duration";

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
            if (tag.hasKey(TAG_MUSIC_BOOK, Constants.NBT.TAG_COMPOUND))
            {
                ValidDuration validDuration = fixSheetMusic(tag);
                ModLogger.info("SheetMusicFixer changed  \"MusicBook\" to \"SheetMusic\" Valid MML: %s, Duration: %s", validDuration.isValidMML(), SheetMusicUtil.formatDuration(validDuration.getDuration()));
            }
        }
        return compound;
    }

    public static ValidDuration fixSheetMusic(@Nonnull NBTTagCompound tag)
    {
        NBTTagCompound musicBook = tag.getCompoundTag(TAG_MUSIC_BOOK);
        String mml = musicBook.getString(KEY_MML);

        NBTTagCompound sheetMusic = new NBTTagCompound();
        sheetMusic.setString(KEY_MML, mml);

        ValidDuration validDuration = SheetMusicUtil.validateMML(mml);
        sheetMusic.setInteger(KEY_DURATION, validDuration.getDuration());
        tag.setTag(TAG_SHEET_MUSIC, sheetMusic);

        tag.removeTag(TAG_MUSIC_BOOK);
        return validDuration;
    }
}
