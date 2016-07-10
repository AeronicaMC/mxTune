package net.aeronica.libs.mml.core;

public class MONote implements IMObjects
{
    static final Type type = IMObjects.Type.NOTE;

    @Override
    public Type getType() {return type;}

    String text;
    int midiNote;
    long startingTicks;
    long lengthTicks;
    int volume;

    public MONote() {}

    public MONote(int midiNote, long startingTicks, long lengthTicks, int volume)
    {
        this.midiNote = midiNote;
        this.startingTicks = startingTicks;
        this.lengthTicks = lengthTicks;
        this.volume = volume;
        this.text = null;
    }

    public int getMidiNote() {return midiNote;}

    public void setMidiNote(int midiNote) {this.midiNote = midiNote;}

    public long getStartingTicks() {return startingTicks;}

    public void setStartingTicks(long startingTicks) {this.startingTicks = startingTicks;}

    public long getLengthTicks() {return lengthTicks;}

    public void setLengthTicks(long lengthTicks) {this.lengthTicks = lengthTicks;}

    public int getNoteVolume() {return volume;}

    public void setNoteVolume(int noteVolume) {this.volume = noteVolume;}

    public String getText() {return text;}

    public void setText(String text) {this.text = text;}

    public String toString()
    {
        return new String("{\"" + type + "\": {\"midiNote\": " + midiNote + ", \"startingTicks\": " + startingTicks + ", \"lengthTicks\": " + lengthTicks + ", \"volume\": " + volume + "}}");
    }
}
