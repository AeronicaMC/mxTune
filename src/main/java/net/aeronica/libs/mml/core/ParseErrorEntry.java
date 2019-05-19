package net.aeronica.libs.mml.core;

import org.antlr.v4.runtime.RecognitionException;

public class ParseErrorEntry
{
    private int line;
    private int charPositionInLine;
    private String msg;
    private RecognitionException e;

    public ParseErrorEntry(int line, int charPositionInLine, String msg, RecognitionException e)
    {
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.msg = msg;
        this.e = e;
    }

    public String getMsg() {return msg;}

    public void setLine(int line) {this.line = line;}

    public int getLine() {return line;}

    public int getCharPositionInLine() {return charPositionInLine;}

    public RecognitionException getE() {return e;}
}
