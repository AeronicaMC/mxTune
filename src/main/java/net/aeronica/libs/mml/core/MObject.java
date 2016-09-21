package net.aeronica.libs.mml.core;

/*
 * MML Object builder class
 * This is an attempt to use the Builder Pattern to simplify the collection of MML objects during
 * the MML parse and eliminating all the individual object classes and the ugliness of using
 * non-type-safe dereferencing in the concrete converter class. e.g. MMLToMIDI.
 */
public class MObject
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
    
    public MObject(MObjectBuilder builder)
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
    
    public static class MObjectBuilder
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
        
        public MObjectBuilder(Type type)
        {
            this.type = type;
        }        
        public MObjectBuilder longestPartTicks(long longestPartTicks)
        {
            this.longestPartTicks = longestPartTicks;
            return this;
        }
        public MObjectBuilder instrument(int instrument)
        {
            this.instrument = instrument;
            return this;
        }        
        public MObjectBuilder startingTicks(long startingTicks)
        {
            this.startingTicks = startingTicks;
            return this;
        }
        public MObjectBuilder cumulativeTicks(long cumulativeTicks)
        {
            this.cumulativeTicks = cumulativeTicks;
            return this;
        }
        public MObjectBuilder text(String text)
        {
            this.text = text;
            return this;
        }
        public MObjectBuilder midiNote(int midiNote)
        {
            this.midiNote = midiNote;
            return this;
        }        
        public MObjectBuilder lengthTicks(long lengthTicks)
        {
            this.lengthTicks = lengthTicks;
            return this;
        }
        public MObjectBuilder volume(int volume)
        {
            this.volume = volume;
            return this;
        }
        public MObjectBuilder tempo(int tempo)
        {
            this.tempo = tempo;
            return this;
        }
       
        public MObject build() {
            MObject mObject = new MObject(this);
            validateMObject(mObject);
            return mObject;
        }
        public void validateMObject(MObject mObject)
        {
            // TODO: basic validations
        }
    }
    
    public static enum Type { INST_BEGIN, TEMPO, INST, PART, NOTE, REST, INST_END, DONE };
    
}
