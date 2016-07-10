package net.aeronica.libs.mml.core;

/** TODO: Find a different way to stuff these into a list collection */
public interface IMObjects
{
    public static enum Type { INST_BEGIN, TEMPO, INST, PART, NOTE, REST, INST_END, DONE };

    public Type getType();
}