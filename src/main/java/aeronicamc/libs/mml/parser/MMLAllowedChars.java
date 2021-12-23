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

public class MMLAllowedChars
{
    // Lexer rules - US_ASCII
    // CMD : [iopstvIOPSTV] ; // MML commands Instrument, Octave, Perform, Sustain, Tempo, Volume
    // LEN : [lL] ; // MML Length command
    // OCTAVE : [<>] ; // Octave down/up
    // NOTE : [a-gA-G] ; // Notes
    // ACC : [+#-] ; // Accidental
    // MIDI : [nN] ; // MIDI note
    // DOT : '.' ; // dotted
    // TIE : '&' ; // Tie
    // REST : [rR] ; // Rests
    // INT : [0-9]+ ; // match integers
    // BEGIN : 'MML@' ; // MML File Begin
    // PART : ',' ; // Part separator
    // END : ';' ; // MML File End
    // WS : [ \t\r\n]+ -> skip ; // toss out whitespace
    // JUNK : [\u0000-~] -> skip ; // anything leftover

    private static final String MML_CHARACTERS_PASTE = "abcdefgABCDEFGrR<>+#-.,&0123456789nNopstvOPSTVlLM@;";
    private static final String MML_CHARACTERS_ONLY = "abcdefgABCDEFGrR<>+#-.&0123456789nNopstvOPSTVlL";
    private MMLAllowedChars() {/* NOP */}

    /**
     * Allow by only standard
     * @param character
     * @param paste
     * @return
     */
    public static boolean isAllowedChar(char character, boolean paste)
    {
        char[] ca = paste ? MML_CHARACTERS_PASTE.toCharArray() : MML_CHARACTERS_ONLY.toCharArray();
        for (int i = 0; i < ca.length; i++)
        {
            if (character == ca[i]) return true;
        }
        return false;
    }

    /**
     * Filter string by only keeping those characters for which isAllowedCharacter() returns true.
     * @param input dirty string
     * @param paste if true allow paste characters ',' ';' 'M' '@' as in 'MML@ ... ,  ... ;'
     * @return
     */
    public static String filter(String input, boolean paste)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (char c0 : input.toCharArray())
        {
            if (isAllowedChar(c0, paste))
            {
                stringbuilder.append(c0);
            }
        }
        return stringbuilder.toString();
    }
}
