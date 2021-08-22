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
 *
 * Remove dependencies on
 * 'import fourthline.mabiicco.midi.InstClass;'
 * 'import fourthline.mabiicco.midi.InstType;'
 * 'import fourthline.mabiicco.midi.MabiDLS;'
 * 'import fourthline.mmlTools.*;'
 *
 * Remove methods:
 * setStartPosition() // 再生開始位置を設定します. (Set the playback start position.)
 *
 * Disable Marker parsing.
 * Ignore Song and Chorus detection.
 * Ignore channel options.
 *
 * Use 'MMLUtil.MML_LOGGER'
 * Partial potential bug fixes for: "The value returned from a stream read should be checked"
 *     Collect and log only, but ideally asserts should raised.
 * Update to read data into mxTune MXT file classes
 */

package net.aeronica.libs.mml.readers.mml3mle;

import net.aeronica.libs.mml.parser.MMLUtil;
import net.aeronica.mods.mxtune.util.SoundFontProxyManager;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;


/**
 * "*.mml" (3mleさん) のファイルを扱います.
 */
public final class MMLFile
{
    private static final Logger LOGGER = LogManager.getLogger();
//    private final MXTuneFile mxTuneFile = new MXTuneFile();
    private String encoding = "Shift_JIS";

    // channel sections
    private LinkedList<String> mmlParts = new LinkedList<>();
    private List<Extension3mleTrack> trackList = null;

//    public static MXTuneFile parse(Path path)
//    {
//        try
//        {
//            return new MMLFile().parse(getFile(path));
//        } catch (MMLParseException e)
//        {
//            LOGGER.error(e);
//        }
//        return null;
//    }
//
//    public MXTuneFile parse(InputStream istream) throws MMLParseException
//    {
//        List<SectionContents> contentsList = SectionContents.makeSectionContentsByInputStream(istream, encoding);
//        if (contentsList.isEmpty())
//        {
//            throw (new MMLParseException("no contents"));
//        }
//        parseSection(contentsList);
//        if ((trackList == null) || trackList.isEmpty())
//        {
//            throw new MMLParseException("no track");
//        }
//        createTrack();
//        return mxTuneFile;
//    }
//
//    private static FileInputStream getFile(@Nullable Path path)
//    {
//        FileInputStream is = null;
//        if (path != null)
//        {
//            try
//            {
//                is = new FileInputStream(path.toFile());
//            } catch (FileNotFoundException e)
//            {
//                LOGGER.error(e.getLocalizedMessage());
//            }
//            return is;
//        }
//        else
//            LOGGER.error("Path is null in AbstractMmlFileReader#getFile");
//        return null;
//    }
//
    private static byte[] decode(String dSection, long c) throws MMLParseException
    {
        CRC32 crc = new CRC32();
        crc.update(dSection.getBytes());
        if (c != crc.getValue())
        {
            throw new MMLParseException("invalid c=" + c + " <> " + crc.getValue());
        }
        Decoder decoder = Base64.getDecoder();
        byte[] b = decoder.decode(dSection);

        int dataLength = ByteBuffer.wrap(b, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        byte[] data = new byte[dataLength];
        int readLength = 0;

        try
        {
            BZip2CompressorInputStream bz2istream = new BZip2CompressorInputStream(new ByteArrayInputStream(b, 12, b.length - 12));
            readLength = bz2istream.read(data);
            bz2istream.close();
        } catch (IOException e)
        {
            LOGGER.error(e);
        }

        LOGGER.debug("byte[] decode: dataLength {}, readLength {}", dataLength, readLength);
        int i = 0;
        String line;
        while (i < dataLength)
        {
            StringBuilder out = new StringBuilder();
            for (int k = 0; k < 25; k++)
            {
                if (i < dataLength)
                    out.append(String.format("%02x ", data[i++]));
                else break;
            }
            line = out.toString();
            LOGGER.debug(line);
        }
        return data;
    }

//    private void parseSection(List<SectionContents> contentsList) throws MMLParseException
//    {
//        for (SectionContents contents : contentsList)
//        {
//            if (contents.getName().equals("[3MLE EXTENSION]"))
//            {
//                trackList = parse3mleExtension(contents.getContents());
//            }
//            else if (contents.getName().matches("\\[Channel[0-9]*\\]"))
//            {
//                mmlParts.add(contents.getContents()
//                    .replaceAll("//.*\n", "\n")
//                    .replaceAll("/\\*/?([^/]|[^*]/)*\\*/", "")
//                    .replaceAll("[ \t\n]", ""));
//            }
//            else if (contents.getName().equals("[Settings]"))
//            {
//                parseSettings(contents.getContents());
//            }
//        }
//
//    }
//
//    private void createTrack()
//    {
//        for (Extension3mleTrack track : trackList)
//        {
//            int program = track.getInstrument() - 1; // 3MLEのInstruments番号は1がスタート.
//            LOGGER.debug("Program: {}", program);
//            String[] text = new String[MMLUtil.MAX_TRACKS];
//            List<MXTuneStaff> staves = new ArrayList<>();
//            for (int i = 0; i < track.getTrackCount(); i++)
//            {
//                text[i] = mmlParts.pop();
//                staves.add(new MXTuneStaff(i, text[i]));
//                LOGGER.debug("text[{}]= {}", i, text[i]);
//            }
//
//            String meta = String.format("%s, program %d", track.getTrackName(), program);
//            String soundProxyID = Map3MLEInstruments.getSoundFontProxyName(program);
//            int packedPreset = SoundFontProxyManager.getPackedPreset(soundProxyID);
//            MXTunePart mxTunePart = new MXTunePart(soundProxyID, meta, packedPreset, staves);
//            mxTuneFile.getParts().add(mxTunePart);
//        }
//    }
//
//    /**
//     * parse [Settings] contents
//     *
//     * @param contents
//     */
//    private void parseSettings(String contents)
//    {
//        TextParser.text(contents)
//                .pattern("Title=", mxTuneFile::setTitle)
//                .pattern("Source=", mxTuneFile::setSource)
//                .pattern("Encoding=", t -> this.encoding = t)
//                .parse();
//    }

    /**
     * [3MLE EXTENSION] をパースし, トラック構成情報を取得します.
     *
     * @param str [3MLE EXTENSION] セクションのコンテンツ
     * @return トラック構成情報
     */
    private List<Extension3mleTrack> parse3mleExtension(String str) throws MMLParseException
    {
        StringBuilder sb = new StringBuilder();
        long c = 0;
        for (String s : str.split("\n"))
        {
            if (s.startsWith("d="))
            {
                sb.append(s.substring(2));
            }
            else if (s.startsWith("c="))
            {
                c = Long.parseLong(s.substring(2));
            }
        }

        byte[] data = decode(sb.toString(), c);
        return parseData(data);
    }

    /**
     * @param data decompress済みのバイト列
     * @return トラック構成情報
     */
    private List<Extension3mleTrack> parseData(byte[] data)
    {
        LinkedList<Extension3mleTrack> extension3mleTracks = new LinkedList<>();
        extension3mleTracks.add(new Extension3mleTrack(-1, -1, -1, null, 0)); // dummy

        ByteArrayInputStream istream = new ByteArrayInputStream(data);
        int b = 0;
        int hb = 0;
        while ((b = istream.read()) != -1)
        {
            if ((hb == 0x12) && (b == 0x10))
            {
                parseHeader(istream);
            }
            else if ((hb == 0x02) && (b == 0x1c))
            {
                parseTrack(extension3mleTracks, istream);
            }
            else if ((hb == 0x09) && ((b > 0x00) && (b < 0x20)))
            {
                parseMarker(istream);
            }

            hb = b;
        }

        extension3mleTracks.removeFirst();
        return extension3mleTracks;
    }

    private void parseHeader(ByteArrayInputStream istream)
    {
        long skipped = istream.skip(37);
        int len = readLEIntValue(istream);
        long lenSkipped = istream.skip(len);
        LOGGER.debug("parseHeader: skipping 37 bytes. Actual={}, OK={}. Skipping {} bytes to track data.", skipped, 37 == skipped, len);
        LOGGER.debug("parseHeader: skipping {} bytes. Actual={}, OK={}", len, lenSkipped, len == lenSkipped);
    }

    private void parseTrack(LinkedList<Extension3mleTrack> trackList, ByteArrayInputStream istream)
    {
        // parse Track
        long skips = istream.skip(3);
        int trackNo = istream.read();
        skips += istream.skip(1); // volumn
        int panpot = istream.read();
        skips += istream.skip(5);
        int startMarker = istream.read();
        skips += istream.skip(7);
        int instrument = istream.read();
        skips += istream.skip(3);
        int group = istream.read();
        skips += istream.skip(13);
        String trackName = readString(istream);
        LOGGER.debug("parseTrack: skips expected 32, skips actual {}, OK={}", skips, 32 == skips);
        LOGGER.debug("{} {} {}", trackNo, instrument, trackName);

        Extension3mleTrack lastTrack = trackList.getLast();
        if ((lastTrack.getGroup() != group) || (lastTrack.getInstrument() != instrument) || (lastTrack.getPanpot() != panpot) || (lastTrack.isLimit()))
        {
            // new Track
            trackList.add(new Extension3mleTrack(instrument, group, panpot, trackName, startMarker));
        }
        else
        {
            lastTrack.addTrack();
        }
    }

    private int readLEIntValue(InputStream istream)
    {
        byte[] b = new byte[4];
        try
        {
            istream.read(b);
        } catch (IOException e)
        {
            LOGGER.error(e);
        }
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private void parseMarker(ByteArrayInputStream istream)
    {
        // parse Marker
        long skips = istream.skip(7);
        int tickOffset = readLEIntValue(istream);
        skips += istream.skip(4);
        String name = readString(istream);
        LOGGER.debug("parseMarker: skips expected 11, skips actual {}, OK={}", skips, 11 == skips);
        LOGGER.debug("Marker {}={}", name, tickOffset);
    }

    private String readString(InputStream istream)
    {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        int b;
        try
        {
            while ((b = istream.read()) != 0)
            {
                ostream.write(b);
            }
            return new String(ostream.toByteArray(), encoding);
        } catch (IOException e)
        {
            LOGGER.error(e);
        }
        return "";
    }

    public static void main(String[] args)
    {
        try
        {
            String str = "c=3902331007\nd=4wAAAJvYl0oBAAAAQlpoOTFBWSZTWReDTXYAAEH/i/7U0AQCAHgAQAAEAGwIEABAAECAAAoABKAAcivUCaZGmRiAyNqDEgnqRpkPTUZGh5S6QfOGHRg+AfSJE3ebNDxInstECT3owI1yYiuIY5IwTCLAQz1oZyAogJFOhVYmv39cWsLxsbh0MkELhClECHm5wCBjLYz8XckU4UJAXg012A==";

            MMLFile mmlFile = new MMLFile();
            List<Extension3mleTrack> trackList = mmlFile.parse3mleExtension(str);
            for (Extension3mleTrack track : trackList)
            {
                LOGGER.debug(track);
            }
        } catch (MMLParseException e)
        {
            LOGGER.error(e);
        }
    }
}
