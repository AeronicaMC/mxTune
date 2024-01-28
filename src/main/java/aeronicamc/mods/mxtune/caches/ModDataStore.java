package aeronicamc.mods.mxtune.caches;

import aeronicamc.mods.mxtune.config.MXTuneConfig;
import aeronicamc.mods.mxtune.util.MXTuneException;
import net.minecraftforge.fml.LogicalSide;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static aeronicamc.mods.mxtune.caches.FileHelper.*;
import static net.minecraftforge.fml.LogicalSide.SERVER;


public class ModDataStore
{
    private static final Logger LOGGER = LogManager.getLogger(ModDataStore.class);
    private static final String SERVER_DATA_STORE_DUMP_FILENAME = "dump.txt";
    private static final ZoneId ROOT_ZONE = ZoneId.of("GMT0");
    private static final ArrayList<LocalDateTime> reapDateTimeKeyList = new ArrayList<>();
    private static LocalDateTime lastDateTime = LocalDateTime.now(ROOT_ZONE);

    public static void start()
    {
        reapSheetMusic();
    }

    private static List<Path> getSafeMusicPaths() {
        List<Path> gzPaths = new ArrayList<>();

        Path path = FileHelper.getDirectory(SERVER_FOLDER, SERVER);
        try (Stream<Path> stream = Files.walk(path)) {
            stream.forEach( entry-> {
                if (entry.getFileName().toString().matches("([0-9Tt])+\\.([gG])([zZ])"))
                    gzPaths.add(entry.getFileName());
            });
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return gzPaths;
    }

    /**
     * Test if the musicText exists in file storage.
     * @param localDateTime The key as a LocalDateTime object.
     * @return true if the musicText exists in file storage.
     */
    private static boolean musicTextExists(LocalDateTime localDateTime)
    {
        return FileHelper.fileExists(SERVER_FOLDER, toSafeFileNameKey(localDateTime.toString()), LogicalSide.SERVER);
    }

    public static int convertDumpToFiles()
    {
        AtomicInteger size = new AtomicInteger(0);
        if (fileExists(SERVER_MUSIC_FOLDER_DUMP_FOLDER, SERVER_DATA_STORE_DUMP_FILENAME, LogicalSide.SERVER))
        {
            try (Stream<String> stream = Files.lines(getCacheFile(SERVER_MUSIC_FOLDER_DUMP_FOLDER, SERVER_DATA_STORE_DUMP_FILENAME, LogicalSide.SERVER, true)))
            {
                LOGGER.info("Convert to Dump using: {}", SERVER_FOLDER );
                stream.forEach(line ->
                {
                    String[] pair = line.split("=");
                    // Tests if the localDateTime string is valid. Will throw an error is the parse fails.
                    LocalDateTime dateTime = (LocalDateTime.parse(pair[0]));
                    String filename = pair[0];
                    Path path;
                    try
                    {
                        filename = toSafeFileNameKey(dateTime);
                        String yearMonthFolders = String.format("%s/%4d/%02d", SERVER_FOLDER, dateTime.getYear(), dateTime.getMonthValue());
                        path = FileHelper.getCacheFile(yearMonthFolders, filename, LogicalSide.SERVER, true);

                        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(path));
                        gzipOutputStream.write(pair[1].getBytes(StandardCharsets.UTF_8));
                        gzipOutputStream.close();
                        LOGGER.info("  Created: {}", path);
                        size.getAndIncrement();
                    } catch (IOException e)
                    {
                        LOGGER.error("  failed to create file: {}", filename, e);
                    }

                });
            } catch (IOException | SecurityException | DateTimeParseException e)
            {
                LOGGER.error("convertDumpToFiles failed. Review <Save Folder>\\mxtune\\dump\\dump.txt for errors.\r\n"+
                        "Each line format is <DateTimeString>=<MML><EOL> with no breaks.\r\n"+
                        "Example: 2022-02-27T21:21:25.787=MML@t120v12l1rrrrrrrro4l4def+g;\r\n"+
                        "Each line can be very long as it represents either music for a single instrument or an entire song.\r\n"+
                        "Notepad++ is useful for reviewing and possibly fixing any issues such are removing unexpected line breaks, tabs, or spaces in a line.\r\n", e);
            }

        } else return size.get();
        return size.get();
    }

    /**
     * Returns the next available key ensuring it is not the same as previous keys whether recent in RAM or file storage.
     * @return next unused key
     */
    private static LocalDateTime nextKey()
    {
        LocalDateTime now;
        do {
            now = LocalDateTime.now(ROOT_ZONE);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.warn(e);
                Thread.currentThread().interrupt();
            }
        } while (now.equals(lastDateTime) || musicTextExists(now));
        lastDateTime = now;
        return now;
    }

    public static void removeSheetMusic(String musicIndex)
    {
        LocalDateTime localDateTime = null;
        try
        {
            localDateTime = LocalDateTime.parse(musicIndex);
        }
        catch (DateTimeParseException e)
        {
            LOGGER.warn("Invalid SheetMusic musicIndex. Can't remove SheetMusic mapping");
        }
        finally
        {
            if (localDateTime != null)
            {
                String filename = "";
                Path path = null;
                try
                {
                    filename = toSafeFileNameKey(localDateTime.toString());
                    String yearMonthFolders = String.format("%s/%d/%02d", SERVER_FOLDER, localDateTime.getYear(), localDateTime.getMonthValue());
                    path = FileHelper.getCacheFile(yearMonthFolders, filename, LogicalSide.SERVER, true);
                    if (Files.exists(path) && !path.toString().matches("\\.\\.|\\*"))
                        Files.delete(path);
                    else
                        LOGGER.warn("removeSheetMusic: WTH? use of .. or * not allowed! {}", path);
                }
                catch (IOException e)
                {
                    LOGGER.error("removeSheetMusic: unable to delete: {}", path == null ? "--not found--" : path, e);
                }
            }
        }
    }

    private static synchronized ArrayList<LocalDateTime> getReapDateTimeKeyList()
    {
        return reapDateTimeKeyList;
    }

    private static boolean canReapSheetMusic(LocalDateTime localDateTime)
    {
        LocalDateTime keyPlusDaysLeft = localDateTime.plusDays(MXTuneConfig.getSheetMusicLifeInDays());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("GMT0"));
        return Math.max((Duration.between(now, keyPlusDaysLeft).getSeconds() / 86400), 0) <= 0;
    }

    private static void reapSheetMusic()
    {
        getReapDateTimeKeyList().clear();
        AtomicLong reapCount = new AtomicLong();

        getSafeMusicPaths().forEach(p -> {
            LocalDateTime key = (keyFromSafeFileNameKey(p.getFileName().toString().replaceAll("\\.gz", "")));
            if (canReapSheetMusic(key))
                getReapDateTimeKeyList().add(key);
        });
        reapCount.set(getReapDateTimeKeyList().size());

        if (!getReapDateTimeKeyList().isEmpty() && MXTuneConfig.sheetMusicExpires())
        {
            // List and Reap
            LOGGER.info("Reaping {} music files if.", reapCount.get());
            for (LocalDateTime entry : getReapDateTimeKeyList()) {
                String filename = toSafeFileNameKey(entry.toString());
                LOGGER.info("    {}", filename);
                removeSheetMusic(entry.toString());
            }
        } else if (!getReapDateTimeKeyList().isEmpty())
        {
            // List only
            LOGGER.info("Could Reap {} music files if allowed to expire.", reapCount.get());
            for (LocalDateTime entry : getReapDateTimeKeyList()) {
                String filename = toSafeFileNameKey(entry.toString());
                LOGGER.info("    {}", filename);
            }
        } else
            LOGGER.info("No expired music files found.");
    }

    /**
     * Add an MML format sting to the store. Returns a unique date-time string as the key to the MML.
     * @param musicText - the MML music text string to be stored.
     * @return a unique date-time string (GMT0) as the key to the entry, or null if the add failed. e.g. "2022-02-27T21:21:25.787" or null
     */
    @Nullable
    public static String addMusicText(String musicText)
    {
        LocalDateTime key;
        key = nextKey();
        String filename = "--error--";
        Path path = null;
        try {
            filename = toSafeFileNameKey(key);
            String yearMonthFolders = String.format("%s/%d/%02d", SERVER_FOLDER, key.getYear(), key.getMonthValue());
            path = FileHelper.getCacheFile(yearMonthFolders, filename, LogicalSide.SERVER, true);
        } catch (IOException e)
        {
            LOGGER.error("  Path error: {}", filename, e);
            key = null;
        } finally {
            if (path != null)
                try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(path));)
                {
                    gzipOutputStream.write(musicText.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LOGGER.error("  Failed write: {}", filename, e);
                    key = null;
                }
        }
        return (key != null) ? key.toString() : null;
    }

    /**
     * Retrieves the MML text string for the given key or null if not found.
     * @param key - The key string in LocalDateTime(GMT0) format.
     * @return The MML text string for the given key or null if not found.
     */
    @Nullable
    public static String getMusicText(@Nullable String key)
    {
        String musicText = null;
        if (key != null)
        {
            Path path = null;
            String filename = "";
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(key);
                String yearMonthFolders = String.format("%s/%d/%02d", SERVER_FOLDER, localDateTime.getYear(), localDateTime.getMonthValue());
                filename = toSafeFileNameKey(key);
                path = FileHelper.getCacheFile(yearMonthFolders, filename, LogicalSide.SERVER, false);
            } catch (DateTimeParseException e) {
                LOGGER.error("getMusicText error on key parse: {}", key, e);
            } catch (IOException e){
                LOGGER.error("getMusicText error on file: {}", filename, e);
            }
            try {
                if (path != null && !Files.exists(path)) throw new MXTuneException("File does not exist: " + path);
            } catch (MXTuneException e) {
                LOGGER.error("getMusicText file error: {}", filename, e);
            }
            if (path != null)
                try (GZIPInputStream gzipInputStream = new GZIPInputStream(Files.newInputStream(path))) {
                    musicText = IOUtils.toString(gzipInputStream, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    LOGGER.error("getMusicText file error: {}", filename, e);
                    musicText = null;
                }
        }
        return musicText;
    }

    /**
     * Tests if the musicText exists
     * @param key in LocalDateTime (GMT0) string format
     * @return true if the musicText key resolves.
     */
    @Deprecated
    public static boolean hasMusicText(@Nullable String key)
    {
        return getMusicText(key) != null;
    }

    /**
     * Example: "2022-02-27T21:21:25.787" to "20220227T212125787.gz"
     * @param key a formatted date-time string with punctuation.
     * @return a formatted date-time string without punctuation appended with the .gz extension.
     */
    public static String toSafeFileNameKey(String key)
    {
        // 2022-02-27T21:21:25.787 to 20220227T212125787.gz
        return String.format("%s.gz", key.replaceAll("(\\.|:|-)", ""));
    }

    public static String toSafeFileNameKey(LocalDateTime localDateTime)
    {
        return toSafeFileNameKey(localDateTime.toString());
    }

    /**
     * Accepts SafeFileNameKey string and returns a LocalDateTime object
     * @param safeFileNameKey
     * @return a valid key or non-existent key from before this code was written.
     */
    public static LocalDateTime keyFromSafeFileNameKey(String safeFileNameKey)
    {
        // 20220227T212125787 to 2022-02-27T21:21:25.787
        LocalDateTime localDateTime;
        try
        {
            String temp = safeFileNameKey.subSequence(0, 4) +
                    "-" + safeFileNameKey.subSequence(4, 6) +
                    "-" + safeFileNameKey.subSequence(6, 11) +
                    ":" + safeFileNameKey.subSequence(11, 13) +
                    ":" + safeFileNameKey.subSequence(13, 15) +
                    "." + safeFileNameKey.subSequence(15, safeFileNameKey.length());
            localDateTime = LocalDateTime.parse(temp);
        } catch (DateTimeParseException | IndexOutOfBoundsException e)
        {
            LOGGER.warn(e);
            localDateTime = LocalDateTime.parse("1999-01-01T01:01:01.001");
        }
        return localDateTime;
    }
}
