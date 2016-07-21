package net.aeronica.libs.mml.core;

import java.nio.ByteBuffer;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class MMLToMIDI extends MMLTransformBase
{
    final int MASTER_TEMPO = 120;
    final double PPQ = 480.0;
    private final int TICKS_OFFSET = 75;
    private Sequence sequence;
    private float fakeVolume = 1F;

    public MMLToMIDI(float fakeVolume) { super(fakeVolume); this.fakeVolume = fakeVolume; }

    @Override
    public long durationTicks(int mmlNoteLength, boolean dottedLEN)
    {
        double dot = dottedLEN ? 15.0d : 10.0d;
        return (long) (((4.0d / (double) mmlNoteLength) * ((double) dot) / 10.0d) * (double) PPQ);
    }

    public Sequence getSequence() {return sequence;}

    @SuppressWarnings("unused")
    @Override
    public void processMObjects(List<IMObjects> mmlObjects)
    {
        // IMObjects.Type type;
        int ch = 0;
        int tk = 0;
        long ticksOffset = TICKS_OFFSET;
        int currentTempo = MASTER_TEMPO;

        try
        {
            sequence = new Sequence(Sequence.PPQ, (int) PPQ);
            for (int i = 0; i < 24; i++)
            {
                sequence.createTrack().add(createMsg(192, 0, 0, 100, 0l));
            }
            Track[] tracks = sequence.getTracks();
            // tracks[0].add(createTempoMetaEvent(currentTempo, ticksOffset));
            // tracks[0].add(createTempoMetaEvent(currentTempo, 0));
            tk++; // Track 0 for meta messages;

            for (int i = 0; i < mmlObjects.size(); i++)
            {
                /** ref: enum Type {INST_BEGIN, TEMPO, INST, PART, NOTE, REST, INST_END, DONE}; */
                switch (getMObject(i).getType())
                {
                case INST_BEGIN:
                {
                    MOInstBegin mmo = (MOInstBegin) getMObject(i);
                    /** Nothing to do in reality **/
                    break;
                }
                case TEMPO:
                {
                    MOTempo mmo = (MOTempo) getMObject(i);
                    currentTempo = mmo.getTempo();
                    tracks[0].add(createTempoMetaEvent(currentTempo, mmo.getTicksStart() + ticksOffset));
                    break;
                }
                case INST:
                {
                    MOInst mmo = (MOInst) getMObject(i);
                    tracks[tk].add(createProgramChangeEvent(ch, mmo.getInstrument(), mmo.getTicksStart() + ticksOffset));
                    break;
                }
                case PART:
                {
                    MOPart mmo = (MOPart) getMObject(i);
                    tk++;
                    if (tk > 23) tk = 23;
                    break;
                }
                case NOTE:
                {
                    MONote mmo = (MONote) getMObject(i);
                    tracks[tk].add(createNoteOnEvent(ch, mmo.getMidiNote(), (int) (scaleVolume(mmo.getNoteVolume()) * 127f / 15f), mmo.getStartingTicks() + ticksOffset));
                    tracks[tk].add(createNoteOffEvent(ch, mmo.getMidiNote(), (int) (mmo.getNoteVolume() * 127f / 15f), mmo.getStartingTicks() + mmo.getLengthTicks() + ticksOffset - 1));
                    if (mmo.getText() != null)
                    {
                        String text = new String("{\"Note\": \"{Track\":" + tk + ", \"Text\":\"" + mmo.getText() + "\"}}");
                        tracks[0].add(createTextMetaEvent(text, mmo.getStartingTicks() + ticksOffset));
                    }
                    break;
                }
                case REST:
                {
                    MORest mmo = (MORest) getMObject(i);
                    /** Nothing to do in reality **/
                    break;
                }
                case INST_END:
                {
                    MOInstEnd mmo = (MOInstEnd) getMObject(i);
                    tk++;
                    if (tk > 23) tk = 23;
                    ch++;
                    if (ch > 15) ch = 15;
                    tracks[tk].add(createMsg(192, ch, 0, 100, 0l));
                    break;
                }
                case DONE:
                {
                    MODone mmo = (MODone) getMObject(i);
                    /**
                     * Create a fake note to extend the play time so decaying
                     * audio does not cutoff suddenly
                     **/
                    tracks[0].add(createNoteOnEvent(ch, 1, 0, mmo.getlongestPartTicks() + ticksOffset + 480));
                    tracks[0].add(createNoteOffEvent(ch, 1, 0, mmo.getlongestPartTicks() + ticksOffset + 480 + 480));
                    break;
                }
                default:
                    System.err.println("MMLToMIDI#processMObjects Impossible?! An undefined enum?");
                }
            }
        } catch (Exception ex)
        {
            System.out.println("MMLToMIDI#processMObjects failed: " + ex);
        }
    }

    private int scaleVolume(int volumeIn)
    {
        return (int) Math.round((float)volumeIn * (Math.exp(this.fakeVolume)-1)/(Math.E-1));
    }
    
    protected MidiEvent createMsg(int mComd, int mChan, int mDat1, int mDat2, long mTime)
    {
        /**
         * NoteOn = 144, Channel, Key, Velocity, Time-stamp NoteOn = 128,
         * Channel, Key, Velocity, Time-stamp Patch = 192, Channel, Patch, 0,
         * Time-stamp
         */
        ShortMessage smLocal = new ShortMessage();
        MidiEvent mEve = new MidiEvent(smLocal, 1);
        try
        {
            smLocal = new ShortMessage(mComd, mChan, mDat1, mDat2);
            mEve = new MidiEvent(smLocal, mTime);
        } catch (Exception ex)
        {
            System.out.println("MMLToMIDI#createMsg failed: " + ex);
        }
        return mEve;
    }

    protected MidiEvent createProgramChangeEvent(int channel, int value, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(0xC0 + channel, value, 0);
        MidiEvent evt = new MidiEvent(msg, tick);
        return evt;
    }

    protected MidiEvent createNoteOnEvent(int channel, int pitch, int velocity, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(0x90 + channel, pitch, velocity);
        MidiEvent evt = new MidiEvent(msg, tick);
        return evt;
    }

    protected MidiEvent createNoteOffEvent(int channel, int pitch, int velocity, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(0x80 + channel, pitch, velocity);
        MidiEvent evt = new MidiEvent(msg, tick);
        return evt;
    }

    protected MidiEvent createTempoMetaEvent(int tempo, long tick) throws InvalidMidiDataException
    {
        MetaMessage msg = new MetaMessage();
        byte[] data = ByteBuffer.allocate(4).putInt(1000000 * 60 / tempo).array();
        data[0] = data[1];
        data[1] = data[2];
        data[2] = data[3];
        msg.setMessage(0x51, data, 3);
        MidiEvent evt = new MidiEvent(msg, tick);
        return evt;
    }

    protected MidiEvent createTextMetaEvent(String text, long tick) throws InvalidMidiDataException
    {
        MetaMessage msg = new MetaMessage();
        byte[] data = text.getBytes();
        msg.setMessage(0x01, data, data.length);
        MidiEvent evt = new MidiEvent(msg, tick);
        return evt;
    }
}
