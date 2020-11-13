/*
 * MIT License
 *
 * Copyright (c) 2020 Paul Boese a.k.a. Aeronica
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.aeronica.libs.mml.parser;


import static net.aeronica.libs.mml.parser.MMLUtil.clamp;

public class InstState
{
    private int tempo;
    private int instrument;
    private long longestPart;
    private int minVolume = 127;
    private int maxVolume = 0;
    private boolean volumeArcheAge = false;
    private int perform = 0;
    private int sustain = 0;

    InstState()
    {
        this.init();
        this.longestPart = 0;
    }

    public void init()
    {
        tempo = 120;
        instrument = 0;
    }

    public int getTempo() {return tempo;}

    public void setTempo(int tempo)
    {
        // tempo 32-255, anything outside the range resets to 120
        if (tempo < 32 || tempo > 255)
        {
            this.tempo = 120;
        }
        else
        {
            this.tempo = tempo;
        }
    }

    public int getInstrument() {return instrument;}

    public void setInstrument(int preset)
    {
        this.instrument = (clamp(0, 99999, preset));
    }

    public int getPerform()
    {
        return perform;
    }

    public void setPerform(int perform)
    {
        this.perform = perform;
    }

    public int getSustain()
    {
        return sustain;
    }

    public void setSustain(int sustain)
    {
        this.sustain = sustain;
    }

    void collectDurationTicks(long durationTicks)
    {
        if (durationTicks > this.longestPart) this.longestPart = durationTicks;
    }

    long getLongestDurationTicks() {return this.longestPart;}

    @Override
    public String toString()
    {
        return "@InstState: tempo=" + tempo + ", instrument=" + instrument + ", LongestDurationTicks= " + longestPart;
    }

    void collectVolume(int volumeIn)
    {
        int volume = clamp(0, 127, volumeIn);
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
