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

package net.aeronica.mods.mxtune.gui.mml;

import com.google.common.io.Files;
import net.aeronica.libs.mml.readers.ms2mml.Ms2MmlReader;
import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.mxt.MXTuneFile;
import net.aeronica.mods.mxtune.mxt.MXTunePart;
import net.aeronica.mods.mxtune.mxt.MXTuneStaff;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImportHelper
{
    private static final int MAX_STAVES = 10;
    private ImportHelper() { /* NOP */ }

    @Nullable
    public static MXTuneFile importToMXTFile(@Nullable Path path)
    {
        if (path == null) return null;
        switch (getExtension(path))
        {
            case "ms2mml":
                return importMs2mml(path);
            case "mml":
                break;
            case "zip":
                break;
            default:
                return null;
        }
        return new MXTuneFile();
    }

    private static String getExtension(Path path)
    {
        return Files.getFileExtension(path.getFileName().toString().toLowerCase(Locale.ROOT));
    }

    @Nullable
    private static MXTuneFile importMs2mml(Path path)
    {
        Ms2MmlReader ms2MmlReader = new Ms2MmlReader();
        if (ms2MmlReader.parseFile(path))
        {
            String title = FileHelper.removeExtension(path.getFileName().toString());
            List<MXTuneStaff> staves = new ArrayList<>();
            MXTuneFile mxTuneFile = new MXTuneFile();
            int i = 0;
            for (String mml : ms2MmlReader.getMML().replaceAll("MML@|;", "").split(","))
            {
                if (i < MAX_STAVES)
                    staves.add(new MXTuneStaff(i++, mml));
                else
                    break;
            }
            mxTuneFile.setTitle(title);
            MXTunePart part = new MXTunePart("Acoustic Piano", "", 0, staves);
            mxTuneFile.getParts().add(part);
            mxTuneFile.applyUserDateTime(true);
            return mxTuneFile;
        } else
            return null;
    }
}
