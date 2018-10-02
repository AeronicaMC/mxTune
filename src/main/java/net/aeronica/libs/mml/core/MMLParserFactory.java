package net.aeronica.libs.mml.core;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static net.aeronica.libs.mml.core.MMLUtil.MML_LOGGER;

public class MMLParserFactory
{
    private MMLParserFactory() {/* NOP */}

    @Nullable
    public static MMLParser getMMLParser(String mml)
    {
        byte[] mmlBuf = null;
        try
        {
            mmlBuf = mml.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e)
        {
            MML_LOGGER.error(e);
        }
        if (mmlBuf == null) return null;
        InputStream is = new java.io.ByteArrayInputStream(mmlBuf);

        /* ANTLR4 MML Parser BEGIN */
        ANTLRInputStream input = null;

        try
        {
            input = new ANTLRInputStream(is);
        } catch (IOException e)
        {
            MML_LOGGER.error(e);
        }
        MMLLexer lexer = new MMLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new MMLParser(tokens);
    }
}
