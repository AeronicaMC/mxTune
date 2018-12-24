package net.aeronica.libs.mml.core;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MMLParserFactory
{
    private MMLParserFactory() { /* NOP */ }

    public static MMLParser getMMLParser(String mml) throws IOException
    {
        byte[] mmlBuf;
        mmlBuf = mml.getBytes(StandardCharsets.US_ASCII);

        InputStream is = new java.io.ByteArrayInputStream(mmlBuf);

        /* ANTLR4 MML Parser BEGIN */
        ANTLRInputStream input = null;

        input = new ANTLRInputStream(is);
        MMLLexer lexer = new MMLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new MMLParser(tokens);
    }
}
