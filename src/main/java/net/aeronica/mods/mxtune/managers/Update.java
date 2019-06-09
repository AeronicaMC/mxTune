/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"},
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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.gui.mml.ImportHelper;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.mxt.MXTuneFile;
import net.aeronica.mods.mxtune.mxt.MXTunePart;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Update
{
    private static final Map<Integer, String> PACKED_PATCH_TO_NAME =
            new ImmutableMap.Builder<Integer, String>()
            .put(0, "Acoustic Piano")
            .put(4, "Electric Piano")
            .put(5, "FM Piano")
            .put(6, "Harpsichord")
            .put(8, "Celeste")
            .put(9, "Glockenspiel")
            .put(10, "Music Box")
            .put(11, "Vibraphone")
            .put(13, "Xylophone")
            .put(19, "Pipe Organ")
            .put(21, "Accordion")
            .put(22, "Harmonica")
            .put(24, "Nylon Guitar")
            .put(25, "Steel Guitar")
            .put(26, "Jazz Guitar")
            .put(29, "Overdrive Guitar")
            .put(30, "Distortion Guitar")
            .put(32, "Acoustic Bass")
            .put(33, "Fingered Bass")
            .put(34, "Pick Bass")
            .put(40, "Violin")
            .put(41, "Viola")
            .put(42, "Cello")
            .put(43, "Double Bass")
            .put(45, "Pizzicato")
            .put(46, "Harp")
            .put(47, "Timpani")
            .put(48, "Strings")
            .put(49, "Slow Strings")
            .put(50, "Synth Strings")
            .put(52, "Concert Choir")
            .put(55, "Orchestra Hit")
            .put(56, "Trumpet")
            .put(57, "Trombone")
            .put(58, "Tuba")
            .put(59, "Muted Trumpet")
            .put(61, "Brass Section")
            .put(64, "Soprano Sax")
            .put(65, "Alto Sax")
            .put(66, "Tenor Sax")
            .put(67, "Baritone Sax")
            .put(68, "Oboe")
            .put(69, "English Horn")
            .put(70, "Bassoon")
            .put(71, "Clarinet")
            .put(72, "Piccolo")
            .put(73, "Flute")
            .put(74, "Recorder")
            .put(75, "Pan Flute")
            .put(78, "Irish Tin Whistle")
            .put(79, "Ocarina")
            .put(104, "Sitar")
            .put(105, "Banjo")
            .put(106, "Shamisen")
            .put(107, "Koto")
            .put(109, "Bagpipes")
            .put(110, "Fiddle")
            .put(111, "Shenai")
            .put(114, "Steel Drums")
            .put(116, "Taiko Drums")
            .put(117, "Melodic Tom")
            .put(134, "Coupled Harpsichord")
            .put(208, "Square Wave")
            .put(209, "Saw Wave")
            .put(1043, "Pipe Organ 2")
            .put(1104, "Sine Wave")
            .put(1536, "M Lute")
            .put(1537, "M Ukulele")
            .put(1538, "M Mandolin")
            .put(1539, "M Whistle")
            .put(1540, "M Roncadora")
            .put(1541, "M Flute")
            .put(1542, "M Chalumeau")
            .put(1554, "M Tuba")
            .put(1555, "M Lyre")
            .put(1556, "M Electric Guitar")
            .put(1557, "M Piano")
            .put(1558, "M Violin")
            .put(1559, "M Cello")
            .put(1560, "M Harp")
            .put(1591, "T Flute")
            .put(1592, "T Whistle")
            .put(1602, "M Bass Drum")
            .put(1603, "M Snare")
            .put(1604, "M Cymbals")
            .put(1613, "M Hand Chime")
            .put(16384, "Standard Set")
            .put(16432, "Orchestra Set")
            .build();
    
    private Update() { /* NOP */ }

    /**
     * A one time update for SNAPSHOT x to 29+
     */
    static void renameAreasToPlaylists()
    {
        Path oldDir = FileHelper.getDirectory(FileHelper.SERVER_FOLDER + "/areas", Side.SERVER, false);
        Path newDir = FileHelper.getDirectory(FileHelper.SERVER_PLAY_LISTS_FOLDER, Side.SERVER, false);
        ModLogger.info("Try renameAreasToPlaylists()");
        if (oldDir.toFile().exists() && oldDir.toFile().isDirectory())
        {
            try
            {
                Files.move(oldDir, newDir, StandardCopyOption.ATOMIC_MOVE);
                ModLogger.info("...Renaming <world>/mxtune/areas to <world>/mxtune/playlists");
            }
            catch (IOException e)
            {
                ModLogger.info("...Failed to rename <world>/mxtune/areas to <world>/mxtune/playlists");
                ModLogger.error(e);
            }
        } else
            ModLogger.info("...Nothing to do");
    }

    /**
     * A one time update for SNAPSHOT x to 29+
     * Song#getMml input = MML@I=1536t240v12l8dfa&adab&b;MML@I=1537t240v12l8<dfa&adab&b;MML@I=1613t240v12l8>dfa&adab&b;
     */
    static void convertSongToMxt()
    {
        List<Path> songFiles = new ArrayList<>();
        boolean error;

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
            ModLogger.info("convertSongToMxt: Aborted reading <song>.dat files: %e", e.getLocalizedMessage());
        }

        for (Path songFile : songFiles)
        {
            error = false;
            // Create MXTuneFile from Song
            MXTuneFile mxTuneFile = new MXTuneFile();
            NBTTagCompound songCompound = FileHelper.getCompoundFromFile(songFile);
            Song song = new Song(songCompound);
            String mxtFileName = FileHelper.removeExtension(song.getFileName()) + FileHelper.EXTENSION_MXT;
            mxTuneFile.setTitle(song.getTitle());

            for (Tuple<Integer, String> songPart : getSongParts(song))
            {
                int packedPatch = songPart.getFirst();
                MXTunePart mxTunePart = new MXTunePart(PACKED_PATCH_TO_NAME.get(packedPatch), "Converted from Song file", packedPatch, ImportHelper.getStaves(songPart.getSecond()));
                mxTuneFile.getParts().add(mxTunePart);
            }

            // Write <songID>.mxt
            NBTTagCompound compoundMxt = new NBTTagCompound();
            mxTuneFile.writeToNBT(compoundMxt);
            try
            {
                path = FileHelper.getCacheFile(FileHelper.SERVER_MUSIC_FOLDER, mxtFileName, Side.SERVER);
                FileHelper.sendCompoundToFile(path, compoundMxt);
            }
            catch (IOException e)
            {
                ModLogger.warn(e);
                ModLogger.info("convertSongToMxt: Write Error in %s for %s", FileHelper.SERVER_MUSIC_FOLDER, song.getFileName());
                error = true;
            }

            // Delete <songID.dat>
            try
            {
                if (!error && !songFile.toFile().isDirectory() && songFile.toFile().exists())
                    Files.delete(songFile);
            } catch (IOException e)
            {
                ModLogger.error(e);
                ModLogger.info("convertSongToMxt: Delete Error in %s for %s", FileHelper.SERVER_MUSIC_FOLDER, song.getFileName());
            }
        }
    }

    private static List<Tuple<Integer, String>> getSongParts(Song song)
    {
        final List<Tuple<Integer, String>> songParts = new ArrayList<>();
        Pattern patternSplit = Pattern.compile("MML@I=");
        Iterable<String> parts = Splitter.onPattern(patternSplit.pattern()).omitEmptyStrings().split(song.getMml());
        for (String part: parts)
        {
            Tuple<Integer, String> pm = getPatchAndMml(part);
            songParts.add(pm);
        }
        return songParts;
    }

    private static Tuple<Integer, String> getPatchAndMml(String part)
    {
        Pattern patternPatch = Pattern.compile("(^([0-9]){1,5})");
        int patch = 0;
        Matcher matcher = patternPatch.matcher(part);
        if (matcher.find())
        {
            patch = Integer.parseInt(matcher.group(0));
            part = part.replaceFirst(patternPatch.pattern(), "");
        }
        return new Tuple<>(patch, part);
    }
}
