package net.aeronica.libs.mml.parser;

import static net.aeronica.libs.mml.parser.ElementTypes.MML_SHARP;

public class TempState
{
    private int pitch;
    private int accidental;
    private int duration;
    private boolean dotted;

    public TempState()
    {
        init();
    }

    @Override
    public String toString()
    {
        return "@tempState: base pitch=" + pitch + ", acc=" + accidental + ", final pitch=" + (pitch + accidental)
                + ", mLen=" + duration + ", dot=" + dotted;
    }

    void init()
    {
        pitch = -1;
        accidental = 0;
        duration = -1;
        dotted = false;
    }

    public int getPitch()
    {
        return pitch + accidental;
    }

    public void setPitch(int pitch)
    {
        this.pitch = pitch;
    }

    public int getAccidental()
    {
        return accidental;
    }

    public void setAccidental(int accidental)
    {
        this.accidental = accidental == MML_SHARP ? 1 : -1;
    }

    public int getDuration()
    {
        return duration;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public boolean isDotted()
    {
        return dotted;
    }

    public void setDotted(boolean dotted)
    {
        this.dotted = dotted;
    }
}
