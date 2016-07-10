package net.aeronica.libs.mml.core;

public class MORest implements IMObjects
{
    static final Type type = IMObjects.Type.REST;

    @Override
    public Type getType() {return type;}

    long startingTicks;
    long lengthTicks;

    public MORest() {}

    public MORest(long ticksStart, long lengthTicks)
    {
        this.startingTicks = ticksStart;
        this.lengthTicks = lengthTicks;
    }

    public long getTicksStart() {return startingTicks;}

    public void setTicksStart(long ticksStart) {this.startingTicks = ticksStart;}

    public long getTicksDuration() {return lengthTicks;}

    public void setTicksDuration(long lengthTicks) {this.lengthTicks = lengthTicks;}

    public String toString()
    {
        return new String("{\"" + type + "\": {\"ticksStart\": " + startingTicks + " ,\"lengthTicks\": " + lengthTicks + "}}");
    }
}
