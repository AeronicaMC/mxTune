package net.aeronica.libs.mml.core;

public class StatePart
{
    private int volume;
    private boolean volumeArcheAge;
    private int octave;
    private int mmlLength;
    private boolean dotted;
    private long runningTicks;
    private boolean tied;

    StatePart() {this.init();}

    public void init()
    {
        volume = 8;
        volumeArcheAge = false;
        octave = 4;
        mmlLength = 4;
        dotted = false;
        runningTicks = 0;
        tied = false;
    }

    @Override
    public String toString()
    {
        return "@PartState: oct=" + octave + ", vol=" + volume + ", mmlLength=" + mmlLength + " ,runningTicks=" + runningTicks + ", tied=" + tied;
    }

    public int getVolume()
    {
        if (this.volumeArcheAge)
            return volume;
        else
            return getMinMax(0, 127, volume * 128 / 15);
    }

    public void setVolume(int volume)
    {
        this.volume = getMinMax(0, 127, volume);
        if (this.volume > 15)
            volumeArcheAge = true;
    }

    public int getOctave() {return octave;}

    public void setOctave(int octave) {this.octave = getMinMax(1, 8, octave);}

    /**
     * You can <<<< an octave to 0, but you can't
     * set octave to 0 via the octave command: o0
     */
    public void downOctave() {this.octave = getMinMax(0, 8, this.octave - 1);}

    public void upOctave() {this.octave = getMinMax(1, 8, this.octave + 1);}

    public int getMMLLength() {return mmlLength;}

    public boolean isDotted() {return dotted;}

    public void setMMLLength(int mmlLength, boolean dotted)
    {
        this.mmlLength = getMinMax(1, 64, mmlLength);
        this.dotted = dotted;
    }

    public void accumulateTicks(long n) {this.runningTicks = this.runningTicks + n;}

    public long getRunningTicks() {return runningTicks;}

    public boolean isTied() {return tied;}

    public void setTied(boolean tied) {this.tied = tied;}

    private int getMinMax(int min, int max, int value) {return (int) Math.max(Math.min(max, value), min);}
}
