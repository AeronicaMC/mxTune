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

import net.aeronica.libs.mml.parser.MMLParser;
import net.aeronica.libs.mml.parser.MMLParserFactory;
import net.aeronica.libs.mml.parser.MMLUtil;
import net.aeronica.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.client.resources.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.sampled.AudioInputStream;

import static net.aeronica.mods.mxtune.sound.ClientAudio.Status.ERROR;
import static net.aeronica.mods.mxtune.sound.ClientAudio.Status.READY;

public class MML2PCM
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final AudioData audioData;
    private final String mmlText;

    MML2PCM(AudioData audioData, String mmlText)
    {
        this.audioData = audioData;
        this.mmlText = mmlText;
    }

    public boolean process()
    {
        MMLParser mmlParser;

        mmlParser = MMLParserFactory.getMMLParser(mmlText);
        MMLToMIDI toMIDI = new MMLToMIDI();
        toMIDI.processMObjects(mmlParser.getMmlObjects());
        // Log bank and program per instrument
        for (int preset: toMIDI.getPresets())
        {
            Patch patchPreset = MMLUtil.packedPreset2Patch(SoundFontProxyManager.getPackedPreset(preset));
            // FIXME: Language Format
            String name = I18n.get(SoundFontProxyManager.getLangKeyName(preset));
            LOGGER.debug("MML2PCM preset: {}, bank: {}, program: {}, name: {}", preset, patchPreset.getBank(),
                           patchPreset.getProgram(), name);
        }

        // Generate PCM Audio Stream from MIDI
        try
        {
            Midi2WavRenderer mw = new Midi2WavRenderer();
            AudioInputStream pcmStream = mw.createPCMStream(toMIDI.getSequence(), audioData.getAudioFormat());
            audioData.setAudioStream(pcmStream);
        } catch (ModMidiException | MidiUnavailableException e)
        {
            audioData.setStatus(ERROR);
            LOGGER.error("MIDI to PCM process: ", e);
            return false;
        }
        audioData.setStatus(READY);
        return true;
    }
}
