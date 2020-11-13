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
package net.aeronica.libs.mml.parser;

import javax.annotation.Nonnull;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;

@SuppressWarnings("unused")
public enum MMLUtil
{
    ;
    public static final double PPQ = 480.0;
    public static final int MAX_TRACKS = 160;
    // 8 players with 10 parts each = 80 parts.
    // 12 slots with 10 parts each = 120 parts.
    // No problem :D, but will make it 160 because we can!
    /* int[0] is not used. Used to reorder ASCII letters to musical notes taking into account accidentals. That is ABCDEFG to CDEFGAB */

    private static final int[] DOE_RE_MI = {50, 9, 11, 0, 2, 4, 5, 7};

    /**
     * Convert character and octave to a MIDI note value
     * @param rawNote US ASCII character value
     * @param mmlOctave in the range of 0-8
     * @return MIDI note
     */
    public static int getMIDINote(char rawNote, int mmlOctave)
    {
        int doreNote = 0;
        /* Convert Note value to integer: aA=1, gG=7 */
        if (rawNote >=65 && rawNote <= 71)
            doreNote = rawNote - 64;
        else
            doreNote = rawNote - 96;

        /* Get start of the MML Octave */
        int octave = (mmlOctave * 12) + 12;
        /* combine the octave and reordered note to get the MIDI note value */
        return octave + DOE_RE_MI[doreNote];
    }

    public static int getMIDINote(char rawNote, int mmlOctave, boolean rest)
    {
        int midiNote = getMIDINote(rawNote, mmlOctave);
        return midiNote + (rest ? 128 : 0);
    }
    
    public static int smartClampMIDI(int midiNoteIn)
    {
        int midiNoteClamped = midiNoteIn;
        while (midiNoteClamped < 0 || midiNoteClamped > 127)
        {
            if (midiNoteClamped < 0) midiNoteClamped += 12;
            if (midiNoteClamped > 127) midiNoteClamped -= 12;
        }
        return midiNoteClamped;
    }

    public static int clamp(int min, int max, int value) {return Math.max(Math.min(max, value), min);}

    /**
     * Unpacks the packedPreset and returns the {@link Patch}<br/>
     * Note that his is not always going to return what you might expect. Tread carefully!
     * @param packedPatchIn the packed bank and program represented as a single integer
     * @return javax.sound.midi.Patch
     */
    public static Patch packedPreset2Patch(int packedPatchIn)
    {
        int program = packedPatchIn & 0x7F;
        int bank = 0;
        if (packedPatchIn > 0x7F)
        {
            bank = (packedPatchIn & 0x1FFF80) >>> 7;
        }
        return new Patch(bank, program);
    }

    /**
     * A convenience method for building a packed integer representation of
     * the bank and program of a soundfont preset
     * @param bankIn 0-128
     * @param programIn 0-127
     * @return the packed integer soundfont Preset
     */
    public static int preset2PackedPreset(int bankIn, int programIn)
    {
        int bank = bankIn < 128 ? bankIn : 128;
        int patch = programIn & 0x7F;
        return (bank << 7) + patch;
    }

    /**
     * Returns a packed integer representation of a ({@link Patch})<br/>
     * Note that his is not always going to return what you might expect. Tread carefully!<br/>
     * e.g. DO NOT USE with {@link Patch javax.sound.midi.Instrument.getPatch()}
     * @param patchIn the Patch
     * @return the packed integer representation of a Patch
     */
    public static int patch2PackedPreset(Patch patchIn)
    {
        return preset2PackedPreset(patchIn.getBank(), patchIn.getProgram());
    }

    public static int instrument2PackedPreset(@Nonnull Instrument instrument)
    {
        /* Table Flip! */
        boolean isPercussionSet = instrument.toString().contains("Drumkit:");
        /* A SoundFont 2.04 preset allows 128 banks 0-127) plus the percussion
         * set for 129 sets! OwO However when you get a patch from an
         * Instrument from a loaded soundfont you will find the bank value
         * for the preset is left shifted 7 bits. However what's worse is that
         * for preset bank:128 the value returned by getBank() is ZERO!
         * So as a workaround check the name of instrument to see if it's a percussion set.
         */
        int bank = instrument.getPatch().getBank() >>> 7;
        int program = instrument.getPatch().getProgram();
        return isPercussionSet ? MMLUtil.preset2PackedPreset(128, program) : MMLUtil.preset2PackedPreset(bank, program);
    }
}
