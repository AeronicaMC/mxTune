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

import java.io.*;

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

    public static final FileFilter MML_FILE_FILTER = accept ->
    {
        boolean zipped = accept.isFile() && accept.getName().endsWith(".zip");
        boolean ms2mml = accept.isFile() && accept.getName().endsWith(".ms2mml");
        boolean mml = accept.isFile() && accept.getName().endsWith(".mml");
        return zipped || ms2mml || mml;
    };

    private FileHelper() { /* NOP */ }

    private static void fixDirectory(File dir)
    {
        if (dir.exists())
        {
            if (!dir.isDirectory() && (!dir.delete() || !dir.mkdirs()))
            {
                ModLogger.warn("Unable to recreate MML folder, it exists but is not a directory: {}", (Object)dir);
            }
        }
        else if (!dir.mkdirs())
        {
            ModLogger.warn("Unable to create MML folder: {}", (Object)dir);
        }
    }

    public static void openFolder(String folder) throws IOException
    {
        OpenGlHelper.openFile(getDirectory(folder));
    }

    public static File getDirectory(String folder) throws IOException
    {
        File loc = new File(".", folder);
        fixDirectory(loc);
        return loc;
    }

    public static File getCacheFile(String folder, String filename) throws IOException
    {
        File loc = getDirectory(folder);
        File cacheFile = new File(loc, filename);

        if(!cacheFile.exists()) {
            if (!cacheFile.getParentFile().mkdirs())
                ModLogger.debug("mkdirs: folder already exists %s", loc.getPath());
            if (!cacheFile.createNewFile())
                ModLogger.debug("createNewFile: file already exists %s", cacheFile);
        }

        return cacheFile;
    }

    public static NBTTagCompound getCompoundFromFile(File file)
    {
        if (file == null)
            throw new MXTuneRuntimeException("Missing cache file!");

        try
        {
            return CompressedStreamTools.readCompressed(new FileInputStream(file));
        }
        catch (IOException e0)
        {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();

            try
            {
                CompressedStreamTools.writeCompressed(nbtTagCompound, new FileOutputStream(file));
                return getCompoundFromFile(file);
            } catch (IOException e1)
            {
                ModLogger.error(e1);
                return null;
            }
        }
    }

    public static void sendCompoundToFile(File file, NBTTagCompound tagCompound)
    {
        try
        {
            CompressedStreamTools.writeCompressed(tagCompound, new FileOutputStream(file));
        } catch(IOException e) {
            ModLogger.error(e);
        }
    }
}
