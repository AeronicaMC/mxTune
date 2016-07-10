package net.aeronica.libs.mml.core;

public class MMLAllowedCharacters
{
    // Lexer rules - US_ASCII
    // CMD : [iotvIOTV] ; // MML commands Instrument, Octave, Tempo, Volume
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

    private static final String mmlCharacters = "abcdefgABCDEFGrR<>+#-.,&0123456789nNotvOTVlLM@;";

    /** TODO: might be worthwhile to try a short circuit && approach like the original, but this is okay for now. */
    public static boolean isAllowedCharacter(char character)
    {
        char[] ca = mmlCharacters.toCharArray();
        for (int i = 0; i < ca.length; i++)
        {
            if (character == ca[i]) return true;
        }
        return false;
        // return character != 167 && character >= 32 && character != 127;
    }

    /** Filter string by only keeping those characters for which isAllowedCharacter() returns true. */
    public static String filterAllowedCharacters(String input)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (char c0 : input.toCharArray())
        {
            if (isAllowedCharacter(c0))
            {
                stringbuilder.append(c0);
            }
        }
        return stringbuilder.toString();
    }
}
