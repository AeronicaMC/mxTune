package net.aeronica.libs.mml.core;

public class MOPart implements IMObjects
{
    static final Type type = IMObjects.Type.PART;

    @Override
    public Type getType() {return type;}

    long cumulativeTicks;

    public MOPart() {}

    public MOPart(long cumulativeTicks) {this.cumulativeTicks = cumulativeTicks;}

    public long getCumulativeTicks() {return cumulativeTicks;}

    public void setCumulativeTicks(long cumulativeTicks) {this.cumulativeTicks = cumulativeTicks;}

    public String toString()
    {
        return new String("{\"" + type + "\": {\"cumulativeTicks\": " + cumulativeTicks + "}}");
    }
}
