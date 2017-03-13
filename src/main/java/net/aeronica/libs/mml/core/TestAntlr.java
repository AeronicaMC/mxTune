package net.aeronica.libs.mml.core;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestAntlr
{
    private static String mmlString = TestData.MML6.getMML();
    private static byte[] mmlBuf = null;
    public static final Logger logger = LogManager.getLogger();

    public static InputStream init()
    {
        try
        {
            mmlBuf = mmlString.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e)
        {
            logger.error(e);
        }
        return new java.io.ByteArrayInputStream(mmlBuf);
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println("Test Begin\n");
        InputStream is = init();
        PlayMIDI player = new PlayMIDI();
        MMLToMIDI mmlTrans = new MMLToMIDI(1.0F);

        ANTLRInputStream input = new ANTLRInputStream(is);
        MMLLexer lexer = new MMLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MMLParser parser = new MMLParser(tokens);
        // parser.removeErrorListeners();
        // parser.addErrorListener(new UnderlineListener());
        parser.setBuildParseTree(true);
        ParseTree tree = parser.band();
        // show tree in text form
        // System.out.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();

        walker.walk(mmlTrans, tree);

        player.mmlPlay(mmlTrans.getSequence());
        
//        Midi2WavRenderer wr = new Midi2WavRenderer();
//        File f = new File("midifile.wav");
//        wr.createWavFile(mmlTrans.getSequence(), f);
//        MidiSystem.write(mmlTrans.getSequence(),1,f);


        System.out.println("\n\nTest End");
    }
}
