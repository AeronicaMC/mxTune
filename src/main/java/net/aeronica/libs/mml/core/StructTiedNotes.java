package net.aeronica.libs.mml.core;

public class StructTiedNotes
{
    int midiNote;
    long startingTicks;
    long lengthTicks;
    int volume;

    public String toString()
    {
        return "midi:" + midiNote + ", startingTicks:" + startingTicks + ", lengthTicks:" + lengthTicks + ", volume:" + volume + "\n";
    }
}
