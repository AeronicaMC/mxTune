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
package aeronicamc.libs.mml.parser;


public class ElementTypes
{
    private ElementTypes() { /* NOP */ }

    public static final byte MML_INSTRUMENT = 1;    // [iI]<0-99999> Instrument Command in packed preset format
    public static final byte MML_LENGTH = 2;        // [lL] Length Command
    public static final byte MML_OCTAVE = 3;        // [oO]<1-8> Octave Command
    public static final byte MML_PERFORM = 4;       // [pP]<1-5> Perform Command
    public static final byte MML_SUSTAIN = 5;       // [sS]<0-1> Sustain Command
    public static final byte MML_TEMPO = 6;         // [tT]<32-255> Tempo Command
    public static final byte MML_VOLUME = 7;        // [vV]<1-15 | 0-127> Volume Command in "Mabinogi/Maple Story 2" or ArcheAge levels
    public static final byte MML_OCTAVE_UP = 8;     // [>] Octave Up Command
    public static final byte MML_OCTAVE_DOWN = 9;   // [<] Octave Down Command
    public static final byte MML_NOTE = 10;         // [a-gA-G] Notes
    public static final byte MML_SHARP = 11;        // [+#] Accidental Sharp
    public static final byte MML_FLAT = 12;         // [-] Accidental Flat
    public static final byte MML_MIDI = 13;         // [nN] MIDI note
    public static final byte MML_DOT = 14;          // '.' dotted
    public static final byte MML_TIE = 15;          // '&' Tie
    public static final byte MML_REST = 16;         // [rR] Rest
    public static final byte MML_NUMBER = 17;       // <0-99999> Positive Integer
    public static final byte MML_BEGIN = 18;        // 'MML@' MML Begin
    public static final byte MML_CHORD = 19;        // ',' Add Chord/Harmony/Counterpoint
    public static final byte MML_END = 20;          // ';' MML End
    public static final byte EOF = 21;              // End of file
}
