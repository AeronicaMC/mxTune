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

import net.aeronica.libs.mml.core.MMLUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * テキストをセクションに区切って扱います.
 * [section-name]
 * text...
 */
public final class SectionContents
{
    private final String name;
    private final StringBuilder buffer = new StringBuilder();

    private SectionContents(String name)
    {
        this.name = name;
    }

    /**
     * InputStreamからセクションのリストを作成します.
     *
     * @param istream Data stream
     * @return Section contents
     */
    public static List<SectionContents> makeSectionContentsByInputStream(InputStream istream)
    {
        return makeSectionContentsByInputStream(istream, "UTF-8");
    }

    /**
     * セクション名を取得します.
     *
     * @return Section Name
     */
    public String getName()
    {
        return name;
    }

    /**
     * セクションコンテンツ（text...部分）を取得します.
     *
     * @return Contents
     */
    public String getContents()
    {
        return buffer.toString();
    }

    /**
     * InputStreamからセクションのリストを作成します.
     *
     * @param istream Data stream
     * @param charsetName Charset name
     * @return Content List
     */
    public static List<SectionContents> makeSectionContentsByInputStream(InputStream istream, String charsetName)
    {
        LinkedList<SectionContents> contentsList = new LinkedList<>();
        try
        {
            InputStreamReader reader = new InputStreamReader(istream, charsetName);
            new BufferedReader(reader).lines().forEach(s ->
                {
                    if (s.startsWith("["))
                    {
                        contentsList.add(new SectionContents(s));
                    }
                    else if (!contentsList.isEmpty())
                    {
                        SectionContents section = contentsList.getLast();
                        section.buffer.append(s).append('\n');
                    }
                });
        } catch (UnsupportedEncodingException e) {
            MMLUtil.MML_LOGGER.error(e);
        }

        return contentsList;
    }
}
