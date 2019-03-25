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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ListMultimap;
import net.aeronica.libs.mml.core.TestData;
import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.managers.records.Area;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.util.MXTuneRuntimeException;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.NBTHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.aeronica.mods.mxtune.caches.FileHelper.SERVER_FOLDER;

public class ServerFileManager
{
    private static final String SERVER_ID_FILE = "server_id.dat";
    private static final String SERVER_ID_FILE_ERROR = "Delete the <world save>/mxtune/server_id.dat file, then try loading the world again.";
    private static UUID serverID;
    private static BiMap<UUID, String> songUuidVsTitles = HashBiMap.create();
    private static ListMultimap<UUID, UUID> playListVsSongs = ArrayListMultimap.create();
    private static ListMultimap<UUID, UUID> areaVsPlayList = ArrayListMultimap.create();
    private static BiMap<UUID, Area> areas = HashBiMap.create();

    private ServerFileManager() { /* NOP */ }

    public static void startUp()
    {
        getOrGenerateServerID();
        stuffServer(); // stuffServer goes here when needed - test data
        initSongs();
        initAreas();
        dumpAll();
    }

    public static void shutDown()
    {
        songUuidVsTitles.clear();
        playListVsSongs.clear();
        areas.clear();
    }

    private static void getOrGenerateServerID()
    {
        boolean fileExists = FileHelper.fileExists(SERVER_FOLDER, SERVER_ID_FILE, Side.SERVER);
        NBTTagCompound compound;
        Path serverDatFile;
        if (fileExists)
        {
            try
            {
                serverDatFile = FileHelper.getCacheFile(SERVER_FOLDER, SERVER_ID_FILE, Side.SERVER);
                compound = FileHelper.getCompoundFromFile(serverDatFile);
                if (compound != null)
                    serverID = NBTHelper.getUuidFromCompound(compound);
                else throw new NullPointerException("NBTTagCompound compound is null!");
            } catch (NullPointerException | IOException e)
            {
                ModLogger.error("The %s/%s file could not be read.", SERVER_FOLDER, SERVER_ID_FILE );
                throw new MXTuneRuntimeException(SERVER_ID_FILE_ERROR, e);
            }
        }
        else
        {
            try
            {
                serverDatFile = FileHelper.getCacheFile(SERVER_FOLDER, SERVER_ID_FILE, Side.SERVER);
                serverID = UUID.randomUUID();
                compound = new NBTTagCompound();
                NBTHelper.setUuidToCompound(compound, serverID);
                FileHelper.sendCompoundToFile(serverDatFile, compound);
            }
            catch (IOException e)
            {
                ModLogger.error("The %s/%s file could not be written.", SERVER_FOLDER, SERVER_ID_FILE );
                throw new MXTuneRuntimeException(SERVER_ID_FILE_ERROR, e);
            }
        }
    }

    public static UUID getServerID()
    {
        return serverID;
    }

    private static void initSongs()
    {
        List<Path> songFiles = new ArrayList<>();

        Path path = FileHelper.getDirectory(FileHelper.SERVER_MUSIC_FOLDER, Side.SERVER);
        PathMatcher filter = FileHelper.getDatMatcher(path);
        try (Stream<Path> paths = Files.list(path))
        {
            songFiles = paths
                    .filter(filter::matches)
                    .collect(Collectors.toList());
        }
        catch (NullPointerException | IOException e)
        {
            ModLogger.error(e);
        }

        for (Path songFile : songFiles)
        {
            NBTTagCompound songCompound = FileHelper.getCompoundFromFile(songFile);
            if (songCompound != null)
            {
                Song song = new Song(songCompound);
                songUuidVsTitles.put(song.getUUID(), song.getTitle());
            }
            else
                ModLogger.warn("NULL NBTTagCompound for song file: %s", songFile.toString());
        }
    }

    private static void initAreas()
    {
        List<Path> areaFiles = new ArrayList<>();

        Path path = FileHelper.getDirectory(FileHelper.SERVER_AREAS_FOLDER, Side.SERVER);
        PathMatcher filter = FileHelper.getDatMatcher(path);
        try (Stream<Path> paths = Files.list(path))
        {
            areaFiles = paths
                    .filter(filter::matches)
                    .collect(Collectors.toList());
        }
        catch (NullPointerException | IOException e)
        {
            ModLogger.error(e);
        }

        for (Path pathArea : areaFiles)
        {
            NBTTagCompound compound = FileHelper.getCompoundFromFile(pathArea);
            if (compound != null)
            {
                Area area = Area.build(compound);
                UUID uuidArea = area.getUUID();
                    areas.put(uuidArea, area);
            }
        }
    }

    public static void setArea(UUID dataTypeUuid, NBTTagCompound dataCompound)
    {
        if (dataCompound != null)
        {
            Area area = Area.build(dataCompound);
            UUID uuidArea = area.getUUID();
            if (dataTypeUuid.equals(uuidArea))
            {
                areas.put(uuidArea, area);
            }
            else
            {
                throw new MXTuneRuntimeException("UUID Mismatch in transport: Corrupted Area data");
            }
        }
        else
        {
            throw new MXTuneRuntimeException("Area dataCompound is null in ServerFileManager.setArea");
        }
    }

    private static void stuffServer()
    {
        // Create Songs
        List<UUID> songUUIDs = new ArrayList<>();
        for (TestData testData : TestData.values())
        {
            Song song = new Song(testData.getTitle(), testData.getMML());
            songUUIDs.add(song.getUUID());
            NBTTagCompound songCompound = new NBTTagCompound();
            song.writeToNBT(songCompound);
            Path path;
            try
            {
                path = FileHelper.getCacheFile(FileHelper.SERVER_MUSIC_FOLDER, song.getFileName(), Side.SERVER);
                FileHelper.sendCompoundToFile(path, songCompound);
            }
            catch (IOException e)
            {
                ModLogger.error(e);
                ModLogger.warn("Unable to create folder: %s and/or file: %s", FileHelper.SERVER_MUSIC_FOLDER, song.getFileName());
            }
        }

        // Create an area
        String areaName = "Test Area";
        String areaFileName = "";
        try
        {
            NBTTagCompound compound = new NBTTagCompound();
            Area area = new  Area(areaName, songUUIDs, songUUIDs);
            areaFileName = area.getFileName();
            Path path = FileHelper.getCacheFile(FileHelper.SERVER_AREAS_FOLDER, areaFileName, Side.SERVER);
            area.writeToNBT(compound);
            FileHelper.sendCompoundToFile(path, compound);
        }
        catch(IOException e)
        {
            ModLogger.error(e);
            ModLogger.warn("Unable to create folder: %s and/or file: %s", FileHelper.SERVER_AREAS_FOLDER, areaFileName);
        }
    }

    private static void dumpAll()
    {
        songUuidVsTitles.forEach((key, value) -> ModLogger.debug("Song uuid:     %s, title:    %s", key.toString(), value));
        playListVsSongs.forEach((key, value) -> ModLogger.debug( "Playlist uuid: %s, song:     %s", key.toString(), value.toString()));
        areaVsPlayList.forEach((key, value) -> ModLogger.debug(  "Area uuid:     %s, playlist: %s", key.toString(), value.toString()));
    }
}
