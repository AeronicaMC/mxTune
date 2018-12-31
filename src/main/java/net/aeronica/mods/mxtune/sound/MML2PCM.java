/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.sound;

import net.aeronica.libs.mml.core.MMLParser;
import net.aeronica.libs.mml.core.MMLParserFactory;
import net.aeronica.libs.mml.core.MMLToMIDI;
import net.aeronica.libs.mml.core.MMLUtil;
import net.aeronica.mods.mxtune.groups.GROUPS;
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

import static net.aeronica.mods.mxtune.sound.ClientAudio.Status.ERROR;
import static net.aeronica.mods.mxtune.sound.ClientAudio.Status.READY;

public class MML2PCM
{
    /**
     * Solo play format "<playerName|groupID>=mml@...;"
     * 
     * Jam play format inserts with a space between each player=MML sequence
     * "<playername1>=MML@...abcd; <playername2>=MML@...efgh; <playername2>=MML@...efgh;"
     * 
     * @param playID the unique id of the play session
     * @param jamFormatMMLIn MML in mapped format
     * @return false if errors
     */
    public boolean process(int playID, String jamFormatMMLIn)
    {
        // Deserialize JAM Formatted MML
        Map<Integer, String> jamFormatMMLMap = GROUPS.deserializeIntStrMap(jamFormatMMLIn);
        if (jamFormatMMLMap.isEmpty())
        {
            ModLogger.error("MML2PCM jamFormatMMLMap is null! Check for an issue with NBT, networking, threads. PlayID: %s", playID);
            ModLogger.error("MML2PCM PlayID: %d", playID);
            ClientAudio.setPlayIDAudioDataStatus(playID, ERROR);
            return false;
        }

        // Re-append all the MML without '|' and '=' symbols
        StringBuilder parseReadyMML = new StringBuilder();
        for (Map.Entry<Integer, String> integerStringMap: jamFormatMMLMap.entrySet())
            parseReadyMML.append(integerStringMap.getValue());

        MMLParser mmlParser;
        try
        {
            mmlParser = MMLParserFactory.getMMLParser(parseReadyMML.toString());
        }
        catch (IOException e)
        {
            ModLogger.debug("MMLParserFactory.getMMLParser() IOException in %s, Error: %s", SheetMusicUtil.class.getSimpleName(), e);
            ClientAudio.setPlayIDAudioDataStatus(playID, ERROR);
            return false;
        }
        mmlParser.setBuildParseTree(true);
        ParseTree tree = mmlParser.band();

        ParseTreeWalker walker = new ParseTreeWalker();
        MMLToMIDI mmlToMIDI = new MMLToMIDI();
        walker.walk(mmlToMIDI, tree);
        // ANTLR4 MML Parser END

        // Log bank and program per instrument
        for (int packedPreset: mmlToMIDI.getPackedPresets())
        {
            Patch patchPreset = MMLUtil.packedPreset2Patch(packedPreset);
            String name = I18n.format(MIDISystemUtil.getPatchNameKey(patchPreset));
            ModLogger.info("MML2PCM preset: bank: %3d, program %3d, name %s", patchPreset.getBank(),
                           patchPreset.getProgram(), name);
        }

        // Generate PCM Audio Stream from MIDI
        try
        {
            Midi2WavRenderer mw = new Midi2WavRenderer();
            AudioInputStream pcmStream = mw.createPCMStream(mmlToMIDI.getSequence(), ClientAudio.getAudioFormat(playID));
            ClientAudio.setPlayIDAudioStream(playID, pcmStream);
        } catch (MidiUnavailableException | InvalidMidiDataException | IOException e)
        {
            ModLogger.error(e);
            ClientAudio.setPlayIDAudioDataStatus(playID, ERROR);
            ModLogger.error("MIDI to PCM process: MidiUnavailableException | InvalidMidiDataException | IOException", e);
            return false;
        }
        ClientAudio.setPlayIDAudioDataStatus(playID, READY);
        return true;
    }
}
