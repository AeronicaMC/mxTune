package aeronicamc.mods.mxtune.gui.mml;

import aeronicamc.libs.mml.parser.MMLAllowedChars;
import aeronicamc.libs.mml.readers.mml3mle.MMLFile;
import aeronicamc.libs.mml.readers.ms2mml.MapMS2Instruments;
import aeronicamc.libs.mml.readers.ms2mml.Ms2MmlReader;
import aeronicamc.mods.mxtune.caches.FileHelper;
import aeronicamc.mods.mxtune.mxt.MXTuneFile;
import aeronicamc.mods.mxtune.mxt.MXTunePart;
import aeronicamc.mods.mxtune.mxt.MXTuneStaff;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
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
                case "zip": // Only multipart .ms2mml supported at this time
                    return importZippedMs2mml(path);
                default:
            }
        return null;
    }

    private static String getExtension(Path path)
    {

        return FilenameUtils.getExtension(path.getFileName().toString().toLowerCase(Locale.ROOT));
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean hasExtension(String test, String ext)
    {
        return FilenameUtils.getExtension(test).equalsIgnoreCase(ext);
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

    // sonarcloud recommended solution to zip-bomb negation
    static final int THRESHOLD_ENTRIES = 20;  // 10-16 typical. 20 should never happen.
    static final int THRESHOLD_SIZE = 200000; // 200 KB
    static final double THRESHOLD_RATIO = 20.0D; // text can have high compression ratios

    @Nullable
    private static MXTuneFile importZippedMs2mml(Path path)
    {
        // sonarcloud recommended solution to zip-bomb negation
        int totalSizeArchive = 0;
        int totalEntryArchive = 0;
        double largestCompressionRatio = 0.0D;

        try (ZipFile file = new ZipFile(path.toString()))
        {
            String title = FileHelper.removeExtension(path.getFileName().toString());
            MXTuneFile mxTuneFile = new MXTuneFile();
            mxTuneFile.setTitle(title);
            Enumeration<? extends ZipEntry> entries = file.entries();
            int count = 0;

            // sonarcloud recommended solution to zip-bomb negation
            int nBytes = -1;
            byte[] buffer = new byte[2048];

            while (entries.hasMoreElements())
            {
                int totalSizeEntry = 0;
                ZipEntry entry = entries.nextElement();
                InputStream in = new BufferedInputStream(file.getInputStream(entry));

                String entryName = entry.getName();
                String entryExt = FilenameUtils.getExtension(entry.getName());
                long entrySize = entry.getSize();

                // sonarcloud recommended solution to zip-bomb negation
                totalEntryArchive ++;
                totalSizeEntry += nBytes;
                totalSizeArchive += nBytes;
                double compressionRatio = 0.0D;

                while((nBytes = in.read(buffer)) > 0) { // Compliant
                    totalSizeEntry += nBytes;
                    totalSizeArchive += nBytes;

                    compressionRatio = (double) totalSizeEntry / entry.getCompressedSize();
                    if(compressionRatio > THRESHOLD_RATIO) {
                        // ratio between compressed and uncompressed data is highly suspicious, looks like a Zip Bomb Attack
                        LOGGER.warn("Compression Ratio {} > {} Threshold Ratio", compressionRatio, THRESHOLD_RATIO);
                        break;
                    }
                }
                if (compressionRatio > largestCompressionRatio)
                    largestCompressionRatio = compressionRatio;

                if(totalSizeArchive > THRESHOLD_SIZE) {
                    // the uncompressed data size is too much for the application resource capacity
                    LOGGER.warn("Total Archive Size {} > {} Threshold Size", totalSizeArchive, THRESHOLD_SIZE);
                    break;
                }

                if(totalEntryArchive > THRESHOLD_ENTRIES) {
                    // too many entries in this archive, can lead to inodes exhaustion of the system
                    LOGGER.warn("Total Archive Entries {} >  {} Threshold Entries", totalEntryArchive, THRESHOLD_ENTRIES);
                    break;
                }

                if (!entry.isDirectory() && hasExtension(entry.getName(), "ms2mml"))
                {
                    Ms2MmlReader ms2MmlReader = new Ms2MmlReader();
                    InputStream is = file.getInputStream(entry);
                    if (ms2MmlReader.parseStream(is))
                    {
                        title = FileHelper.removeExtension(entry.getName());
                        String soundFontProxyID = MapMS2Instruments.getSoundFontProxyNameFromMeta(title);
                        int packedPatch = SoundFontProxyManager.getPackedPreset(soundFontProxyID);
                        List<MXTuneStaff> staves = getStaves(ms2MmlReader.getMML());
                        if (!staves.isEmpty())
                        {
                            MXTunePart part = new MXTunePart(soundFontProxyID, title, packedPatch, staves);
                            mxTuneFile.getParts().add(part);
                            count++;
                        }
                    }
                } else
                    LOGGER.warn("|-- Ext: {}, File: {}, size from file: {}, size read {}, Compression Ratio {}", entryExt, entryName, entrySize, totalSizeEntry, compressionRatio);
            }
            LOGGER.debug("| Largest Compression Ratio: {} < {} Threshold Ratio", largestCompressionRatio, THRESHOLD_RATIO);
            LOGGER.debug("| Total Archive Size:        {} < {} Threshold Size", totalSizeArchive, THRESHOLD_SIZE);
            LOGGER.debug("| Total Archive Entries:     {} <= {} Threshold Entries", totalEntryArchive, THRESHOLD_ENTRIES);
            if (count > 0)
                LOGGER.info("|---- Success! Found at least one ms2mml file.");
            else
                LOGGER.warn("|---- Load Failure! No ms2mml files found.");
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
                staves.add(new MXTuneStaff(i++, MMLAllowedChars.filter(mml, false)));
            else
                break;
        }
        return staves;
    }
}
