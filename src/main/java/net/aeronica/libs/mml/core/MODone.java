package net.aeronica.libs.mml.core;

public class MODone implements IMObjects
{
    static final Type type = IMObjects.Type.DONE;

    @Override
    public Type getType() {return type;}

    long longestPartTicks;

    public MODone() {}

    public MODone(long longestPartTicks) {this.longestPartTicks = longestPartTicks;}

    public long getlongestPartTicks() {return longestPartTicks;}

    public void setlongestPartTicks(long longestPartTicks) {this.longestPartTicks = longestPartTicks;}

    public String toString() {return new String("{\"" + type + "\": {\"longestPartTicks\": " + longestPartTicks + "}}\n");}
}
