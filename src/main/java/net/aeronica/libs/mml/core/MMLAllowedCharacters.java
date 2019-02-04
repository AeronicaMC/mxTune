package net.aeronica.libs.mml.core;

public class MMLAllowedCharacters
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

    private static final String MML_CHARACTERS = "abcdefgABCDEFGrR<>+#-.,&0123456789nNopstvOPSTVlLM@;";
    private MMLAllowedCharacters() {/* NOP */}

    public static boolean isAllowedCharacter(char character)
    {
        char[] ca = MML_CHARACTERS.toCharArray();
        for (int i = 0; i < ca.length; i++)
        {
            if (character == ca[i]) return true;
        }
        return false;
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
