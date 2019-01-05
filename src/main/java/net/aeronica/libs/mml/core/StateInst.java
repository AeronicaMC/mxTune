package net.aeronica.libs.mml.core;

public class StateInst
{
    private int tempo;
    private int instrument;
    private long longestPart;
    private int minVolume = 127;
    private int maxVolume = 0;
    private boolean volumeArcheAge = false;

    StateInst()
    {
        this.init();
        this.longestPart = 0;
    }

    public void init()
    {
        tempo = 120;
        instrument = 0;
    }

    public void setTempo(int tempo)
    {
        // tempo 32-255, anything outside the range resets to 120
        if (tempo < 32 || tempo > 255)
        {
            this.tempo = 120;
        } else
        {
            this.tempo = tempo;
        }
    }

    public int getTempo() {return tempo;}

    public int getInstrument() {return instrument;}

    public void setInstrument(int gmInstrument)
    {
        // Packed preset number 0-16,511 - bank 0-128, preset 0-127, zero based
        this.instrument = (getMinMax(0, 0x407F, gmInstrument));
    }

    void collectDurationTicks(long durationTicks)
    {
        if (durationTicks > this.longestPart) this.longestPart = durationTicks;
    }

    long getLongestDurationTicks() {return this.longestPart;}

    @Override
    public String toString()
    {
        return "\n@CommonState: tempo=" + tempo + ", instrument=" + instrument;
    }

    private int getMinMax(int min, int max, int value) {return Math.max(Math.min(max, value), min);}

    void collectVolume(int volumeIn)
    {
        int volume = getMinMax(0, 127, volumeIn);
        if (volume > 15)
            volumeArcheAge = true;
        this.minVolume = Math.min(this.minVolume, volume);
        this.maxVolume = Math.max(this.maxVolume, volume);
    }

    int getMinVolume()
    {
        return volumeArcheAge ? minVolume : minVolume * 127 / 15;
    }

    int getMaxVolume()
    {
        return volumeArcheAge ? maxVolume : maxVolume * 127 / 15;
    }
}
