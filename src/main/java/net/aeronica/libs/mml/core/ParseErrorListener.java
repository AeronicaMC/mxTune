package net.aeronica.libs.mml.core;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParseErrorListener extends BaseErrorListener implements IParseErrorEntries
{
    private List<ParseErrorEntry> parseErrorList = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
    {
        List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
        Collections.reverse(stack);
        parseErrorList.add(new ParseErrorEntry(line, charPositionInLine, msg, e));
    }

    @Override
    public List<ParseErrorEntry> getParseErrorEntries()
    {
        /// copy the records out then clear the local list
        List<ParseErrorEntry> parseErrorEntries = new ArrayList<>(parseErrorList);
        parseErrorList.clear();
        return parseErrorEntries;
    }
}
