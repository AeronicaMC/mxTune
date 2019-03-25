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

package net.aeronica.mods.mxtune.managers;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.managers.records.*;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.GetServerDataMessage;
import net.aeronica.mods.mxtune.util.MXTuneRuntimeException;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.aeronica.mods.mxtune.network.bidirectional.GetServerDataMessage.GetType;

public class ClientFileManager
{
    private static UUID cachedServerID = Reference.EMPTY_UUID;
    private static Minecraft mc = Minecraft.getMinecraft();

    private static final String DIR_AREAS = "areas";
    private static final String DIR_PLAYLISTS = "playlists";
    private static final String DIR_MUSIC = "music";
    private static final String DIR_PCM = "pcm";

    private static Path pathCacheParent;
    private static Path pathAreas;
    private static Path pathPlayLists;
    private static Path pathMusic;
    private static Path pathPCM;

    private static Map<UUID, Area> mapAreas = new HashMap<>();
    private static Map<UUID, PlayList> mapPlayLists = new HashMap<>();
    private static Map<UUID, SongProxy> mapMusic = new HashMap<>();
    private static Map<UUID, Path> mapPCM = new HashMap<>();
    private static Set<UUID> badAreas = new HashSet<>();
    private static Set<UUID> badPlayLists = new HashSet<>();
    private static Set<UUID> badMusic = new HashSet<>();
    private static Deque<UUID> musicQueue = new ArrayDeque<>();

    private static boolean waitArea = false;
    private static boolean waitPlayList = false;
    private static boolean waitMusic = false;


    // TODO: Client side server cache folders must be per server and player (integrated AND dedicated)
    // This must be done to avoid potential naming conflicts.
    private ClientFileManager() { /* NOP*/ }

    /**
     * Sets the cached server ID for the server the client logged onto. This is an mxTune feature for internal use.
     * Called when the client logs on to the server as part of the ClientStateData query.
     * @param msb UUID MSB
     * @param lsb UUID LSB
     */
    public static void setCachedServerID(long msb, long lsb)
    {
        cachedServerID = new UUID(msb, lsb);
        ModLogger.debug("Cached Server ID received: %s", cachedServerID.toString());
        createClientSideCacheDirectories();
        loadCache(pathAreas, mapAreas, Area.class);
        loadCache(pathPlayLists, mapPlayLists, PlayList.class);
        loadCache(pathMusic, mapMusic, SongProxy.class);
        badAreas.clear();
        badPlayLists.clear();
        badMusic.clear();
        ModLogger.debug("Cache loaded");
    }

    /**
     * <p>Build a unique client side server data cache directory by player and server.</p>
     * e.g. &lt;game folder&gt;/mxtune/server_cache/&lt;player UUID&gt/&lt;server UUID&gt;
     */
    private static void createClientSideCacheDirectories()
    {
        UUID playerUniqueID = mc.player.getUniqueID();
        Path clientSidePlayerServerCachePath = Paths.get(FileHelper.CLIENT_SERVER_CACHE_FOLDER, playerUniqueID.toString(), getCachedServerID().toString());
        Path pathCacheParent = FileHelper.getDirectory(clientSidePlayerServerCachePath.toString(), Side.CLIENT);
        pathAreas = getSubDirectory(clientSidePlayerServerCachePath.toString(), DIR_AREAS);
        pathPlayLists = getSubDirectory(clientSidePlayerServerCachePath.toString(), DIR_PLAYLISTS);
        pathMusic = getSubDirectory(clientSidePlayerServerCachePath.toString(), DIR_MUSIC);
        pathPCM = getSubDirectory(clientSidePlayerServerCachePath.toString(), DIR_PCM);
    }

    private static Path getSubDirectory(String parent, String child)
    {
        Path join = Paths.get(parent, child);
        return FileHelper.getDirectory(join.toString(), Side.CLIENT);
    }

    private static UUID getCachedServerID()
    {
        if (Reference.EMPTY_UUID.equals(cachedServerID))
            throw new MXTuneRuntimeException("EMPTY_UUID detected! Something is seriously wrong.");
        return cachedServerID;
    }

    public static void addArea(UUID uuid, NBTTagCompound data, boolean error)
    {
        if (error)
        {
            addBadArea(uuid);
            return;
        }
        Path path;
        Area area = new Area();
        area.readFromNBT(data);
        mapAreas.put(uuid, area);
        try
        {
            path = FileHelper.getCacheFile(pathAreas.toString(), uuid.toString() + ".dat", Side.CLIENT);
        }
        catch (IOException e)
        {
            ModLogger.error(e);
            ModLogger.error("Unable to write Area file: %s to cache folder: %s", uuid.toString() + "dat", pathAreas.toString());
            return;
        }
        waitArea = false;
        FileHelper.sendCompoundToFile(path, data);
    }

    public static void addPlayList(UUID uuid, NBTTagCompound data, boolean error)
    {
        if (error)
        {
            addBadPlayList(uuid);
            return;
        }
        Path path;
        PlayList playList = new PlayList();
        playList.readFromNBT(data);
        mapPlayLists.put(uuid, playList);
        try
        {
            path = FileHelper.getCacheFile(pathPlayLists.toString(), uuid.toString() + ".dat", Side.CLIENT);
        }
        catch (IOException e)
        {
            ModLogger.error(e);
            ModLogger.error("Unable to write PlayList file: %s to cache folder: %s", uuid.toString() + "dat", pathPlayLists.toString());
            return;
        }
        waitPlayList = false;
        FileHelper.sendCompoundToFile(path, data);
    }

    public static void addMusic(UUID uuid, NBTTagCompound data, boolean error)
    {
        if (error)
        {
            addBadMusic(uuid);
            return;
        }
        Path path;
        if (!mapMusic.containsKey(uuid))
        {
            SongProxy proxy = new SongProxy();
            proxy.readFromNBT(data);
            mapMusic.put(uuid, proxy);
        }
        Boolean fileExists = FileHelper.fileExists(pathMusic.toString(), uuid.toString() + ".dat", Side.CLIENT);
        if (!fileExists)
        {
            try
            {
                path = FileHelper.getCacheFile(pathMusic.toString(), uuid.toString() + ".dat", Side.CLIENT);
            } catch (IOException e)
            {
                ModLogger.error(e);
                ModLogger.error("Unable to write Music file: %s to cache folder: %s", uuid.toString() + "dat", pathMusic.toString());
                return;
            }
            waitMusic = false;
            FileHelper.sendCompoundToFile(path, data);
        }
    }

    static boolean hasMusic(UUID uuidMusic)
    {
        return mapMusic.containsKey(uuidMusic);
    }

    private static void addBadArea(UUID badUuid)
    {
        badAreas.add(badUuid);
    }

    private static void addBadPlayList(UUID badUuid)
    {
        badPlayLists.add(badUuid);
    }

    private static void addBadMusic(UUID badUuid)
    {
        badMusic.add(badUuid);
    }

    private static boolean isNotBadArea(UUID uuid)
    {
        return !badAreas.contains(uuid);
    }

    static boolean isNotBadPlayList(UUID uuid)
    {
        return !badPlayLists.contains(uuid);
    }

    static boolean isNotBadMusic(UUID uuid)
    {
        return !badMusic.contains(uuid);
    }

    private static <T extends BaseData> void loadCache(Path loc, Map<UUID, T> map, Class<T> type)
    {
        List<Path> files = new ArrayList<>();
        map.clear();
        Path path = FileHelper.getDirectory(loc.toString(), Side.CLIENT);
        PathMatcher filter = FileHelper.getDatMatcher(path);
        try (Stream<Path> paths = Files.list(path))
        {
            files = paths
                    .filter(filter::matches)
                    .collect(Collectors.toList());
        }
        catch (NullPointerException | IOException e)
        {
            ModLogger.error(e);
        }

        for (Path file : files)
        {
            NBTTagCompound compound = FileHelper.getCompoundFromFile(file);
            if (compound != null)
                try
                {
                    T data = type.newInstance();
                    data.readFromNBT(compound);
                    UUID uuid = data.getUUID();
                    map.put(uuid, data);
                } catch (InstantiationException | IllegalAccessException e)
                {
                    ModLogger.error(e);
                    ModLogger.error("What did you do? What's this thing?: %s", type.getSimpleName());
                    throw new MXTuneRuntimeException(e);
                }
        }
    }

    // This is called every tick so after at least one to two ticks a song should be available
    // If no local cache files exist then the area/playlist will get DL and cached. On subsequent ticks
    // the song itself will be chosen and made available
    static boolean songAvailable(UUID uuidArea)
    {
        // META: What to do?

        // Check local cache for area/playlist
        // if area/playlist not exists
        //     DL area/playlist and cache to collection and disk (async threaded flush)
        // else
        //     choose song based on <Area> settings and <playlist>
        //     if song not cached
        //         DL and cache
        //     else
        //         get song from cache
        // queue song for next play and set songAvailable to true.
        return resolveArea(uuidArea) && isNotBadArea(uuidArea) && !waitArea && !waitPlayList && !waitMusic;
    }

    private static boolean resolveArea(UUID uuidArea)
    {
        if (mapAreas.containsKey(uuidArea))
        {
            resolvePlayList(mapAreas.get(uuidArea).getPlayList());
            waitArea = false;
            return true;
        }
        else
        {
            if (!Reference.EMPTY_UUID.equals(uuidArea) && isNotBadArea(uuidArea))
            {
                waitArea = true;
                waitPlayList = true;
                PacketDispatcher.sendToServer(new GetServerDataMessage(uuidArea, GetType.AREA));
            }
            return false;
        }
    }

    private static boolean resolvePlayList(UUID uuidPlayList)
    {
        if (mapPlayLists.containsKey(uuidPlayList))
        {
            waitPlayList = false;
            return true;
        }
        else
        {
            if (!Reference.EMPTY_UUID.equals(uuidPlayList) && isNotBadPlayList(uuidPlayList))
            {
                waitPlayList = true;
                PacketDispatcher.sendToServer(new GetServerDataMessage(uuidPlayList, GetType.PLAY_LIST));
            }
            return false;
        }
    }

    @Nullable
    static PlayList getAreaPlayList(UUID uuidArea)
    {
        if (resolveArea(uuidArea))
        {
            UUID uuidPlayList = mapAreas.get(uuidArea).getPlayList();
            if (resolvePlayList(uuidPlayList))
                return mapPlayLists.get(uuidPlayList);
        }
        return null;
    }

    @Nullable
    static Song getMusicFromCache(UUID uuid)
    {
        if (mapMusic.containsKey(uuid) && isNotBadMusic(uuid))
        {
            try
            {
                Path path = FileHelper.getCacheFile(pathMusic.toString(), uuid.toString() + ".dat", Side.CLIENT);
                NBTTagCompound compound = FileHelper.getCompoundFromFile(path);
                Song song = new Song();
                song.readFromNBT(compound);
                return song;
            } catch (IOException e)
            {
                ModLogger.error(e);
                Path path = Paths.get(pathMusic.toString(), uuid.toString() + ".dat");
                ModLogger.error("Unable to read file: " + path);
            }
        }
        return null;
    }
}
