package net.aeronica.libs.mml.core;

public class MOTempo implements IMObjects
{
    static final Type type = IMObjects.Type.TEMPO;

    @Override
    public Type getType() {return type;}

    int tempo;
    long startingTicks;

    public MOTempo() {}

    public MOTempo(int tempo, long startingTicks)
    {
        this.tempo = tempo;
        this.startingTicks = startingTicks;
    }

    public int getTempo() {return tempo;}

    public void setTempo(int tempo) {this.tempo = tempo;}

    public long getTicksStart() {return startingTicks;}

    public void setTicksStart(long startingTicks) {this.startingTicks = startingTicks;}

    public String toString()
    {
        return new String("{\"" + type + "\": {\"tempo\": " + tempo + ", \"startingTicks\": " + startingTicks + "}}");
    }
}
