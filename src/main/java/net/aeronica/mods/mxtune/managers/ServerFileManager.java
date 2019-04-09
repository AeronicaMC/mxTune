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

import net.aeronica.libs.mml.core.TestData;
import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.managers.records.Area;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.aeronica.mods.mxtune.util.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.aeronica.mods.mxtune.caches.FileHelper.SERVER_FOLDER;

public class ServerFileManager
{
    private static final String SERVER_ID_FILE = "server_id" + FileHelper.EXTENSION_DAT;
    private static final String SERVER_ID_FILE_ERROR = "Delete the <world save>/mxtune/server_id" + FileHelper.EXTENSION_DAT + " file, then try loading the world again.";
    private static UUID serverID;
    private static Map<GUID, SongProxy> songProxyMap = new HashMap<>();
    private static Map<GUID, Area> areas = new HashMap<>();

    private ServerFileManager() { /* NOP */ }

    public static void start()
    {
        getOrGenerateServerID();
        // stuffServer goes here when needed - test data
        new Thread(() ->
                   {
                       initSongs();
                       initAreas();
                       dumpAll();
                   }
        ).start();
    }

    public static void shutdown()
    {
        songProxyMap.clear();
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
                SongProxy songProxy = new SongProxy(songCompound);
                songProxyMap.put(songProxy.getGUID(), songProxy);
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
                GUID areaGUID = area.getGUID();
                    areas.put(areaGUID, area);
            }
        }
    }

    public static List<Area> getAreas()
    {
        List<Area> areaList = new ArrayList<>();
        areas.forEach((key, value) -> areaList.add(value));
        return areaList;
    }

    /**
     * Get the Area record that corresponds to the given GUID.
     * @param guidArea GUID of the desired Area record
     * @return the Area record, or if it does not exist an Empty Area
     */
    public static Area getArea(GUID guidArea)
    {
        return areas.containsKey(guidArea) ? areas.get(guidArea) : new Area();
    }

    public static ResultMessage setArea(GUID dataTypeUuid, Area area)
    {
        ResultMessage errorResult = ResultMessage.NO_ERROR;
        if (area != null)
        {
            GUID areaGUID = area.getGUID();
            if (dataTypeUuid.equals(areaGUID))
            {
                String areaFileName = area.getFileName();
                try
                {
                    NBTTagCompound compound = new NBTTagCompound();
                    area.writeToNBT(compound);
                    Path path = FileHelper.getCacheFile(FileHelper.SERVER_AREAS_FOLDER, areaFileName, Side.SERVER);
                    FileHelper.sendCompoundToFile(path, compound);
                }
                catch(IOException e)
                {
                    ModLogger.error(e);
                    ModLogger.warn("Unable to create folder: %s and/or file: %s", FileHelper.SERVER_AREAS_FOLDER, areaFileName);
                    errorResult = new ResultMessage(true, new TextComponentTranslation("mxtune.error.unable_to_create_file_folder",FileHelper.SERVER_AREAS_FOLDER, areaFileName));
                }
                if (!errorResult.hasError() || !areas.containsKey(areaGUID))
                    areas.put(areaGUID, area);
            }
            else
            {
                throw new MXTuneRuntimeException("GUID Mismatch in transport: Corrupted Area data");
            }
        }
        else
        {
            throw new MXTuneRuntimeException("dataCompound is null in ServerFileManager.setArea");
        }
        return errorResult;
    }

    public static ResultMessage setSong(GUID dataTypeUuid, Song song)
    {
        ResultMessage errorResult = ResultMessage.NO_ERROR;
        if (song != null)
        {
            SongProxy songProxy = new SongProxy(song);
            GUID songGUID = song.getGUID();
            if (dataTypeUuid.equals(songGUID))
            {
                String songFileName = song.getFileName();
                try
                {
                    NBTTagCompound dataCompound = new NBTTagCompound();
                    song.writeToNBT(dataCompound);
                    Path path = FileHelper.getCacheFile(FileHelper.SERVER_MUSIC_FOLDER, songFileName, Side.SERVER);
                    FileHelper.sendCompoundToFile(path, dataCompound);
                }
                catch(IOException e)
                {
                    ModLogger.warn(e);
                    ModLogger.warn("Unable to create folder: %s and/or file: %s", FileHelper.SERVER_MUSIC_FOLDER, songFileName);
                    errorResult = new ResultMessage(true, new TextComponentTranslation("mxtune.error.unable_to_create_file_folder",FileHelper.SERVER_MUSIC_FOLDER, songFileName));
                }
                if (!errorResult.hasError() || !songProxyMap.containsKey(songGUID))
                    songProxyMap.put(songGUID, songProxy);
            }
            else
            {
                throw new MXTuneRuntimeException("GUID Mismatch in transport: Corrupted Song data");
            }
        }
        else
        {
            throw new MXTuneRuntimeException("dataCompound is null in ServerFileManager.setSong");
        }
        return errorResult;
    }

    @SuppressWarnings("unused")
    private static void stuffServer()
    {
        // Create Songs
        List<SongProxy> songProxies = new ArrayList<>();
        for (TestData testData : TestData.values())
        {
            Song song = new Song(testData.getTitle(), testData.getMML());

            NBTTagCompound songCompound = new NBTTagCompound();
            SongProxy songProxy = new SongProxy(songCompound);
            songProxies.add(songProxy);

            song.writeToNBT(songCompound);
            Path path;
            try
            {
                path = FileHelper.getCacheFile(FileHelper.SERVER_MUSIC_FOLDER, song.getFileName(), Side.SERVER);
                FileHelper.sendCompoundToFile(path, songCompound);
            }
            catch (IOException e)
            {
                ModLogger.warn(e);
                ModLogger.warn("Unable to create folder: %s and/or file: %s", FileHelper.SERVER_MUSIC_FOLDER, song.getFileName());
            }
        }

        // Create an area
        String areaName = "Test Area";
        String areaFileName = "";
        try
        {
            NBTTagCompound compound = new NBTTagCompound();
            Area area = new  Area(areaName, songProxies, songProxies);
            areaFileName = area.getFileName();
            Path path = FileHelper.getCacheFile(FileHelper.SERVER_AREAS_FOLDER, areaFileName, Side.SERVER);
            area.writeToNBT(compound);
            FileHelper.sendCompoundToFile(path, compound);
        }
        catch(IOException e)
        {
            ModLogger.warn(e);
            ModLogger.warn("Unable to create folder: %s and/or file: %s", FileHelper.SERVER_AREAS_FOLDER, areaFileName);
        }
    }

    private static void dumpAll()
    {
        areas.forEach((key, value) -> ModLogger.debug("Area guid:     %s, Name:    %s", key.toString(), value.getName()));
        songProxyMap.forEach((key, value) -> ModLogger.debug("Song guid:     %s, title:    %s", key.toString(), value.getTitle()));
    }
}
