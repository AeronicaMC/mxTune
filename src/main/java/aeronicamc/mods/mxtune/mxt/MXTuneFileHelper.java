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

package aeronicamc.mods.mxtune.mxt;

import aeronicamc.mods.mxtune.caches.FileHelper;
import aeronicamc.mods.mxtune.util.MusicProperties;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.nbt.CompoundNBT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Iterator;

public class MXTuneFileHelper
{
    private static final Logger LOGGER = LogManager.getLogger(MXTuneFileHelper.class.getSimpleName());
    private MXTuneFileHelper() { /* NOP */ }

    @Nullable
    public static MXTuneFile getMXTuneFile(@Nullable Path path)
    {
        MXTuneFile mxTuneFile = null;
        if (path != null)
        {
            CompoundNBT compound = FileHelper.getCompoundFromFile(path);
            if (compound != null)
            {
                mxTuneFile = MXTuneFile.build(compound);
            }
        }
        LOGGER.debug("getMXTuneFile version: {}", mxTuneFile == null ? "** file read failure **" : mxTuneFile.getMxtVersion());
        return mxTuneFile;
    }

    public static MusicProperties getMusicProperties(MXTuneFile mxTuneFile)
    {
        StringBuilder builder = new StringBuilder();
        for (MXTunePart part : mxTuneFile.getParts())
        {
            // Check the MXT Version and deal with the legacy packed patch and the new SoundFontProxy id
            if (mxTuneFile.getMxtVersion().equalsIgnoreCase("1.0.0"))
                builder.append("MML@I=").append(SoundFontProxyManager.getIndexForFirstMatchingPackedPreset(part.getPackedPatch()));
            else if (mxTuneFile.getMxtVersion().equalsIgnoreCase("2.0.0"))
                builder.append("MML@I=").append(SoundFontProxyManager.getIndexById(part.getInstrumentId()));
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
        return new MusicProperties(builder.toString(), mxTuneFile.getDuration());
    }
//    public static Song getSong(MXTuneFile tune)
//    {
//        return new Song(tune.getTitle(), getMML(tune));
//    }
//
//    public static SongProxy getSongProxy(MXTuneFile tune)
//    {
//        return getSongProxy(getSong(tune));
//    }
//
//    public static SongProxy getSongProxy(Song song)
//    {
//        CompoundNBT compound = new CompoundNBT();
//        song.writeToNBT(compound);
//        return new SongProxy(compound);
//    }
}
