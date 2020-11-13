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
 * Remove dependency on 'import fourthline.mabiicco.midi.InstClass;'
 * Set trackLimit to 'MMLUtil MAX_TRACKS;'
 */

package net.aeronica.libs.mml.readers.mml3mle;


import net.aeronica.libs.mml.parser.MMLUtil;

/**
 * "[3MLE EXTENSION]" Track
 */
public final class Extension3mleTrack
{
    private int instrument;
    private int panpot;
    private int startMarker;
    private int trackCount;
    private int trackLimit;
    private int group;
    private String trackName;

    public Extension3mleTrack(int instrument, int group, int panpot, String trackName, int startMarker)
    {
        this.instrument = instrument;
        this.group = group;
        this.panpot = panpot;
        this.startMarker = startMarker;
        this.trackName = trackName;
        this.trackCount = 1;
        this.trackLimit = MMLUtil.MAX_TRACKS;
    }

    public boolean isLimit()
    {
        return (trackCount >= trackLimit);
    }

    public void addTrack()
    {
        trackCount++;
    }

    public int getInstrument()
    {
        return instrument;
    }

    public int getGroup()
    {
        return group;
    }

    public int getPanpot()
    {
        return panpot;
    }

    public int getStartMarker()
    {
        return startMarker;
    }

    public int getTrackCount()
    {
        return trackCount;
    }

    public String getTrackName()
    {
        return trackName;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        sb.append(group + " ");
        sb.append(instrument + " ");
        sb.append(panpot + " ");
        sb.append(trackCount + " ");
        sb.append(trackName + " ]");
        return sb.toString();
    }
}
