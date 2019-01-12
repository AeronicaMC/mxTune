package net.aeronica.libs.mml.core;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static net.aeronica.libs.mml.readers.ms2mml.TestMs2Mml.getFile;
import static net.aeronica.libs.mml.readers.ms2mml.TestMs2Mml.viewMs2Mml;

public class TestAntlr
{
    //private static String mmlString = TestData.MML4.getMML();

    public static InputStream init()
    {
        byte[] mmlBuf = new byte[0];
        try
        {
            mmlBuf = viewMs2Mml(getFile("E:\\Users\\Paul\\Downloads\\darling-in-the-franxx-ed5-escape-r2518.ms2mml")).getBytes(StandardCharsets.US_ASCII);
            //mmlBuf = viewZipFileContents("E:\\Users\\Paul\\Downloads\\tokyo-ghoul-unravel-r11.zip").getBytes(StandardCharsets.US_ASCII);
            //mmlBuf = viewMs2Mml(getFile("E:\\Users\\Paul\\Downloads\\killerblood-run-lads-run-r8.ms2mml")).getBytes(StandardCharsets.US_ASCII);
        } catch (JAXBException e)
        {
            MMLUtil.MML_LOGGER.error(e);
        }
        //mmlBuf = mmlString.getBytes(StandardCharsets.US_ASCII);
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
