package net.aeronica.libs.mml.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Patch;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class MMLToMIDI extends MMLTransformBase
{
    private static final int MASTER_TEMPO = 120;
    private static final double PPQ = 480.0;
    private static final int TICKS_OFFSET = (int) (PPQ);
    private Sequence sequence;
    private HashSet<Integer> packedPresets = new HashSet<Integer>();

    public MMLToMIDI() {}

    @Override
    public long durationTicks(int mmlNoteLength, boolean dottedLEN)
    {
        double dot = dottedLEN ? 15.0d : 10.0d;
        return (long) (((4.0d / (double) mmlNoteLength) * ((double) dot) / 10.0d) * (double) PPQ);
    }

    public Sequence getSequence() {return sequence;}
    
    public List<Integer> getPackedPresets()
    {
        List<Integer> packedPresetList = new ArrayList<Integer>();
        for (Integer i: packedPresets) packedPresetList.add(i);
        return packedPresetList;
    }

    @Override
    public void processMObjects(List<MObject> mmlObject)
    {
        int ch = 0;
        int tk = 0;
        long ticksOffset = TICKS_OFFSET;
        int currentTempo = MASTER_TEMPO;

        try
        {
            sequence = new Sequence(Sequence.PPQ, (int) PPQ);
            for (int i = 0; i < 24; i++)
            {
                sequence.createTrack();
            }
            Track[] tracks = sequence.getTracks();

            for (int i = 0; i < mmlObject.size(); i++)
            {
                /* ref: enum Type {INST_BEGIN, TEMPO, INST, PART, NOTE, REST, INST_END, DONE}; */
                MObject mmo = getMObject(i);
                switch (mmo.getType())
                {
                case INST_BEGIN:
                case REST:
                {                    
                    /* Nothing to do in this implementation */
                    break;
                }
                case TEMPO:
                {
                    currentTempo = mmo.getTempo();
                    tracks[0].add(createTempoMetaEvent(currentTempo, mmo.getStartingTicks() + ticksOffset));
                    break;
                }
                case INST:
                {
                    Patch preset = MMLUtil.packetPreset2Patch(mmo.getInstrument());
                    int bank =  preset.getBank();
                    int programPreset = preset.getProgram();
                    /* Detect a percussion set */
                    if (bank == 128)
                    {
                        /* Set Bank Select for Rhythm Channel MSB 0x78, LSB 0x00  - 14 bits only */
                        bank = 0x7800 >>> 1;
                    }
                    else
                    {
                        /* Convert the preset bank to the Bank Select bank */
                        bank = bank << 7;
                    }
                    tracks[tk].add(createBankSelectEventMSB(ch, bank, mmo.getStartingTicks() + ticksOffset-2));
                    tracks[tk].add(createBankSelectEventLSB(ch, bank, mmo.getStartingTicks() + ticksOffset-1));
                    tracks[tk].add(createProgramChangeEvent(ch, programPreset, mmo.getStartingTicks() + ticksOffset));
                    packedPresets.add(mmo.getInstrument());
                    break;
                }
                case PART:
                {
                    tk++;
                    if (tk > 23) tk = 23;
                    break;
                }
                case NOTE:
                {
                    tracks[tk].add(createNoteOnEvent(ch, MMLUtil.smartClampMIDI(mmo.getMidiNote()), mmo.getNoteVolume(), mmo.getStartingTicks() + ticksOffset));
                    tracks[tk].add(createNoteOffEvent(ch, MMLUtil.smartClampMIDI(mmo.getMidiNote()), mmo.getNoteVolume(), mmo.getStartingTicks() + mmo.getLengthTicks() + ticksOffset - 1));
                    if (mmo.getText() != null)
                    {
                        String text = "{\"Note\": \"{Track\":" + tk + ", \"Text\":\"" + mmo.getText() + "\"}}";
                        tracks[0].add(createTextMetaEvent(text, mmo.getStartingTicks() + ticksOffset));
                    }
                    break;
                }
                case INST_END:
                {
                    tk++;
                    if (tk > 23) tk = 23;
                    ch++;
                    if (ch > 15) ch = 15;
                    tracks[tk].add(createMsg(192, ch, 0, 100, 0l));
                    break;
                }
                case DONE:
                {
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

    protected MidiEvent createBankSelectEventMSB(int channel, int value, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 0, value >> 7);
        MidiEvent evt = new MidiEvent(msg, tick);
        return evt;
    }
    
    protected MidiEvent createBankSelectEventLSB(int channel, int value, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 32, value & 0x7F);
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
