package net.aeronica.libs.mml.core;

public class MOInstEnd implements IMObjects
{
    static final Type type = IMObjects.Type.INST_END;

    @Override
    public Type getType() {return type;}

    long cumulativeTicks;

    public MOInstEnd() {}

    public MOInstEnd(long cumulativeTicks) {this.cumulativeTicks = cumulativeTicks;}

    public long getCumulativeTicks() {return cumulativeTicks;}

    public void setCumulativeTicks(long cumulativeTicks) {this.cumulativeTicks = cumulativeTicks;}

    public String toString()
    {
        return new String("{\"" + type + "\": {\"cumulativeTicks\": " + cumulativeTicks + "}}");
    }
}
