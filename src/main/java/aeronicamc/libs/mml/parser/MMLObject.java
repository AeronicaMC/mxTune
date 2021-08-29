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
package aeronicamc.libs.mml.parser;

/*
 * MML Object builder class
 */
public class MMLObject
{

    private final Type type;
    private final long longestPartTicks;
    private final int instrument;
    private final long startingTicks;
    private final long cumulativeTicks;
    private final String text;
    private final int midiNote;
    private final long lengthTicks;
    private final int volume;
    private final int tempo;
    private final int minVolume;
    private final int maxVolume;
    private final boolean sustain;
    private final boolean tied;
    private boolean doNoteOn = true;
    private boolean doNoteOff = true;
    
    public MMLObject(Builder builder)
    {
        this.type = builder.type;
        this.longestPartTicks = builder.longestPartTicks;
        this.instrument = builder.instrument;
        this.startingTicks = builder.startingTicks;
        this.cumulativeTicks = builder.cumulativeTicks;
        this.text = builder.text;
        this.midiNote = builder.midiNote;
        this.lengthTicks = builder.lengthTicks;
        this.volume = builder.volume;
        this.tempo = builder.tempo;
        this.minVolume = builder.minVolume;
        this.maxVolume = builder.maxVolume;
        this.sustain = builder.sustain;
        this.tied = builder.tied;
    }

    public Type getType() {return type;}
    public long getlongestPartTicks() {return longestPartTicks;}
    public int getInstrument() {return instrument;}
    public long getStartingTicks() {return startingTicks;}
    public long getCumulativeTicks() {return cumulativeTicks;}
    public int getMidiNote() {return midiNote;}
    public int getNoteVolume() {return volume;}
    public String getText() {return text;}
    public long getLengthTicks() {return lengthTicks;}
    public int getTempo() {return tempo;}
    public int getMinVolume() {return minVolume;}
    public int getMaxVolume() {return maxVolume;}
    public boolean isTied() {return tied;}

    public void setDoNoteOn(boolean state) {doNoteOn = state;}
    public void setDoNoteOff(boolean state) {doNoteOff = state;}
    public boolean doNoteOn() {return doNoteOn;}
    public boolean doNoteOff() {return doNoteOff;}
    public boolean doSustain() {return  sustain;}
    
    public static class Builder
    {
        private final Type type;
        private long longestPartTicks;
        private int instrument;
        private long startingTicks;
        private long cumulativeTicks;
        private String text;
        private int midiNote;
        private long lengthTicks;
        private int volume;
        private int tempo;
        private int minVolume;
        private int maxVolume;
        private boolean sustain;
        private boolean tied;
        
        public Builder(Type type)
        {
            this.type = type;
        }        
        public Builder longestPartTicks(long longestPartTicks)
        {
            this.longestPartTicks = longestPartTicks;
            return this;
        }
        public Builder instrument(int instrument)
        {
            this.instrument = instrument;
            return this;
        }        
        public Builder startingTicks(long startingTicks)
        {
            this.startingTicks = startingTicks;
            return this;
        }
        public Builder cumulativeTicks(long cumulativeTicks)
        {
            this.cumulativeTicks = cumulativeTicks;
            return this;
        }
        public Builder text(String text)
        {
            this.text = text;
            return this;
        }
        public Builder midiNote(int midiNote)
        {
            this.midiNote = midiNote;
            return this;
        }        
        public Builder lengthTicks(long lengthTicks)
        {
            this.lengthTicks = lengthTicks;
            return this;
        }
        public Builder volume(int volume)
        {
            this.volume = volume;
            return this;
        }
        public Builder tempo(int tempo)
        {
            this.tempo = tempo;
            return this;
        }
        public Builder minVolume(int minVolume)
        {
            this.minVolume = minVolume;
            return this;
        }
        public Builder maxVolume(int maxVolume)
        {
            this.maxVolume = maxVolume;
            return this;
        }
        public Builder sustain(boolean sustain)
        {
            this.sustain = sustain;
            return this;
        }
        public Builder tied(boolean tied)
        {
            this.tied = tied;
            return this;
        }
        public MMLObject build() {
            MMLObject mmlObj = new MMLObject(this);
            validateMMLObject(mmlObj);
            return mmlObj;
        }
        public void validateMMLObject(MMLObject mmlObj)
        {
            // basic validations
        }
    }
    public enum Type {INIT, TEMPO, SUSTAIN, INST, PART, NOTE, REST, STOP, DONE }
}
