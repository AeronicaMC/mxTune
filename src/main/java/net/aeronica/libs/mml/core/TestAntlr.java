package net.aeronica.libs.mml.core;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestAntlr
{
    private static String mmlString = TestData.MML0.getMML();

    public static InputStream init()
    {
        byte[] mmlBuf = mmlString.getBytes(StandardCharsets.US_ASCII);
        return new java.io.ByteArrayInputStream(mmlBuf);
    }

    public static void main(String[] args) throws Exception
    {
        MMLUtil.MML_LOGGER.info("Test Begin\n");
        InputStream is = init();
        PlayMIDI player = new PlayMIDI();
        MMLToMIDI mmlTrans = new MMLToMIDI();

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


        MMLUtil.MML_LOGGER.info("\n\nTest End");
    }
}
