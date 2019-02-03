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
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

    public static File getCacheFile(String folder, String filename) throws IOException {
        File loc = new File(".");
        File cacheFile = new File(loc, folder + "/" + filename);

        if(!cacheFile.exists()) {
            cacheFile.getParentFile().mkdirs();
            cacheFile.createNewFile();
        }

        return cacheFile;
    }

    public static NBTTagCompound getCompoundFromFile(File file)
    {
        if (file == null)
            throw new MXTuneRuntimeException("Missing cache file!");

        try
        {
            NBTTagCompound tagCompound = CompressedStreamTools.readCompressed(new FileInputStream(file));
            return tagCompound;
        } catch (IOException e0)
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

    public static void sendCompoundToFile(File file, NBTTagCompound tagCompound) {
        try {
            CompressedStreamTools.writeCompressed(tagCompound, new FileOutputStream(file));
        } catch(IOException e) {
            ModLogger.error(e);
        }
    }
}
