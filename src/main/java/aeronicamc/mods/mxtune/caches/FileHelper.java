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

package aeronicamc.mods.mxtune.caches;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.util.MXTuneRuntimeException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public class FileHelper
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MOD_FOLDER = Reference.MOD_ID;
    private static final String CLIENT_FOLDER = MOD_FOLDER;
    public static final String CLIENT_MML_FOLDER = CLIENT_FOLDER + "/import_folder";
    public static final String CLIENT_LIB_FOLDER = CLIENT_FOLDER + "/library";
    public static final String CLIENT_SERVER_CACHE_FOLDER = CLIENT_FOLDER + "/server_cache";
    public static final String SERVER_FOLDER = MOD_FOLDER;
    public static final String SERVER_PLAY_LISTS_FOLDER = SERVER_FOLDER + "/playlists";
    public static final String SERVER_MUSIC_FOLDER = SERVER_FOLDER + "/music";
    public static final String SERVER_MUSIC_FOLDER_DUMP_FOLDER = SERVER_FOLDER + "/dump";

    public static final String EXTENSION_DAT = ".dat";
    public static final String EXTENSION_MXT = ".mxt";

    private static Path serverWorldFolder;

    /**
     * Stores the path of the server side 'world' folder. This changes based on the chosen save folder for SP/LAN
     * integrated server or the 'level-name' property in the server.properties for dedicated servers.
     * <p></p>
     * This needs to be called from the {@link FMLServerStartingEvent}
     */
    public static void initialize(MinecraftServer server)
    {
        // The top level "world" save folder a.k.a. the "Over World"
        //.getActiveAnvilConverter().getFile(server.getFolderName(), "serverconfig").toPath();
        // File chunkDir = server.getActiveAnvilConverter().getFile(server.getFolderName(), "");
        File chunkDir = server.getWorldPath(new FolderName("")).toFile();
        serverWorldFolder = Paths.get(chunkDir.getPath());
        LOGGER.debug("FileHelper: serverWorldFolder {}", serverWorldFolder.toString());
    }

    private FileHelper() { /* NOP */ }

    private static Path getServerWorldFolder()
    {
        return serverWorldFolder;
    }

    public static PathMatcher getMmlMatcher(Path path)
    {
        return path.getFileSystem().getPathMatcher("glob:**.{mml,ms2mml,zip}");
    }

    public static PathMatcher getGZMatcher(Path path)
    {
        return path.getFileSystem().getPathMatcher("glob:**.{gz}");
    }

    /**
     * Match the .mxt file extension
     * @param path to match files
     * @return files with the mxTune file type extension
     */
    public static PathMatcher getMxtMatcher(Path path)
    {
        return path.getFileSystem().getPathMatcher("glob:**.{mxt}");
    }

    public static String removeExtension(String s)
    {
        return s.replaceAll("(\\.\\w+$)", "");
    }

    public static String normalizeFilename(String s)
    {
        return s.replaceAll("([\\x00-\\x1F!\"\\$\'\\.\\(\\)\\*,\\/:;<>\\?\\[\\\\\\]\\{\\|\\}\\x7F]+)", "");
    }

    private static void fixDirectory(Path dir)
    {
        if (dir.toFile().exists() && !dir.toFile().isDirectory())
            try
            {
                Files.delete(dir);
                Files.createDirectories(dir);
            } catch (IOException e)
            {
                LOGGER.error(e);
                LOGGER.warn("Unable to recreate folder, it exists but is not a directory: {}", (Object) dir);
            }
        else
            try
            {
                Files.createDirectories(dir);

            } catch (IOException e)
            {
                LOGGER.error(e);
                LOGGER.warn("Unable to create folder: {}", (Object) dir);
            }
    }

    /**
     * Open an OS folder on the client side.
     * @param folder of interest
     */
    @OnlyIn(Dist.CLIENT)
    public static void openFolder(String folder)
    {
        Util.getPlatform().openFile(getDirectory(folder, LogicalSide.CLIENT).toFile());
    }

    /**
     * getDirectory is side sensitive.
     * Client side references the game run folder (e.g. .minecraft): "."
     * Server side references the world save folder (e.g. world):  "<world folder name>"
     * This method will create the specified directory if it does not exist!
     * @param folder folder name
     * @param side Side.SERVER or Side CLIENT
     * @return the Path of the folder on the specified side
     */
    public static Path getDirectory(String folder, LogicalSide side)
    {
        return getDirectory(folder, side, true);
    }

    /**
     * getDirectory is side sensitive.
     * Client side references the game run folder (e.g. .minecraft): "."
     * Server side references the world save folder (e.g. world):  "<world folder name>"
     * This method will create the specified directory if it does not exist!
     * @param folder folder name
     * @param side Side.SERVER or Side CLIENT
     * @param fixDirectory Recreates the directory if true
     * @return the Path of the folder on the specified side
     */
    public static Path getDirectory(String folder, LogicalSide side, boolean fixDirectory)
    {
        String sidedPath = side == LogicalSide.SERVER ? getServerWorldFolder().toString() : ".";
        Path loc = Paths.get(sidedPath, folder);
        if (fixDirectory)
            fixDirectory(loc);
        return loc;
    }

    public static Path getCacheFile(String folder, String filename, LogicalSide sideIn) throws IOException
    {
        Path dir = getDirectory(folder, sideIn);
        Path cacheFile = dir.resolve(filename);

        if(!cacheFile.toFile().exists())
        {
            Files.createDirectories(dir);
            Files.createFile(cacheFile);
        }
        return cacheFile;
    }

    public static boolean fileExists(String folder, String filename, LogicalSide sideIn)
    {
        Path dir = getDirectory(folder, sideIn);
        Path resolve = dir.resolve(filename);
        return resolve.toFile().exists();
    }

    @Nullable
    public static CompoundNBT getCompoundFromFile(@Nullable Path path)
    {
        if (path == null)
            throw new MXTuneRuntimeException("Missing cache file!");

        try
        {
            return CompressedStreamTools.readCompressed(new FileInputStream(path.toFile()));
        }
        catch (IOException e0)
        {
            CompoundNBT nbtTagCompound = new CompoundNBT();
            try
            {
                CompressedStreamTools.writeCompressed(nbtTagCompound, new FileOutputStream(path.toFile()));
                return getCompoundFromFile(path);
            } catch (IOException e1)
            {
                LOGGER.error(e1);
                return null;
            }
        }
    }

    public static void sendCompoundToFile(Path path, CompoundNBT tagCompound)
    {
        try
        {
            CompressedStreamTools.writeCompressed(tagCompound, new FileOutputStream(path.toFile()));
        } catch(IOException e) {
            LOGGER.error(e);
        }
    }
}
