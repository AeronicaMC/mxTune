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
package aeronicamc.mods.mxtune.sound;

import aeronicamc.libs.mml.midi.MIDIHelper;
import aeronicamc.libs.mml.parser.MMLObject;
import aeronicamc.libs.mml.parser.MMLUtil;
import aeronicamc.mods.mxtune.util.MXTuneRuntimeException;
import aeronicamc.mods.mxtune.util.MapListHelper;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Patch;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.*;

public class MMLToMIDI
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int OFFSET = 500;
    private TicksOffset ticksOffset;

    private Sequence sequence;
    private final Set<Integer> presets;
    private int channel;
    private int track;
    private String currentTransform = "";
    private Map<Integer,Integer> noteTranslations;

    public MMLToMIDI()
    {
        noteTranslations = new HashMap<>();
        presets = new HashSet<>();
    }

    public Sequence getSequence()
    {
        if (sequence == null)
            throw new MXTuneRuntimeException("Must use the method processMObjects before accessing getSequence!");
        else
            return sequence;
    }
    
    public List<Integer> getPresets()
    {
        return new ArrayList<>(presets);
    }

    public void processMObjects(List<MMLObject> mmlObjects)
    {
        ticksOffset = new TicksOffset(OFFSET);
        channel = 0;
        track = 1; // Track 0 is reserved for tempo and text

        try
        {
            sequence = new Sequence(Sequence.PPQ, (int) MMLUtil.PPQ);
            for (int i = 0; i <= MMLUtil.MAX_TRACKS; i++)
            {
                sequence.createTrack();
            }
            Track[] tracks = sequence.getTracks();

            for (MMLObject mmo: mmlObjects)
            {
                switch (mmo.getType())
                {
                    case INIT:
                    case REST:
                    case DONE:
                        addText(mmo, tracks, track, channel);
                        break;

                    case SUSTAIN:
                        setSustain(mmo, tracks, track, channel);
                        addText(mmo, tracks, track, channel);
                        break;

                    case TEMPO:
                        setTempo(mmo, tracks);
                        addText(mmo, tracks, track, channel);
                        break;

                    case INST:
                        addInstrument(mmo, tracks, track, channel);
                        addText(mmo, tracks, track, channel);
                        break;

                    case PART:
                        nextTrack();
                        addText(mmo, tracks, track, channel);
                        break;

                    case NOTE:
                        addNote(mmo, tracks, track, channel);
                        addText(mmo, tracks, track, channel);
                        break;

                    case STOP:
                        nextTrack();
                        nextChannel();
                        addText(mmo, tracks, track, channel);
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

    /**
     * For loop limited by MMLUtil.MAX_TRACKS
     */
    private void nextTrack()
    {
        track++;
    }

    private void nextChannel()
    {
        channel++;
        if (channel > 15) channel = 15;
    }

    private void addInstrument(MMLObject mmo, Track[] tracks, int track, int ch) throws InvalidMidiDataException
    {
        Patch preset = MMLUtil.packedPreset2Patch(SoundFontProxyManager.getPackedPreset(mmo.getInstrument()));
        updateCurrentSoundFontProxy(mmo.getInstrument());
        int bank =  preset.getBank();
        int bankMSB;
        int bankLSB;
        int programPreset = preset.getProgram();
        /* Detect a percussion set */
        if (bank == 128)
        {
            /* Set Bank Select for Rhythm Channel MSB 0x78, LSB 0x00  - 14 bits only */
            bankMSB = 0x7800 >>> 1;
            bankLSB = 0;
        }
        else
        {
            /* Set Bank Select for Melodic Channel MSB 0x79, LSB 0x00 - 14 bits only */
            bankMSB = 0x7900 >>> 1;
            bankLSB = 0;
        }
        tracks[track].add(MIDIHelper.createBankSelectEventMSB(ch, bankMSB, 0));
        tracks[track].add(MIDIHelper.createBankSelectEventLSB(ch, bankLSB, 0));
        tracks[track].add(MIDIHelper.createProgramChangeEvent(ch, programPreset, 1));
        presets.add(mmo.getInstrument());
    }

    private void updateCurrentSoundFontProxy(int preset)
    {
        if (SoundFontProxyManager.hasTransform(preset))
        {
            currentTransform = SoundFontProxyManager.getTransform(preset);
            noteTranslations.clear();
            noteTranslations = MapListHelper.deserializeIntIntMap(currentTransform);
            LOGGER.debug("Transform: {}", noteTranslations.entrySet());
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

    private void addNote(MMLObject mmo, Track[] tracks, int track, int channel) throws InvalidMidiDataException
    {
        int midiNote = transformNote(mmo.getMidiNote());
        if (mmo.doNoteOn())
            tracks[track].add(MIDIHelper.createNoteOnEvent(channel, MMLUtil.smartClampMIDI(midiNote), mmo.getNoteVolume(), ticksOffset.apply(mmo.getStartingTicks())));
        if (mmo.doNoteOff())
            tracks[track].add(MIDIHelper.createNoteOffEvent(channel, MMLUtil.smartClampMIDI(midiNote), mmo.getNoteVolume(), ticksOffset.apply(mmo.getStartingTicks(), mmo.getLengthTicks(), -1L)));
    }

    private void addText(MMLObject mmo, Track[] tracks, int track, int channel) throws InvalidMidiDataException
    {
        String onOff = String.format("%s%s", mmo.doNoteOn() ? "^" : "-", mmo.doNoteOff() ? "v" : "-");
        String pitch = mmo.getType() == MMLObject.Type.NOTE ? String.format("%s(%03d)", onOff, mmo.getMidiNote()) : "--(---)";
        String text = String.format("{t=% 8d l=% 8d}[T:%02d C:%02d %s %s]{ %s }", mmo.getStartingTicks(),
                                    mmo.getLengthTicks(), track, channel, mmo.getType().name(), pitch, mmo.getText());
        tracks[0].add(MIDIHelper.createTextMetaEvent(text, ticksOffset.apply(mmo.getStartingTicks())));
    }

    private void setTempo(MMLObject mmo, Track[] tracks) throws InvalidMidiDataException
    {
        tracks[0].add(MIDIHelper.createTempoMetaEvent(mmo.getTempo(), ticksOffset.apply(mmo.getStartingTicks())));
    }

    private void setSustain(MMLObject mmo, Track[] tracks, int track, int channel) throws InvalidMidiDataException
    {
        int sustain = mmo.doSustain() ? 127 : 0;
        tracks[track].add(MIDIHelper.createControlChangeEvent(channel, 64, sustain, ticksOffset.apply(mmo.getStartingTicks())));
    }

    private static class TicksOffset
    {
        private final long offset;

        public TicksOffset(long offset) { this.offset = offset; }

        /**
         * Sum ticks and apply an offset
         * @param ticks vararg list of longs
         * @return sum of arguments plus an offset
         */
        long apply(Long... ticks)
        {
            final long[] value = new long[1];
            Arrays.asList(ticks).forEach(tick -> value[0] += tick);
            return value[0] + offset;
        }
    }
}
