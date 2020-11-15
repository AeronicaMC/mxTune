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

import static net.aeronica.libs.mml.parser.ElementTypes.MML_SHARP;

public class TempState
{
    private int pitch;
    private int accidental;
    private int duration;
    private boolean dotted;

    public TempState()
    {
        init();
    }

    @Override
    public String toString()
    {
        return "@tempState: base pitch=" + pitch + ", acc=" + accidental + ", final pitch=" + (pitch + accidental)
                + ", mLen=" + duration + ", dot=" + dotted;
    }

    void init()
    {
        pitch = -1;
        accidental = 0;
        duration = -1;
        dotted = false;
    }

    public int getPitch()
    {
        return pitch + accidental;
    }

    public void setPitch(int pitch)
    {
        this.pitch = pitch;
    }

    public int getAccidental()
    {
        return accidental;
    }

    public void setAccidental(int accidental)
    {
        this.accidental = accidental == MML_SHARP ? 1 : -1;
    }

    public int getDuration()
    {
        return duration;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public boolean isDotted()
    {
        return dotted;
    }

    public void setDotted(boolean dotted)
    {
        this.dotted = dotted;
    }
}
