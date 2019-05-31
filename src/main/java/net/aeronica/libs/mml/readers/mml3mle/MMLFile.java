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
 *
 * Update to read data into mxTune MXT file classes
 */

package net.aeronica.libs.mml.readers.mml3mle;

import net.aeronica.libs.mml.core.MMLUtil;
import net.aeronica.mods.mxtune.mxt.MXTuneFile;
import net.aeronica.mods.mxtune.mxt.MXTunePart;
import net.aeronica.mods.mxtune.mxt.MXTuneStaff;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.client.resources.I18n;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

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
    private final MXTuneFile mxTuneFile = new MXTuneFile();
    private String encoding = "Shift_JIS";

    // channel sections
    private LinkedList<String> mmlParts = new LinkedList<>();
    private List<Extension3mleTrack> trackList = null;

    public static MXTuneFile parse(Path path)
    {
        try
        {
            return new MMLFile().parse(getFile(path));
        } catch (MMLParseException e)
        {
            MMLUtil.MML_LOGGER.error(e);
        }
        return null;
    }

    public MXTuneFile parse(InputStream istream) throws MMLParseException
    {
        List<SectionContents> contentsList = SectionContents.makeSectionContentsByInputStream(istream, encoding);
        if (contentsList.isEmpty())
        {
            throw (new MMLParseException("no contents"));
        }
        parseSection(contentsList);
        if ((trackList == null) || (trackList.size() == 0))
        {
            throw new MMLParseException("no track");
        }
        createTrack();
        return mxTuneFile;
    }

    private static FileInputStream getFile(@Nullable Path path)
    {
        FileInputStream is = null;
        if (path != null)
        {
            try
            {
                is = new FileInputStream(path.toFile());
            } catch (FileNotFoundException e)
            {
                MMLUtil.MML_LOGGER.error(e.getLocalizedMessage());
            }
            return is;
        }
        else
            MMLUtil.MML_LOGGER.error("Path is null in AbstractMmlFileReader#getFile");
        return null;
    }

    private static byte[] decode(String dSection, long c) throws MMLParseException
    {
        CRC32 crc = new CRC32();
        crc.update(dSection.getBytes());
        if (c != crc.getValue())
        {
            throw new MMLParseException("invalid c=" + c + " <> " + crc.getValue());
        }
        Decoder decoder = Base64.getDecoder();
        byte b[] = decoder.decode(dSection);

        int dataLength = ByteBuffer.wrap(b, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        byte data[] = new byte[dataLength];

        try
        {
            BZip2CompressorInputStream bz2istream = new BZip2CompressorInputStream(new ByteArrayInputStream(b, 12, b.length - 12));
            bz2istream.read(data);
            bz2istream.close();
        } catch (IOException e)
        {
            MMLUtil.MML_LOGGER.error(e);
        }

        for (int i = 0; i < dataLength; i++)
        {
            System.out.printf("%02x ", data[i]);
        }
        System.out.println();
        return data;
    }

    private void parseSection(List<SectionContents> contentsList) throws MMLParseException
    {
        for (SectionContents contents : contentsList)
        {
            if (contents.getName().equals("[3MLE EXTENSION]"))
            {
                trackList = parse3mleExtension(contents.getContents());
            }
            else if (contents.getName().matches("\\[Channel[0-9]*\\]"))
            {
                mmlParts.add(contents.getContents()
                    .replaceAll("//.*\n", "\n")
                    .replaceAll("/\\*/?([^/]|[^*]/)*\\*/", "")
                    .replaceAll("[ \t\n]", ""));
            }
            else if (contents.getName().equals("[Settings]"))
            {
                parseSettings(contents.getContents());
            }
        }

    }

    private void createTrack()
    {
        for (Extension3mleTrack track : trackList)
        {
            int program = track.getInstrument() - 1; // 3MLEのInstruments番号は1がスタート.
            String text[] = new String[]{"", "", ""};
            List<MXTuneStaff> staves = new ArrayList<>();
            for (int i = 0; i < track.getTrackCount(); i++)
            {
                text[i] = mmlParts.pop();
                staves.add(new MXTuneStaff(i, text[i]));
                MMLUtil.MML_LOGGER.info("text[{}]= {}", i, text[i]);
            }
            int packedPreset = MMLUtil.preset2PackedPreset(12, program);
            MXTunePart mxTunePart = new MXTunePart(I18n.format(MIDISystemUtil.getPatchNameKey(MMLUtil.packedPreset2Patch(packedPreset))), track.getTrackName(), packedPreset, staves);
            mxTuneFile.getParts().add(mxTunePart);
        }
    }

    /**
     * parse [Settings] contents
     *
     * @param contents
     */
    private void parseSettings(String contents)
    {
        TextParser.text(contents)
                .pattern("Title=", t -> mxTuneFile.setTitle(t))
                .pattern("Source=", t -> mxTuneFile.setSource(t))
                .pattern("Encoding=", t -> this.encoding = t)
                .parse();
    }

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

        byte data[] = decode(sb.toString(), c);
        return parseData(data);
    }

    /**
     * @param data decompress済みのバイト列
     * @return トラック構成情報
     */
    private List<Extension3mleTrack> parseData(byte data[])
    {
        LinkedList<Extension3mleTrack> trackList = new LinkedList<>();
        trackList.add(new Extension3mleTrack(-1, -1, -1, null, 0)); // dummy

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
                parseTrack(trackList, istream);
            }
            else if ((hb == 0x09) && ((b > 0x00) && (b < 0x20)))
            {
                parseMarker(istream);
            }

            hb = b;
        }

        trackList.removeFirst();
        return trackList;
    }

    private void parseHeader(ByteArrayInputStream istream)
    {
        istream.skip(37);
        int len = readLEIntValue(istream);
        istream.skip(len);
    }

    private void parseTrack(LinkedList<Extension3mleTrack> trackList, ByteArrayInputStream istream)
    {
        // parse Track
        istream.skip(3);
        int trackNo = istream.read();
        istream.skip(1); // volumn
        int panpot = istream.read();
        istream.skip(5);
        int startMarker = istream.read();
        istream.skip(7);
        int instrument = istream.read();
        istream.skip(3);
        int group = istream.read();
        istream.skip(13);
        String trackName = readString(istream);
        MMLUtil.MML_LOGGER.info(trackNo + " " + instrument + " " + trackName);

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
        byte b[] = new byte[4];
        try
        {
            istream.read(b);
        } catch (IOException e)
        {
            MMLUtil.MML_LOGGER.error(e);
        }
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private void parseMarker(ByteArrayInputStream istream)
    {
//		List<Marker> markerList = mxTuneFile.getMarkerList();

        // parse Marker
        istream.skip(7);
        int tickOffset = readLEIntValue(istream);
        istream.skip(4);
        String name = readString(istream);
        MMLUtil.MML_LOGGER.info("Marker {}={}", name, tickOffset);
//		if (markerList != null) {
//			markerList.add(new Marker(name, tickOffset));
//		}
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
            MMLUtil.MML_LOGGER.error(e);
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
                MMLUtil.MML_LOGGER.info(track);
            }
        } catch (MMLParseException e)
        {
            MMLUtil.MML_LOGGER.error(e);
        }
    }
}
