package aeronicamc.mods.mxtune.caches;

import aeronicamc.libs.mml.util.TestData;
import aeronicamc.mods.mxtune.util.MXTuneRuntimeException;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
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
                LOGGER.debug("MVStore version: {}, file: {}", getMvStore().getCurrentVersion(), getMvStore().getFileStore());
        }
        testGet();
    }

    public static void shutdown()
    {
        if (getMvStore() != null)
            getMvStore().close();
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

    public static void main(String[] args) throws Exception
    {
        LocalDate localDate = LocalDate.now(ROOT_ZONE);
        LOGGER.info("local date:      {}", localDate.toString());
        LocalDate future = localDate.plusDays(30);
        LOGGER.info("future date:     {}", future.toString());
        LOGGER.info("Difference:      {}" , localDate.isBefore(future));
        LOGGER.info("----");

//        for (String zoneId : ZoneId.getAvailableZoneIds())
//        {
//            LOGGER.info(zoneId);
//        }
//        LOGGER.info("----");

        LocalDateTime localDateTime = LocalDateTime.now(ROOT_ZONE);
        LOGGER.info("local date Time: {}", localDateTime.toString());
        LOGGER.info("----");

        int i;
        for (i=0 ; i<10; i++)
        {
            LOGGER.info("Unique Timestamp:    {}", getNextDateTime());
        }
    }

    private static LocalDateTime getNextDateTime()
    {
        LocalDateTime now;
        do {
            now = LocalDateTime.now(ROOT_ZONE);
        } while (now.equals(lastDateTime));
        lastDateTime = now;
        return now;
    }

}
