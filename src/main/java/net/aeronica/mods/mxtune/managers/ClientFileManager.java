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
import net.aeronica.mods.mxtune.managers.records.Area;
import net.aeronica.mods.mxtune.managers.records.BaseData;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.GetServerDataMessage;
import net.aeronica.mods.mxtune.util.CallBack;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.MXTuneRuntimeException;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
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

public enum  ClientFileManager implements CallBack
{
    INSTANCE;

    private static UUID cachedServerID = Reference.EMPTY_UUID;
    private static Minecraft mc = Minecraft.getMinecraft();

    private static final String DIR_AREAS = "areas";
    private static final String DIR_MUSIC = "music";

    private static Path pathAreas;
    private static Path pathMusic;

    private static final Map<GUID, Area> mapAreas = new HashMap<>();
    private static Map<GUID, SongProxy> mapSongProxies = new HashMap<>();
    private static Set<GUID> badAreas = new HashSet<>();
    private static Set<GUID> badSongs = new HashSet<>();

    private static boolean waitArea = false;
    private static boolean waitSong = false;

    private static UUID cachedPlayerUUID;

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
        // No need to retrieve from cache since we pull down each session and clear the cache on close.
        // load Cache (pathAreas, mapAreas, Area.class) ;
        // load Cache (pathMusic, mapSongProxies, SongProxy.class );
        badAreas.clear();
        badSongs.clear();
        ModLogger.debug("Cache loaded");
    }

    public static void clearCache()
    {
        if (!cachedPlayerUUID.equals(Reference.EMPTY_UUID) && !cachedServerID.equals(Reference.EMPTY_UUID))
        {
            clearCache(pathAreas);
            clearCache(pathMusic);
        }
    }

    /**
     * <p>Build a unique client side server data cache directory by player and server.</p>
     * e.g. &lt;game folder&gt;/mxtune/server_cache/&lt;player UUID&gt/&lt;server UUID&gt;
     */
    private static void createClientSideCacheDirectories()
    {
        cachedPlayerUUID = mc.player.getUniqueID();
        Path clientSidePlayerServerCachePath = Paths.get(FileHelper.CLIENT_SERVER_CACHE_FOLDER, cachedPlayerUUID.toString(), getCachedServerID().toString());
        pathAreas = getSubDirectory(clientSidePlayerServerCachePath.toString(), DIR_AREAS);
        pathMusic = getSubDirectory(clientSidePlayerServerCachePath.toString(), DIR_MUSIC);
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

    // Call backs from the Client State Monitor on serer connect. Pulls the playlists.

    @Override
    public void onFailure(@Nonnull ITextComponent textComponent)
    {
        ModLogger.warn("ClientFileManager onFailure: %s", textComponent.getFormattedText());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onResponse(@Nullable Object payload)
    {
        if (payload != null)
        {
            for (Area area : (List<Area>) payload)
            {
                NBTTagCompound compound = new NBTTagCompound();
                area.writeToNBT(compound);
                addArea(area.getGUID(), compound, false);
            }
        }
    }

    // Playlist

    public static List<Area> getAreas()
    {
        List<Area> areas = new ArrayList<>();
        synchronized (mapAreas)
        {
            for (Map.Entry<GUID, Area> entry : mapAreas.entrySet())
            {
                areas.add(entry.getValue());
            }
        }
        return areas;
    }

    public static void addArea(GUID guid, NBTTagCompound data, boolean error)
    {
        if (error)
        {
            addBadArea(guid);
            return;
        }
        Path path;
        Area area = new Area();
        area.readFromNBT(data);
        mapAreas.put(guid, area);
        try
        {
            path = FileHelper.getCacheFile(pathAreas.toString(), guid.toString() + FileHelper.EXTENSION_DAT, Side.CLIENT);
        }
        catch (IOException e)
        {
            ModLogger.error(e);
            ModLogger.error("Unable to write Area file: %s to cache folder: %s", guid.toString() + FileHelper.EXTENSION_DAT, pathAreas.toString());
            return;
        }
        waitArea = false;
        FileHelper.sendCompoundToFile(path, data);
    }

    // Songs

    public static void addSong(GUID uuid, NBTTagCompound data, boolean error)
    {
        if (error)
        {
            addBadSong(uuid);
            return;
        }
        Path path;
        if (!mapSongProxies.containsKey(uuid))
        {
            SongProxy proxy = new SongProxy();
            proxy.readFromNBT(data);
            mapSongProxies.put(uuid, proxy);
        }
        Boolean fileExists = FileHelper.fileExists(pathMusic.toString(), uuid.toString() + FileHelper.EXTENSION_DAT, Side.CLIENT);
        if (!fileExists)
        {
            try
            {
                path = FileHelper.getCacheFile(pathMusic.toString(), uuid.toString() + FileHelper.EXTENSION_DAT, Side.CLIENT);
            } catch (IOException e)
            {
                ModLogger.error(e);
                ModLogger.error("Unable to write Music file: %s to cache folder: %s", uuid.toString() + FileHelper.EXTENSION_DAT, pathMusic.toString());
                return;
            }
            waitSong = false;
            FileHelper.sendCompoundToFile(path, data);
        }
    }

    static boolean hasSongProxy(GUID guid)
    {
        return mapSongProxies.containsKey(guid);
    }

    @Nullable
    static SongProxy getSongProxy(GUID guid)
    {
        if (hasSongProxy(guid))
            return  mapSongProxies.get(guid);
        else
            return null;
    }

    // Bad Playlist and Bad Song - not found data.

    private static void addBadArea(GUID guid)
    {
        badAreas.add(guid);
    }

    private static void addBadSong(GUID guid)
    {
        badSongs.add(guid);
    }

    private static boolean isNotBadArea(GUID guid)
    {
        return !badAreas.contains(guid);
    }

    static boolean isNotBadSong(GUID guid)
    {
        return !badSongs.contains(guid);
    }

    // Cache management

    @SuppressWarnings("unused")
    private static <T extends BaseData> void loadCache(Path loc, Map<GUID, T> map, Class<T> type)
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
                    GUID guid = data.getGUID();
                    map.put(guid, data);
                } catch (InstantiationException | IllegalAccessException e)
                {
                    ModLogger.error(e);
                    ModLogger.error("What did you do? What's this thing?: %s", type.getSimpleName());
                    throw new MXTuneRuntimeException(e);
                }
        }
    }

    private static void clearCache(Path loc)
    {
        List<Path> files = new ArrayList<>();
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
            try
            {
                if (!file.toFile().isDirectory())
                {
                    Files.delete(file);

                }
            } catch (UnsupportedOperationException | IOException | SecurityException e)
            {
                ModLogger.error(e);
            }
    }

    // This is called every tick so after at least one to two ticks a song should be available
    // If no local cache files exist then the area/playlist will get DL and cached. On subsequent ticks
    // the song itself will be chosen and made available
    static boolean songAvailable(GUID uuidArea)
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
        return resolveArea(uuidArea) && isNotBadArea(uuidArea) && !waitArea && !waitSong;
    }

    private static boolean resolveArea(GUID guid)
    {
        if (mapAreas.containsKey(guid))
        {
            waitArea = false;
            return true;
        }
        else
        {
            if (!Reference.EMPTY_GUID.equals(guid) && isNotBadArea(guid))
            {
                waitArea = true;
                PacketDispatcher.sendToServer(new GetServerDataMessage(guid, GetType.AREA));
            }
            return false;
        }
    }

    @Nullable
    public static Area getArea(GUID guid)
    {
        if (resolveArea(guid))
        {
            return mapAreas.get(guid);
        }
        return null;
    }

    @Nullable
    static Song getSongFromCache(GUID guid)
    {
        if (mapSongProxies.containsKey(guid) && isNotBadSong(guid))
        {
            try
            {
                Path path = FileHelper.getCacheFile(pathMusic.toString(), guid.toString() + FileHelper.EXTENSION_DAT, Side.CLIENT);
                NBTTagCompound compound = FileHelper.getCompoundFromFile(path);
                Song song = new Song();
                song.readFromNBT(compound);
                return song;
            } catch (IOException e)
            {
                ModLogger.error(e);
                Path path = Paths.get(pathMusic.toString(), guid.toString() + FileHelper.EXTENSION_DAT);
                ModLogger.error("Unable to read file: " + path);
            }
        }
        return null;
    }
}
