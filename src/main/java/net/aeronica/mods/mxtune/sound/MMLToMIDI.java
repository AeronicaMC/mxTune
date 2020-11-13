/*
 * Aeronica's mxTune MOD
 * Copyright 2020, Paul Boese a.k.a. Aeronica
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
 * MIT License
 *
 * Copyright (c) 2020 Paul Boese a.k.a. Aeronica
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.aeronica.mods.mxtune.sound;

import net.aeronica.libs.mml.parser.MMLObject;
import net.aeronica.mods.mxtune.util.MapListHelper;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SoundFontProxyManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Patch;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.*;

import static net.aeronica.libs.mml.midi.MIDIHelper.*;
import static net.aeronica.libs.mml.parser.MMLUtil.*;

public class MMLToMIDI
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int TICKS_OFFSET = 10;

    private Sequence sequence;
    private final Set<Integer> presets = new HashSet<>();
    private int channel;
    private int track;
    private String currentTransform = "";
    private Map<Integer,Integer> noteTranslations = new HashMap<>();

    public MMLToMIDI() { /* NOP */ }

    public Sequence getSequence() {return sequence;}
    
    @SuppressWarnings("unused")
    public List<Integer> getPresets()
    {
        return new ArrayList<>(presets);
    }
    
    public void processMObjects(List<MMLObject> mmlObjects)
    {
        channel = 0;
        track = 1;
        long ticksOffset = TICKS_OFFSET;
        int currentTempo;

        try
        {
            sequence = new Sequence(Sequence.PPQ, (int) PPQ);
            for (int i = 0; i < MAX_TRACKS; i++)
            {
                sequence.createTrack();
            }
            Track[] tracks = sequence.getTracks();

            for (MMLObject mmo: mmlObjects)
            {
                switch (mmo.getType())
                {
                    case SUSTAIN:
                    case INIT:
                    case REST:
                    case DONE:
                        addText(mmo, tracks, track, channel, ticksOffset);
                        break;

                    case TEMPO:
                        currentTempo = mmo.getTempo();
                        tracks[0].add(createTempoMetaEvent(currentTempo, mmo.getStartingTicks() + ticksOffset));
                        break;

                    case INST:
                        addInstrument(mmo, tracks[track], channel, ticksOffset);
                        addText(mmo, tracks, track, channel, ticksOffset);
                        break;

                    case PART:
                        nextTrack();
                        addText(mmo, tracks, track, channel, ticksOffset);
                        break;

                    case NOTE:
                        addNote(mmo, tracks, track, channel, ticksOffset);
                        addText(mmo, tracks, track, channel, ticksOffset);
                        break;

                    case STOP:
                        nextTrack();
                        nextChannel();
                        addText(mmo, tracks, track, channel, ticksOffset);
                        break;

                    default:
                        LOGGER.debug("MMLToMIDI#processMObjects Impossible?! An undefined enum?");
                }
            }
        } catch (InvalidMidiDataException e)
        {
            LOGGER.error(e);
        }
    }

    private void nextTrack()
    {
        if (track++ > MAX_TRACKS) track = MAX_TRACKS;
    }

    private void nextChannel()
    {
        channel++;
        if (channel == 8) channel = 10; // Skip over percussion channels
        if (channel > 15) channel = 15;
    }

    private void addInstrument(MMLObject mmo, Track track, int ch, long ticksOffset) throws InvalidMidiDataException
    {
        Patch preset = packedPreset2Patch(SoundFontProxyManager.getPackedPreset(mmo.getInstrument()));
        updateCurrentSoundFontProxy(mmo.getInstrument());
        int bank =  preset.getBank();
        int programPreset = preset.getProgram();
        /* Detect a percussion set */
        if (bank == 128)
        {
            /* Set Bank Select for Rhythm Channel MSB 0x78, LSB 0x00  - 14 bits only */
            bank = 0x7800 >>> 1;
        }
        else
        {
            /* Convert the preset bank to the Bank Select bank */
            bank = bank << 7;
        }
        track.add(createBankSelectEventMSB(ch, bank, mmo.getStartingTicks() + ticksOffset-2L));
        track.add(createBankSelectEventLSB(ch, bank, mmo.getStartingTicks() + ticksOffset-1L));
        track.add(createProgramChangeEvent(ch, programPreset, mmo.getStartingTicks() + ticksOffset));
        presets.add(mmo.getInstrument());
    }

    private void updateCurrentSoundFontProxy(int preset)
    {
        if (SoundFontProxyManager.hasTransform(preset))
        {
            currentTransform = SoundFontProxyManager.getTransform(preset);
            noteTranslations.clear();
            noteTranslations = MapListHelper.deserializeIntIntMap(currentTransform);
            ModLogger.debug("Transform: %s", noteTranslations.entrySet());
        } else
        {
            currentTransform = "";
            noteTranslations.clear();
        }
    }

    private int transformNote(int midiNote)
    {
        if (!currentTransform.isEmpty())
            return noteTranslations.getOrDefault(midiNote, midiNote);
        else
            return midiNote;
    }

    private void addNote(MMLObject mmo, Track[] tracks, int track, int channel, long ticksOffset) throws InvalidMidiDataException
    {
        int midiNote = transformNote(mmo.getMidiNote());
        if (mmo.doNoteOn())
            tracks[track].add(createNoteOnEvent(channel, smartClampMIDI(midiNote), mmo.getNoteVolume(), mmo.getStartingTicks() + ticksOffset));
        if (mmo.doNoteOff())
            tracks[track].add(createNoteOffEvent(channel, smartClampMIDI(midiNote), mmo.getNoteVolume(), mmo.getStartingTicks() + mmo.getLengthTicks() + ticksOffset - 1));
    }

    private void addText(MMLObject mmo, Track[] tracks, int track, int channel, long ticksOffset) throws InvalidMidiDataException
    {
        String onOff = String.format("%s%s", mmo.doNoteOn() ? "^" : "-", mmo.doNoteOff() ? "v" : "-");
        String pitch = mmo.getType() == MMLObject.Type.NOTE ? String.format("%s(%03d)", onOff, mmo.getMidiNote()) : "--(---)";
        String text = String.format("{t=% 8d l=% 8d}[T:%02d C:%02d %s %s]{ %s }", mmo.getStartingTicks(),
                                    mmo.getLengthTicks(), track, channel, mmo.getType().name(), pitch, mmo.getText());
        tracks[0].add(createTextMetaEvent(text, mmo.getStartingTicks() + ticksOffset));
    }
}
