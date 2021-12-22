package aeronicamc.mods.mxtune.gui.mml;

import aeronicamc.libs.mml.readers.mml3mle.MMLFile;
import aeronicamc.libs.mml.readers.ms2mml.MapMS2Instruments;
import aeronicamc.libs.mml.readers.ms2mml.Ms2MmlReader;
import aeronicamc.mods.mxtune.caches.FileHelper;
import aeronicamc.mods.mxtune.mxt.MXTuneFile;
import aeronicamc.mods.mxtune.mxt.MXTunePart;
import aeronicamc.mods.mxtune.mxt.MXTuneStaff;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static aeronicamc.mods.mxtune.util.SoundFontProxyManager.INSTRUMENT_DEFAULT_ID;


public class ImportHelper
{
    private static final Logger LOGGER = LogManager.getLogger(ImportHelper.class);
    private static final int MAX_STAVES = 10;
    private ImportHelper() { /* NOP */ }

    @Nullable
    public static MXTuneFile importToMXTFile(@Nullable Path path)
    {
        if (path != null)
            switch (getExtension(path))
            {
                case "ms2mml":
                    return importMs2mml(path);
                case "mml":
                    return MMLFile.parse(path);
                case "zip": // Only multi-part ms2mml supported at this time
                    return importZippedMs2mml(path);
                default:
            }
        return null;
    }

    private static String getExtension(Path path)
    {
        return Files.getFileExtension(path.getFileName().toString().toLowerCase(Locale.ROOT));
    }

    private static boolean hasExtension(String test, String ext)
    {
        return Files.getFileExtension(test).equalsIgnoreCase(ext);
    }

    @Nullable
    private static MXTuneFile importMs2mml(Path path)
    {
        Ms2MmlReader ms2MmlReader = new Ms2MmlReader();
        if (ms2MmlReader.parseFile(path))
        {
            String title = FileHelper.removeExtension(path.getFileName().toString());
            List<MXTuneStaff> staves = getStaves(ms2MmlReader.getMML());
            if (staves.isEmpty()) return null;
            MXTuneFile mxTuneFile = new MXTuneFile();
            mxTuneFile.setTitle(title);
            MXTunePart part = new MXTunePart(INSTRUMENT_DEFAULT_ID, "piano", 0, staves);
            mxTuneFile.getParts().add(part);
            return mxTuneFile;
        } else
            return null;
    }

    @Nullable
    private static MXTuneFile importZippedMs2mml(Path path)
    {
        if (path == null) return null;
        try (ZipFile file = new ZipFile(path.toString()))
        {
            String title = FileHelper.removeExtension(path.getFileName().toString());
            MXTuneFile mxTuneFile = new MXTuneFile();
            mxTuneFile.setTitle(title);
            Enumeration<? extends ZipEntry> entries = file.entries();
            LOGGER.debug("---- Zip File: {}", file.getName().substring(file.getName().lastIndexOf('\\')+1));
            int count = 0;
            while(entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && hasExtension(entry.getName(), "ms2mml"))
                {
                    Ms2MmlReader ms2MmlReader = new Ms2MmlReader();
                    LOGGER.debug("Ext: {}, File: {}, size: {}", Files.getFileExtension(entry.getName()), entry.getName(), entry.getSize());
                    InputStream is = file.getInputStream(entry);
                    if (ms2MmlReader.parseStream(is))
                    {
                        title = FileHelper.removeExtension(entry.getName());
                        String soundFontProxyID = MapMS2Instruments.getSoundFontProxyNameFromMeta(title);
                        int packedPatch = SoundFontProxyManager.getPackedPreset(soundFontProxyID);
                        List<MXTuneStaff> staves = getStaves(ms2MmlReader.getMML());
                        LOGGER.debug(String.format("Part: packedPatch %05d, sfpId: %s, meta: %s", packedPatch, soundFontProxyID, title));
                        if (!staves.isEmpty())
                        {
                            MXTunePart part = new MXTunePart(soundFontProxyID, title, packedPatch, staves);
                            mxTuneFile.getParts().add(part);
                            count++;
                        }
                    }
                }
            }
            return count > 0 ? mxTuneFile : null;

        } catch (IOException e)
        {
            LOGGER.error(e);
        }
        return null;
    }

    public static List<MXTuneStaff> getStaves(String data)
    {
        final List<MXTuneStaff> staves = new ArrayList<>();
        int i = 0;
        for (String mml : data.replaceAll("MML@|MML|;", "").split(","))
        {
            if (i < MAX_STAVES)
                staves.add(new MXTuneStaff(i++, mml));
            else
                break;
        }
        return staves;
    }
}
