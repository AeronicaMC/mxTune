package net.aeronica.libs.mml.core;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

public class TestE_Listener2
{
    public static class UnderlineListener extends BaseErrorListener
    {
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
        {
            System.err.println("line " + line + ":" + charPositionInLine + " " + msg);
            underlineError(recognizer, (Token) offendingSymbol, line, charPositionInLine);
        }

        @SuppressWarnings("rawtypes")
        protected void underlineError(Recognizer recognizer, Token offendingToken, int line, int charPositionInLine)
        {
            CommonTokenStream tokens = (CommonTokenStream) recognizer.getInputStream();
            String input = tokens.getTokenSource().getInputStream().toString();
            String[] lines = input.split("\n");
            String errorLine = lines[line - 1];
            System.err.println(errorLine);
            for (int i = 0; i < charPositionInLine; i++)
                System.err.print(" ");
            int start = offendingToken.getStartIndex();
            int stop = offendingToken.getStopIndex();
            if (start >= 0 && stop >= 0)
            {
                for (int i = start; i <= stop; i++)
                    System.err.print("^");
            }
            System.err.println();
        }
    }
    // public static void main(String[] args) throws Exception {
    // ANTLRInputStream input = new ANTLRInputStream(System.in);
    // SimpleLexer lexer = new SimpleLexer(input);
    // CommonTokenStream tokens = new CommonTokenStream(lexer);
    // SimpleParser parser = new SimpleParser(tokens);
    // parser.removeErrorListeners(); // remove ConsoleErrorListener
    // parser.addErrorListener(new UnderlineListener());
    // parser.prog();
    // }
}