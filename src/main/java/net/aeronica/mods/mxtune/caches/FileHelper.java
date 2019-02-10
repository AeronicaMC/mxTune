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

/*
 * Borrowed from vazkii.minetunes.config CacheHelper class
 * Why Vazkii did you use the [Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License] on minetunes?
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 */

package net.aeronica.mods.mxtune.caches;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.MXTuneRuntimeException;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public class FileHelper
{
    private static final String MOD_FOLDER = Reference.MOD_ID;
    public static final String CLIENT_CACHE_FOLDER = MOD_FOLDER + "/client_cache";
    public static final String CLIENT_MML_FOLDER = MOD_FOLDER + "/client_mml";
    public static final String CLIENT_LIB_FOLDER = MOD_FOLDER + "/client_lib";
    public static final String CLIENT_PLAYLISTS_FOLDER = MOD_FOLDER + "/client_playlists";
    public static final String CLIENT_SERVER_CACHE_FOLDER = MOD_FOLDER + "/client_server_cache";
    public static final String SERVER_LIB_FOLDER = MOD_FOLDER + "/server_lib";
    public static final String SERVER_PLAYLISTS_FOLDER = MOD_FOLDER + "/server_playlists";

    private FileHelper() { /* NOP */ }

    public static PathMatcher getMMLMatcher(Path path)
    {
        return path.getFileSystem().getPathMatcher("glob:**.{mml,ms2mml,zip}");
    }

    private static void fixDirectory(Path dir)
    {
        if (Files.exists(dir) && !Files.isDirectory(dir))
            try
            {
                Files.delete(dir);
                Files.createDirectories(dir);
            } catch (IOException e)
            {
                ModLogger.error(e);
                ModLogger.warn("Unable to recreate MML folder, it exists but is not a directory: {}", (Object) dir);
            }
        else
            try
            {
                Files.createDirectories(dir);

            } catch (IOException e)
            {
                ModLogger.error(e);
                ModLogger.warn("Unable to create MML folder: {}", (Object) dir);
            }
    }

    public static void openFolder(String folder)
    {
        OpenGlHelper.openFile(getDirectory(folder).toFile());
    }

    public static Path getDirectory(String folder)
    {
        Path loc = Paths.get(".", folder);
        fixDirectory(loc);
        return loc;
    }

    public static Path getCacheFile(String folder, String filename) throws IOException
    {
        Path dir = getDirectory(folder);
        Path cacheFile = dir.resolve(filename);

        if(!Files.exists(cacheFile))
        {
            Files.createDirectories(dir);
            Files.createFile(cacheFile);
        }
        return cacheFile;
    }

    public static NBTTagCompound getCompoundFromFile(Path path)
    {
        if (path == null)
            throw new MXTuneRuntimeException("Missing cache file!");

        try
        {
            return CompressedStreamTools.readCompressed(new FileInputStream(path.toFile()));
        }
        catch (IOException e0)
        {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            try
            {
                CompressedStreamTools.writeCompressed(nbtTagCompound, new FileOutputStream(path.toFile()));
                return getCompoundFromFile(path);
            } catch (IOException e1)
            {
                ModLogger.error(e1);
                return null;
            }
        }
    }

    public static void sendCompoundToFile(Path path, NBTTagCompound tagCompound)
    {
        try
        {
            CompressedStreamTools.writeCompressed(tagCompound, new FileOutputStream(path.toFile()));
        } catch(IOException e) {
            ModLogger.error(e);
        }
    }
}
