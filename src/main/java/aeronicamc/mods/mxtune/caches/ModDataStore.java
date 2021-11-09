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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static final AtomicInteger nextIndex = new AtomicInteger();

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
            LOGGER.error("Big OOPS here! Out of disk space?");
            LOGGER.error(e);
            throw new MXTuneRuntimeException("Unable to create mxtune data store.", e);
        }
        finally
        {
            if (mvStore != null)
                LOGGER.debug("MVStore version: {}, file: {}", mvStore.getCurrentVersion(), mvStore.getFileStore());
        }
        initializeIndex();
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
        MVMap<Integer, String> indexToMusicText = mvStore.openMap("MusicTexts");
        for (TestData c : TestData.values())
        {
           Integer index;
           if ((index = addSheetMusic(c.getMML())) == null)
               LOGGER.warn("Duplicate record: {}, musicText: {}", String.format("%02d", index), c.getMML().substring(0, Math.min(24, c.getMML().length())));
        }
        removeSheetMusic(2);
        removeSheetMusic(5);
        printReusableKeys();
    }



    public static void testGet()
    {
        int index = 0;

        MVMap<Integer, String> indexToMusicText = mvStore.openMap("MusicTexts");
        for (Map.Entry<Integer, String> c : indexToMusicText.entrySet())
        {
            LOGGER.debug("id: {}, musicText: {}", String.format("%02d", c.getKey()), c.getValue().substring(0, Math.min(24, c.getValue().length())));
        }

        LOGGER.debug("Last key: {}", indexToMusicText.lastKey());
        LOGGER.debug("Contains -removed-? {}", indexToMusicText.containsValue("-removed-"));
    }

    public static void initializeIndex()
    {
        if (mvStore != null)
        {
            MVMap<Integer, String> indexToMusicText = mvStore.openMap("MusicTexts");
            nextIndex.set(indexToMusicText.lastKey() == null ? 0 : indexToMusicText.lastKey());
            MVMap<String, Set<Integer>> indexToReUsableKey = mvStore.openMap("ReUsableMusicIndices");
            if (indexToReUsableKey.isEmpty()){
                indexToReUsableKey.putIfAbsent("ReUsableKeys", new HashSet<>());
            }
        }
    }

    private static void printReusableKeys()
    {
        if (mvStore != null)
        {
            MVMap<String, Set<Integer>> indexToReUsableKey = mvStore.openMap("ReUsableMusicIndices");
            Set<Integer> keySet = indexToReUsableKey.get("ReUsableKeys");
            keySet.forEach(key -> LOGGER.debug(" available key: {}", key));
        }
    }

    @Nullable
    private static Integer nextKey()
    {
        Integer newKey = null;
        if (mvStore != null)
        {
            MVMap<String, Set<Integer>> indexToReUsableKey = mvStore.openMap("ReUsableMusicIndices");
            Set<Integer> keySet = indexToReUsableKey.get("ReUsableKeys");
            if (keySet.isEmpty())
            {
                newKey = nextIndex.getAndIncrement();
            }
            else
            {
                Iterator<Integer> iterator = keySet.iterator();
                newKey = iterator.next();
                iterator.remove();
            }
        }
        return newKey;
    }


    public static void removeSheetMusic(@Nullable Integer musicIndex)
    {
        if (musicIndex != null && mvStore != null)
        {
            MVMap<String, Set<Integer>> indexToReUsableKey = mvStore.openMap("ReUsableMusicIndices");
            MVMap<Integer, String> indexToMusicText = mvStore.openMap("MusicTexts");
            Set<Integer> keySet = indexToReUsableKey.get("ReUsableKeys");
            if (indexToMusicText.containsKey(musicIndex))
            {
                indexToMusicText.replace(musicIndex, "-removed-");
                keySet.add(musicIndex);
                mvStore.commit();
            }
        }
    }

    @Nullable
    public static Integer addSheetMusic(String musicText)
    {
        Integer key = null;
        if (mvStore != null)
        {
            key = nextKey();
            MVMap<Integer, String> indexToMusicText = mvStore.openMap("MusicTexts");
            if (indexToMusicText.containsKey(key))
            {
                indexToMusicText.replace(key, musicText);
            }
            else
            {
                indexToMusicText.put(key, musicText);
            }
        }
        return key;
    }

    @Nullable
    public static String getMusicText(int key)
    {
        String musicText = null;
        MVMap<Integer, String> indexToMusicText;
        if (mvStore != null)
        {
            musicText = (indexToMusicText = mvStore.openMap("MusicTexts")).get(key);

        }
        return musicText;
    }

}
