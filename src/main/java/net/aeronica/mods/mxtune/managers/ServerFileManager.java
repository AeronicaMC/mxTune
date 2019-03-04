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
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.util.ModLogger;
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

public class ServerFileManager
{
    private static BiMap<UUID, String> songUuidVsTitles = HashBiMap.create();
    private static ListMultimap<UUID, UUID> playListVsSongs = ArrayListMultimap.create();

    private ServerFileManager() { /* NOP */ }

    public static void startUp()
    {
        initSongs();
        initPlayLists();
    }

    public static void shutDown()
    {
        songUuidVsTitles.clear();
        playListVsSongs.clear();
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

    private static void initPlayLists()
    {

    }

    private static void stuffServer()
    {
        for (TestData testData : TestData.values())
        {
            Song song = new Song(testData.getTitle(), testData.getMML());
            NBTTagCompound songCommpound = new NBTTagCompound();
            song.writeToNBT(songCommpound);
            Path path;
            try
            {
                path = FileHelper.getCacheFile(FileHelper.SERVER_MUSIC_FOLDER, song.getFileName(), Side.SERVER);
                FileHelper.sendCompoundToFile(path, songCommpound);
            }
            catch (IOException e)
            {
                ModLogger.error(e);
                ModLogger.warn("Unable to create folder: %s and/or file: %s", FileHelper.SERVER_MUSIC_FOLDER, song.getFileName());
            }
        }
    }

    private static void dumpSongs()
    {
        songUuidVsTitles.forEach((key, value) -> {
            ModLogger.debug("uuid: %s, title: %s", key.toString(), value);
        });
    }
}
