/*
 * Copyright (C) 2014 たんらる
 *
 * https://github.com/fourthline/mmlTools
 * https://twitter.com/fourthline
 */

/*
 * 2019-05-30 Paul Boese, a.k.a. Aeronica
 * Initial import from https://github.com/fourthline/mmlTools
 * Used with permission.
 */

package net.aeronica.libs.mml.readers.mml3mle;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class TextParser
{
    private final String text;
    private HashMap<String, Consumer<String>> map = new HashMap<>();

    private TextParser(String text)
    {
        this.text = text;
    }

    public static TextParser text(String test)
    {
        return new TextParser(test);
    }

    public TextParser pattern(String s, Consumer<String> func)
    {
        map.put(s, func);
        return this;
    }

    public void parse()
    {
        Pattern.compile("\n").splitAsStream(this.text).forEachOrdered(lineText ->
            map.keySet().forEach(key ->
                {
                    if (lineText.startsWith(key))
                        map.get(key).accept(lineText.substring(key.length()));
                }));
    }
}
