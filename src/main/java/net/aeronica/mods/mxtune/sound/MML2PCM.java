/*
 * Copyright {2016} Paul Boese aka Aeronica
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.sound;

import net.aeronica.libs.mml.core.MMLParser;
import net.aeronica.libs.mml.core.MMLParserFactory;
import net.aeronica.libs.mml.core.MMLToMIDI;
import net.aeronica.libs.mml.core.MMLUtil;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.client.resources.I18n;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.util.Map;

public class MML2PCM
{
    /**
     * Solo play format "<playerName|groupID>=mml@...;"
     * 
     * Jam play format inserts with a space between each player=MML sequence
     * "<playername1>=MML@...abcd; <playername2>=MML@...efgh; <playername2>=MML@...efgh;"
     * 
     * @param playID the unique id of the play session
     * @param musicText MML in mapped format
     * @return false if errors
     */
    public boolean process(int playID, String musicText)
    {
        // TODO: Too many steps here. Make a method to deserialize and append the mml then check for null/empty
        Map<Integer, String> playerMML = GROUPS.deserializeIntStrMap(musicText);
        if (playerMML == null)
        {
            ModLogger.error("MML2PCM playerMML is null! Check for an issue with NBT, networking, threads. PlayID", playID, musicText);
            ModLogger.error("MML2PCM PlayID: %d", playID);
            ModLogger.error("MML2PCM MML: %s", musicText.substring(0, musicText.length() >= 60 ? 60 : musicText.length()));
            ClientAudio.setPlayIDAudioDataStatus(playID, Status.ERROR);
            return false;
        }

        /* Append all the MML */
        StringBuilder mml = new StringBuilder();
        for (Map.Entry<Integer, String> integerStringMap: playerMML.entrySet())
            mml.append(integerStringMap.getValue());

        MMLParser parser;
        try
        {
            parser = MMLParserFactory.getMMLParser(mml.toString());
        }
        catch (IOException e)
        {
            ModLogger.debug("MMLParserFactory.getMMLParser() IOException in %s, Error: %s", SheetMusicUtil.class.getSimpleName(), e);
            ClientAudio.setPlayIDAudioDataStatus(playID, Status.ERROR);
            return false;
        }
        parser.setBuildParseTree(true);
        ParseTree tree = parser.band();

        ParseTreeWalker walker = new ParseTreeWalker();
        MMLToMIDI mmlTrans = new MMLToMIDI();
        walker.walk(mmlTrans, tree);
        /* ANTLR4 MML Parser END */

        /* Log used programs */
        for (int packedPreset: mmlTrans.getPackedPresets())
        {
            Patch patchPreset = MMLUtil.packedPreset2Patch(packedPreset);
            String name = I18n.format(MIDISystemUtil.getPatchNameKey(patchPreset));
            ModLogger.info("MML2PCM preset: bank: %3d, program %3d, name %s", patchPreset.getBank(),
                           patchPreset.getProgram(), name);
        }
      
        try
        {
            Midi2WavRenderer mw = new Midi2WavRenderer();
            AudioInputStream ais = mw.createPCMStream(mmlTrans.getSequence(), ClientAudio.getAudioFormat(playID));
            ClientAudio.setPlayIDAudioStream(playID, ais);
        } catch (MidiUnavailableException | InvalidMidiDataException | IOException e)
        {
            ModLogger.error(e);
            ClientAudio.setPlayIDAudioDataStatus(playID, Status.ERROR);
            ModLogger.error("MML2PCM process: MidiUnavailableException | InvalidMidiDataException | IOException", e);
            return false;
        }
        ClientAudio.setPlayIDAudioDataStatus(playID, Status.READY);
        return true;
    }
}
