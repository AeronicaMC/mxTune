/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.mxt;

import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Iterator;

public class MXTuneFileHelper
{
    private MXTuneFileHelper() { /* NOP */ }

    @Nullable
    public static MXTuneFile getMXTuneFile(@Nullable Path path)
    {
        MXTuneFile mxTuneFile = null;
        if (path != null)
        {
            NBTTagCompound compound = FileHelper.getCompoundFromFile(path);
            if (compound != null)
            {
                mxTuneFile = MXTuneFile.build(compound);
            }
        }
        ModLogger.debug("MXTuneFileHelper#getMXTuneFile version: %s", mxTuneFile == null ? "** file read failure **" : mxTuneFile.getMxtVersion());
        return mxTuneFile;
    }

    public static String getMML(MXTuneFile mxTuneFile)
    {
        StringBuilder builder = new StringBuilder();
        for (MXTunePart part : mxTuneFile.getParts())
        {
            // Check the MXT Version and deal with the legacy packed patch and the new SoundFontProxy id
            if (mxTuneFile.getMxtVersion().equalsIgnoreCase("1.0.0"))
                builder.append("MML@I=").append(SoundFontProxyManager.getIndexForFirstMatchingPackedPreset(part.getPackedPatch()));
            else if (mxTuneFile.getMxtVersion().equalsIgnoreCase("2.0.0"))
                builder.append("MML@I=").append(SoundFontProxyManager.getIndexById(part.getInstrumentName()));
            else
                builder.append("MML@");

            Iterator<MXTuneStaff> iterator = part.getStaves().iterator();
            while (iterator.hasNext())
            {
                builder.append(iterator.next().getMml());
                if (iterator.hasNext())
                    builder.append(",");
            }
            builder.append(";");
        }
        return builder.toString();
    }

    public static Song getSong(MXTuneFile tune)
    {
        return new Song(tune.getTitle(), getMML(tune));
    }

    public static SongProxy getSongProxy(MXTuneFile tune)
    {
        return getSongProxy(getSong(tune));
    }

    public static SongProxy getSongProxy(Song song)
    {
        NBTTagCompound compound = new NBTTagCompound();
        song.writeToNBT(compound);
        return new SongProxy(compound);
    }
}
