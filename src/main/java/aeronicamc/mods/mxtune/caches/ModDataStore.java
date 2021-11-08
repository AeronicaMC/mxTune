package aeronicamc.mods.mxtune.caches;

import aeronicamc.libs.mml.util.TestData;
import aeronicamc.mods.mxtune.util.MXTuneRuntimeException;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.io.IOException;
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
    private static MVStore mvStore;

    public static void start()
    {
        try
        {
            mvStore = new MVStore.Builder()
                    .fileName(getCacheFile(SERVER_FOLDER, SERVER_DATA_STORE_FILENAME, LogicalSide.SERVER).toString())
                    .compress()
                    .open();
        }
        catch (IOException e)
        {
            LOGGER.error("Big OOPS here! Out of disk space? No write permissions?");
            LOGGER.error(e);
            throw new MXTuneRuntimeException("Unable to create mxtune data store.", e);
        }
        finally
        {
            if (mvStore != null)
                LOGGER.debug("MVStore version: {}, file: {}", mvStore.getCurrentVersion(), mvStore.getFileStore());
        }
        testPut();
        testGet();
    }

    public static void shutdown()
    {
        if (mvStore != null)
            mvStore.close();
    }

    public static MVStore getMvStore()
    {
        return mvStore;
    }

    public static void testPut()
    {
        int index = 0;

       MVMap<Integer, String> idToMusicText = mvStore.openMap("MusicTexts");
       for (TestData c : TestData.values())
       {
           if (idToMusicText.putIfAbsent(c.getIndex(), c.getMML()) != null)
               LOGGER.warn("Duplicate record: {}", c.getIndex(), c.getMML().substring(0, Math.min(24, c.getMML().length())));
       }
       mvStore.commit();
       idToMusicText.replace(1, "-removed-");
    }

    public static void testGet()
    {
        int index = 0;

        MVMap<Integer, String> idToMusicText = mvStore.openMap("MusicTexts");
        for (Map.Entry<Integer, String> c : idToMusicText.entrySet())
        {
            LOGGER.debug("id: {}, musicText: {}", String.format("%02d", c.getKey()), c.getValue().substring(0, Math.min(24, c.getValue().length())));
        }

        LOGGER.debug("Last key: {}", idToMusicText.lastKey());
        LOGGER.debug("Contains -removed-? {}", idToMusicText.containsValue("-removed-"));
    }
}
