package aeronicamc.mods.mxtune.caches;

import aeronicamc.libs.mml.util.TestData;
import aeronicamc.mods.mxtune.config.MXTuneConfig;
import aeronicamc.mods.mxtune.util.MXTuneRuntimeException;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Map;

import static aeronicamc.mods.mxtune.caches.FileHelper.SERVER_FOLDER;
import static aeronicamc.mods.mxtune.caches.FileHelper.getCacheFile;

/**
 * A key-value data store for music in MML format. Used to offload the long string
 * data from SheetMusic ItemStacks and in world TileEntities.
 * Ref: {@link <A href="https://www.h2database.com/html/mvstore.html">H2 MVStore</A>}
 * Ref: {@link <A href="https://gamlor.info/posts-output/2019-09-23-mv-store-intro/en/">Intro to MVStore, an embedded key value store</A>}
 */
public class ModDataStore
{
    private static final Logger LOGGER = LogManager.getLogger(ModDataStore.class);

    private static final String SERVER_DATA_STORE_FILENAME = "music.mv";
    private static final ZoneId ROOT_ZONE = ZoneId.of("GMT0");
    private static final ArrayList<LocalDateTime> reapDateTimeKeyList = new ArrayList<>();
    private static LocalDateTime lastDateTime = LocalDateTime.now(ROOT_ZONE);
    private static MVStore mvStore;

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
        catch (IOException e)
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
    }

    public static void shutdown()
    {
        if (getMvStore() != null)
            getMvStore().close();
        mvStore = null;
        LOGGER.debug("MVStore Shutdown.");
    }

    @Nullable
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
               LOGGER.warn("Duplicate record: {}, musicText: {}", String.format("%s", index), c.getMML().substring(0, Math.min(24, c.getMML().length())));
        }
    }

    private static void testGet()
    {
        if (getMvStore() != null)
        {
            MVStore.TxCounter using = getMvStore().registerVersionUsage();
            MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
            for (Map.Entry<LocalDateTime, String> c : indexToMusicText.entrySet())
            {
                LOGGER.debug("id: {}, musicText: {}", String.format("%s", c.getKey()), c.getValue().substring(0, Math.min(24, c.getValue().length())));
            }

            LOGGER.debug("Last key: {}", indexToMusicText.lastKey());
            getMvStore().deregisterVersionUsage(using);
        }
    }

    private static LocalDateTime nextKey()
    {
        LocalDateTime now;
        do {
            now = LocalDateTime.now(ROOT_ZONE);
        } while (now.equals(lastDateTime));
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
            if (getMvStore() != null && localDateTime != null)
            {
                MVStore.TxCounter using = getMvStore().registerVersionUsage();
                MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
                try
                {
                    indexToMusicText.remove(localDateTime);
                }
                catch (ClassCastException | UnsupportedOperationException | NullPointerException e)
                {
                    LOGGER.error("removeSheetMusic: " + localDateTime.toString(), e);
                }
                getMvStore().deregisterVersionUsage(using);
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
        long reapCount = 0;
        if (getMvStore() != null)
        {
            MVStore.TxCounter using = getMvStore().registerVersionUsage();
            MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
            for (Map.Entry<LocalDateTime, String> entry : indexToMusicText.entrySet())
            {
                if (canReapSheetMusic(entry.getKey()))
                    getReapDateTimeKeyList().add(entry.getKey());
            }
            getMvStore().deregisterVersionUsage(using);
            reapCount = getReapDateTimeKeyList().size();
            if (!getReapDateTimeKeyList().isEmpty() && !whatIf)
            {
                // List and Reap
                for(LocalDateTime entry : getReapDateTimeKeyList())
                {
                    LOGGER.info("Reap SheetMusic key: {}", entry);
                    indexToMusicText.remove(entry);
                }
                LOGGER.info("Reaped {} entries", getReapDateTimeKeyList().size());
            }
            else
            {
                // whatIf is true: List only
                for(LocalDateTime entry : getReapDateTimeKeyList())
                {
                    LOGGER.info("Can Reap SheetMusic key: {}", entry);
                }
                LOGGER.info("{} entries could be reaped", getReapDateTimeKeyList().size());
            }
            getReapDateTimeKeyList().clear();
        }
        return reapCount;
    }

    /**
     * Add add a MML format sting to the store. Returns a unique date-time string as the key to the MML.
     * (At least providing there are no unexpected time shifts due to incorrect time on the server/pc etc.)
     * @param musicText - the MML music text string to be stored.
     * @return a unique date-time string (GMT0) as the key to the entry, or null if the add failed.
     */
    @Nullable
    public static String addMusicText(String musicText)
    {
        LocalDateTime key = null;
        if (getMvStore() != null)
        {
            key = nextKey();
            MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
            try
            {
                indexToMusicText.put(key, musicText);
                getMvStore().commit();
            }
            catch (UnsupportedOperationException | ClassCastException | NullPointerException | IllegalArgumentException e)
            {
                LOGGER.error("addMusicText: key: " + key.toString() + ", musicText: " + "", e);
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
    public static String getMusicText(String key)
    {
        String musicText = null;
        if (getMvStore() != null)
        {
            MVMap<LocalDateTime, String> indexToMusicText = getMvStore().openMap("MusicTexts");
            LocalDateTime localDateTime = LocalDateTime.parse(key);
            try
            {
                musicText = indexToMusicText.get(localDateTime);
            }
            catch (ClassCastException | NullPointerException e)
            {
                LOGGER.error("getMusicText error or key : " + key, e);
            }
        }
        return musicText;
    }

}
