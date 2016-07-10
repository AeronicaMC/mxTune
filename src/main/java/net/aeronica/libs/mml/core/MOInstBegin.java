package net.aeronica.libs.mml.core;

public class MOInstBegin implements IMObjects
{
    static final Type type = IMObjects.Type.INST_BEGIN;

    @Override
    public Type getType() {return type;}

    long cumulativeTicks;

    public MOInstBegin() {}

    public String toString()
    {
        return new String("{\"" + type + "\": {\"MML@\"}}");
    }
}
