package net.aeronica.libs.mml.core;

public class MMLUtil
{
    /** int[0] is not used. Used to reorder ASCII letters to musical notes taking into account accidentals. That is ABCDEFG to CDEFGAB */
    private static final int[] doerayme = {50, 9, 11, 0, 2, 4, 5, 7};

    public static int getMIDINote(int rawNote, int mmlOctave)
    {
        /** Convert ASCII UPPER CASE Note value to integer: A=1, G=7 */
        int doreNote = rawNote - 64;
        /** Get start of the MML Octave */
        int octave = (mmlOctave * 12) + 12;
        /** combine the octave and reordered note to get the MIDI note value */
        return octave + doerayme[doreNote];
    }

    public static int getMIDINote(int rawNote, int mmlOctave, boolean rest)
    {
        int midiNote = getMIDINote(rawNote, mmlOctave);
        return midiNote + (rest ? 128 : 0);
    }
    
    public static int smartClampMIDI(int midiNote)
    {
        while (midiNote < 0 || midiNote > 127)
        {
            if (midiNote < 0) midiNote += 12;
            if (midiNote > 127) midiNote -= 12;
        }
        return midiNote;
    }
}
