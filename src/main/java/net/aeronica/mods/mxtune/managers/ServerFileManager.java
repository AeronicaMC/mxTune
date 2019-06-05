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
import net.aeronica.mods.mxtune.managers.records.PlayList;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.aeronica.mods.mxtune.mxt.MXTuneFile;
import net.aeronica.mods.mxtune.mxt.MXTuneFileHelper;
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
    private static final String FORMAT_UNABLE_TO_CREATE = "Unable to create folder: %s and/or file: %s";
    private static final String SERVER_ID_FILE = "server_id" + FileHelper.EXTENSION_DAT;
    private static final String SERVER_ID_FILE_ERROR = "Delete the <world save>/mxtune/server_id" + FileHelper.EXTENSION_DAT + " file, then try loading the world again.";
    private static UUID serverID;
    private static final Map<GUID, SongProxy> songProxyMap = new HashMap<>();
    private static final Map<GUID, PlayList> playLists = new HashMap<>();

    private ServerFileManager() { /* NOP */ }

    public static void start()
    {
        getOrGenerateServerID();
        Update.renameAreasToPlaylists();
        Update.convertSongToMxt();
        // stuffServer goes here when needed - test data
        new Thread(() ->
                   {
                       initPlayLists();
                       initSongs();
                       dumpAll();
                   }
        ).start();
    }

    public static void shutdown()
    {
        songProxyMap.clear();
        playLists.clear();
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

    private static void initPlayLists()
    {
        // The NULL and NO MUSIC Playlists.
        PlayList nullPlaylist = PlayList.undefinedPlaylist();
        playLists.put(nullPlaylist.getGUID(), nullPlaylist);
        PlayList noPlaylists = PlayList.emptyPlaylist();
        playLists.put(noPlaylists.getGUID(), noPlaylists);

        List<Path> playListFiles = new ArrayList<>();

        Path path = FileHelper.getDirectory(FileHelper.SERVER_PLAY_LISTS_FOLDER, Side.SERVER);
        PathMatcher filter = FileHelper.getDatMatcher(path);
        try (Stream<Path> paths = Files.list(path))
        {
            playListFiles = paths
                    .filter(filter::matches)
                    .collect(Collectors.toList());
        }
        catch (NullPointerException | IOException e)
        {
            ModLogger.error(e);
        }

        for (Path pathPlayList : playListFiles)
        {
            NBTTagCompound compound = FileHelper.getCompoundFromFile(pathPlayList);
            if (compound != null)
            {
                PlayList playList = PlayList.build(compound);
                GUID playListGUID = playList.getGUID();
                    playLists.put(playListGUID, playList);
            }
        }
    }

    public static synchronized List<PlayList> getPlayLists()
    {
        List<PlayList> playListList = new ArrayList<>();
        playLists.forEach((key, value) -> playListList.add(value));
        return playListList;
    }

    /**
     * Get the PlayList record that corresponds to the given GUID.
     * @param guidPlayList GUID of the desired PlayList record
     * @return the PlayList record, or if it does not exist an Empty PlayList
     */
    public static synchronized PlayList getPlayList(GUID guidPlayList)
    {
        return playLists.containsKey(guidPlayList) ? playLists.get(guidPlayList) : PlayList.undefinedPlaylist();
    }

    public static ResultMessage setPlayList(GUID dataTypeUuid, PlayList playList)
    {
        ResultMessage errorResult = ResultMessage.NO_ERROR;
        if (playList != null)
        {
            GUID playListGUID = playList.getGUID();
            if (dataTypeUuid.equals(playListGUID))
            {
                String playListFileName = playList.getFileName();
                try
                {
                    NBTTagCompound compound = new NBTTagCompound();
                    playList.writeToNBT(compound);
                    Path path = FileHelper.getCacheFile(FileHelper.SERVER_PLAY_LISTS_FOLDER, playListFileName, Side.SERVER);
                    FileHelper.sendCompoundToFile(path, compound);
                }
                catch(IOException e)
                {
                    ModLogger.error(e);
                    ModLogger.warn(FORMAT_UNABLE_TO_CREATE, FileHelper.SERVER_PLAY_LISTS_FOLDER, playListFileName);
                    errorResult = new ResultMessage(true, new TextComponentTranslation("mxtune.error.unable_to_create_file_folder", FileHelper.SERVER_PLAY_LISTS_FOLDER, playListFileName));
                }
                if (!errorResult.hasError())
                    synchronized (playLists)
                    {
                        playLists.put(playListGUID, playList);
                    }
            }
            else
            {
                throw new MXTuneRuntimeException("GUID Mismatch in transport: Corrupted PlayList data");
            }
        }
        else
        {
            throw new MXTuneRuntimeException("dataCompound is null in ServerFileManager.setPlayList");
        }
        return errorResult;
    }

    public static ResultMessage setMXTFile(GUID dataTypeUuid, MXTuneFile mxTuneFile)
    {
        ResultMessage errorResult = ResultMessage.NO_ERROR;
        if (mxTuneFile != null)
        {
            SongProxy songProxy = MXTuneFileHelper.getSongProxy(mxTuneFile);
            GUID songGUID = mxTuneFile.getGUID();
            if (dataTypeUuid.equals(songGUID))
            {
                String songFileName = mxTuneFile.getFileName();
                try
                {
                    NBTTagCompound dataCompound = new NBTTagCompound();
                    mxTuneFile.writeToNBT(dataCompound);
                    Path path = FileHelper.getCacheFile(FileHelper.SERVER_MUSIC_FOLDER, songFileName, Side.SERVER);
                    FileHelper.sendCompoundToFile(path, dataCompound);
                }
                catch(IOException e)
                {
                    ModLogger.warn(e);
                    ModLogger.warn(FORMAT_UNABLE_TO_CREATE, FileHelper.SERVER_MUSIC_FOLDER, songFileName);
                    errorResult = new ResultMessage(true, new TextComponentTranslation("mxtune.error.unable_to_create_file_folder",FileHelper.SERVER_MUSIC_FOLDER, songFileName));
                }
                if (!errorResult.hasError())
                    synchronized (songProxyMap)
                    {
                        songProxyMap.put(songGUID, songProxy);
                    }
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
                ModLogger.warn(FORMAT_UNABLE_TO_CREATE, FileHelper.SERVER_MUSIC_FOLDER, song.getFileName());
            }
        }

        // Create an playlist
        String playListName = "Test PlayList";
        String playListFileName = "";
        try
        {
            NBTTagCompound compound = new NBTTagCompound();
            PlayList playList = new PlayList(playListName, songProxies, songProxies);
            playListFileName = playList.getFileName();
            Path path = FileHelper.getCacheFile(FileHelper.SERVER_PLAY_LISTS_FOLDER, playListFileName, Side.SERVER);
            playList.writeToNBT(compound);
            FileHelper.sendCompoundToFile(path, compound);
        }
        catch(IOException e)
        {
            ModLogger.warn(e);
            ModLogger.warn(FORMAT_UNABLE_TO_CREATE, FileHelper.SERVER_PLAY_LISTS_FOLDER, playListFileName);
        }
    }

    private static void dumpAll()
    {
        playLists.forEach((key, value) -> ModLogger.debug("PlayList guid:     %s, Name:    %s", key.toString(), value.getName()));
        songProxyMap.forEach((key, value) -> ModLogger.debug("Song guid:     %s, title:    %s", key.toString(), value.getTitle()));
    }

    public static void main(String[] args)
    {
        //convertSongToMxt();
    }
}
