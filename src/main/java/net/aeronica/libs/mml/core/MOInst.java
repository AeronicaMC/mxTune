package net.aeronica.libs.mml.core;

public class MOInst implements IMObjects
{
    static final Type type = IMObjects.Type.INST;

    @Override
    public Type getType() {return type;}

    int instrument;
    long startingTicks;

    public MOInst() {}

    public MOInst(int instrument, long startingTicks)
    {
        this.instrument = instrument;
        this.startingTicks = startingTicks;
    }

    public int getInstrument() {return instrument;}

    public void setInstrument(int instrument) {this.instrument = instrument;}

    public long getTicksStart() {return startingTicks;}

    public void setTicksStart(long startingTicks) {this.startingTicks = startingTicks;}

    public String toString()
    {
        return new String("{\"" + type + "\": {\"instrument\": " + instrument + ", \"startingTicks\": " + startingTicks + "}}");
    }
}
