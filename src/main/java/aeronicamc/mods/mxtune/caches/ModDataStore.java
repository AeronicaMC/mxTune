package aeronicamc.mods.mxtune.caches;

import aeronicamc.libs.mml.util.TestData;
import aeronicamc.mods.mxtune.config.MXTuneConfig;
import aeronicamc.mods.mxtune.util.MXTuneRuntimeException;
import net.minecraftforge.fml.LogicalSide;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static aeronicamc.mods.mxtune.caches.FileHelper.*;
import static java.lang.Math.min;
import static net.minecraftforge.fml.LogicalSide.SERVER;

/**
 * A key-value data store for music in MML format. Used to offload the long string
 * data from SheetMusic ItemStacks and in world TileEntities.
 * Ref: {@link <A href="https://www.h2database.com/html/mvstore.html">H2 MVStore</A>}
 * Ref: {@link <A href="https://gamlor.info/posts-output/2019-09-23-mv-store-intro/en/">Intro to MVStore, an embedded key value store</A>}
 */
public class ModDataStore
{
    private static final Logger LOGGER = LogManager.getLogger(ModDataStore.class);

    private static final boolean USE_MV = false;
    private static final String SERVER_DATA_STORE_FILENAME = "music.mv";
    private static final String SERVER_DATA_STORE_DUMP_FILENAME = "dump.txt";
    private static final ZoneId ROOT_ZONE = ZoneId.of("GMT0");
    private static final ArrayList<LocalDateTime> reapDateTimeKeyList = new ArrayList<>();
    private static LocalDateTime lastDateTime = LocalDateTime.now(ROOT_ZONE);
    private static MVStore mvStore;

    // TODO: See about hooking the world gather event to force a commit of the music storage.
    // TODO: Setup a simple music storage backup and maintains only a user defined number of them.
    public static void start()
    {
        String pathFileName = String.format("Folder: '%s', Filename: '%s'", SERVER_FOLDER, SERVER_DATA_STORE_FILENAME);
        try
        {
            pathFileName = getCacheFile(SERVER_FOLDER, SERVER_DATA_STORE_FILENAME, LogicalSide.SERVER).toString();
            mvStore = new MVStore.Builder()
                    .fileName(pathFileName)
                    .compress()
                    .open();
        }
        catch (IOException | MVStoreException e)
        {
            LOGGER.error("Big OOPS here! Out of disk space? {}", pathFileName);
            LOGGER.error(e);
            throw new MXTuneRuntimeException("Unable to create mxtune data store.", e);
        }
        finally
        {
            if (getMvStore() != null)
                LOGGER.debug("MVStore Started. Commit Version: {}, file: {}", getMvStore().getCurrentVersion(), getMvStore().getFileStore());
        }
        testGet();
        long count = reapSheetMusic(true); // TODO: Remember to set whatIf to false for production!
        LOGGER.info("Reaped {} music file(s).", count);
    }

    @Deprecated
    public static void shutdown()
    {
        if (getMvStore() != null)
            getMvStore().close();
        mvStore = null;
        LOGGER.debug("MVStore Shutdown.");
    }

    @Nullable
    @Deprecated
    private static MVStore getMvStore()
    {
        return mvStore;
    }

    private static void testPut()
    {
        for (TestData c : TestData.values())
        {
           String index;
           if ((index = addMusicText(c.getMML())) == null)
               LOGGER.warn("Duplicate record: {}, musicText: {}", String.format("%s", index), c.getMML().substring(0, min(24, c.getMML().length())));
        }
    }

    private static void testGet()
    {
        int i = 0;
        if (USE_MV) {
            if (getMvStore() != null) {
                MVStore.TxCounter using = getMvStore().registerVersionUsage();
                MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
                for (Map.Entry<LocalDateTime, String> c : indexToMusicText.entrySet()) {
                    if (i++ >= 10) break;
                    LOGGER.debug("id: {}, musicText: {}", String.format("%s", c.getKey()), c.getValue().substring(0, min(24, c.getValue().length())));
                }

                LOGGER.debug("Last key: {}, Total records: {}", indexToMusicText.lastKey(), indexToMusicText.size());
                getMvStore().deregisterVersionUsage(using);
            }
        } else
        {
            List<Path> gzPaths = getSafeMusicPaths();
            gzPaths.subList(0, Math.min(gzPaths.size(), 10)).forEach(p -> LOGGER.debug("  musicText: {}", p));
            LOGGER.debug("  Showing {} of {} Music files.", Math.min(gzPaths.size(), 10), gzPaths.size());
        }
    }

    private static List<Path> getSafeMusicPaths() {
        List<Path> gzPaths = new ArrayList<>();
        Path path = FileHelper.getDirectory(SERVER_FOLDER, SERVER);
        PathMatcher filter = FileHelper.getGZMatcher(path);
        try (Stream<Path> paths = Files.list(path)) {
            gzPaths = paths
                    .filter(filter::matches)
                    .collect(Collectors.toList());
        } catch (NullPointerException | IOException e) {
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

    @Deprecated
    public static int dumpToFile()
    {
        int size = 0;
        if (getMvStore() != null)
        {
            try
            {
                String pathName = getCacheFile(SERVER_MUSIC_FOLDER_DUMP_FOLDER, SERVER_DATA_STORE_DUMP_FILENAME, LogicalSide.SERVER).toString();
                PrintWriter printWriter = new PrintWriter(pathName, "UTF-8");
                MVStore.TxCounter using = getMvStore().registerVersionUsage();
                MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
                size = indexToMusicText.size();
                for (Map.Entry<LocalDateTime, String> c : indexToMusicText.entrySet())
                {
                    printWriter.printf("%s=%s\n", c.getKey(), c.getValue());
                }
                printWriter.close();
                getMvStore().deregisterVersionUsage(using);
            } catch (IOException e)
            {
                LOGGER.error(e);
            }
        }
        return size;
    }

    @Deprecated
    public static int loadDumpFile()
    {
        int size = -1;
        if (getMvStore() != null)
        {
            if (fileExists(SERVER_MUSIC_FOLDER_DUMP_FOLDER, SERVER_DATA_STORE_DUMP_FILENAME, LogicalSide.SERVER))
            {
                MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
                try (Stream<String> stream = Files.lines(getCacheFile(SERVER_MUSIC_FOLDER_DUMP_FOLDER, SERVER_DATA_STORE_DUMP_FILENAME, LogicalSide.SERVER)))
                {
                    stream.forEach(line ->
                                   {
                                       String[] pair = line.split("=");
                                       LocalDateTime dateTime = (LocalDateTime.parse(pair[0]));
                                       indexToMusicText.putIfAbsent(dateTime, pair[1]);
                                   });
                    size = indexToMusicText.size();
                    getMvStore().commit();
                } catch (IOException | SecurityException | DateTimeParseException e)
                {
                    LOGGER.error(e);
                }
            }
            else return size;
        }
        return size;
    }

    public static int convertDumpToFiles()
    {
        AtomicInteger size = new AtomicInteger(0);
        if (fileExists(SERVER_MUSIC_FOLDER_DUMP_FOLDER, SERVER_DATA_STORE_DUMP_FILENAME, LogicalSide.SERVER))
        {
            try (Stream<String> stream = Files.lines(getCacheFile(SERVER_MUSIC_FOLDER_DUMP_FOLDER, SERVER_DATA_STORE_DUMP_FILENAME, LogicalSide.SERVER)))
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
                        filename = toSafeFileNameKey(dateTime.toString());
                        path = FileHelper.getCacheFile(SERVER_FOLDER, filename, LogicalSide.SERVER);
                        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(path));
                        gzipOutputStream.write(pair[1].getBytes(StandardCharsets.UTF_8));
                        gzipOutputStream.close();
                        LOGGER.info("  Created: {}", path.toString());
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
            if (USE_MV) {
                if (getMvStore() != null && localDateTime != null) {
                    MVStore.TxCounter using = getMvStore().registerVersionUsage();
                    MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
                    try {
                        indexToMusicText.remove(localDateTime);
                    } catch (ClassCastException | UnsupportedOperationException | NullPointerException e) {
                        LOGGER.error("removeSheetMusic: " + localDateTime, e);
                    }
                    getMvStore().deregisterVersionUsage(using);
                }
            } else if (localDateTime != null)
            {
                String filename = "";
                Path path = null;
                try
                {
                    filename = toSafeFileNameKey(localDateTime.toString());
                    path = FileHelper.getCacheFile(SERVER_FOLDER, filename, LogicalSide.SERVER);
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

    private static long reapSheetMusic(boolean whatIf)
    {
        getReapDateTimeKeyList().clear();
        AtomicLong reapCount = new AtomicLong();
        if (USE_MV) {
            if (getMvStore() != null) {
                MVStore.TxCounter using = getMvStore().registerVersionUsage();
                MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
                for (Map.Entry<LocalDateTime, String> entry : indexToMusicText.entrySet()) {
                    if (canReapSheetMusic(entry.getKey()))
                        getReapDateTimeKeyList().add(entry.getKey());
                }
                getMvStore().deregisterVersionUsage(using);
                reapCount.set(getReapDateTimeKeyList().size());
                if (!getReapDateTimeKeyList().isEmpty() && !whatIf) {
                    // List and Reap
                    for (LocalDateTime entry : getReapDateTimeKeyList()) {
                        LOGGER.info("Reap SheetMusic key: {}", entry);
                        indexToMusicText.remove(entry);
                    }
                    LOGGER.info("Reaped {} entries", getReapDateTimeKeyList().size());
                } else {
                    // whatIf is true: List only
                    for (LocalDateTime entry : getReapDateTimeKeyList()) {
                        LOGGER.info("Can Reap SheetMusic key: {}", entry);
                    }
                    LOGGER.info("{} entries could be reaped", getReapDateTimeKeyList().size());
                }
                getReapDateTimeKeyList().clear();
            }
        } else
        {
            // Gather files to reap
            getSafeMusicPaths().forEach(p -> {
                LocalDateTime key = (keyFromSafeFileNameKey(p.getFileName().toString().replaceAll("\\.gz", "")));
                if (canReapSheetMusic(key))
                {
                    getReapDateTimeKeyList().add(key);
                }
            });
            reapCount.set(getReapDateTimeKeyList().size());

            if (!getReapDateTimeKeyList().isEmpty() && !whatIf)
            {
                // List and Reap
                for (LocalDateTime entry : getReapDateTimeKeyList()) {
                    String filename = toSafeFileNameKey(entry.toString());
                    LOGGER.info("Reaped SheetMusic file: {}", filename);
                    removeSheetMusic(entry.toString());
                }
            } else
            {
                // List only
                for (LocalDateTime entry : getReapDateTimeKeyList()) {
                    String filename = toSafeFileNameKey(entry.toString());
                    LOGGER.info("Could reap SheetMusic file: {}", filename);
                }
            }
        }
        return reapCount.get();
    }

    /**
     * Add an MML format sting to the store. Returns a unique date-time string as the key to the MML.
     * @param musicText - the MML music text string to be stored.
     * @return a unique date-time string (GMT0) as the key to the entry, or null if the add failed. e.g. "2022-02-27T21:21:25.787" or null
     */
    @Nullable
    public static String addMusicText(String musicText)
    {
        LocalDateTime key = null;
        if (USE_MV) {
            if (getMvStore() != null) {
                key = nextKey();
                MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
                try {
                    indexToMusicText.put(key, musicText);
                    getMvStore().commit();
                } catch (UnsupportedOperationException | ClassCastException | NullPointerException |
                         IllegalArgumentException e) {
                    LOGGER.error("addMusicText: key: " + key.toString() + ", musicText: " + "", e);
                }
            }
        } else
        {
            // Write zipped music text
            key = nextKey();
            String filename = "--error--";
            try
            {
                filename = toSafeFileNameKey(key.toString());
                Path path = FileHelper.getCacheFile(SERVER_FOLDER, filename, LogicalSide.SERVER);
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(path));
                gzipOutputStream.write(musicText.getBytes(StandardCharsets.UTF_8));
                gzipOutputStream.close();
            } catch (IOException e)
            {
                LOGGER.error("  Failed write: {}", filename, e);
            }
        }
        return key != null ? key.toString() : null;
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
        if (USE_MV) {
            if (getMvStore() != null && key != null) {
                MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
                LocalDateTime localDateTime = LocalDateTime.parse(key);
                try {
                    musicText = indexToMusicText.get(localDateTime);
                } catch (ClassCastException | DateTimeParseException | NullPointerException e) {
                    LOGGER.error("getMusicText error or key : " + key, e);
                }
            }
        } else if (key != null)
        {
            // Read zipped music text
            try {
                String filename = toSafeFileNameKey(key);
                Path path = FileHelper.getCacheFile(SERVER_FOLDER, filename, LogicalSide.SERVER);
                GZIPInputStream gzipInputStream = new GZIPInputStream(Files.newInputStream(path));
                musicText = IOUtils.toString(gzipInputStream, StandardCharsets.UTF_8);
                gzipInputStream.close();
            } catch (IOException e) {
                LOGGER.error("getMusicText error or key : " + key, e);
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
