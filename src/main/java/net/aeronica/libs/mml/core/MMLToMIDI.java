package net.aeronica.libs.mml.core;

import javax.sound.midi.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.aeronica.libs.mml.core.MMLUtil.*;

public class MMLToMIDI extends MMLTransformBase
{
    private static final double PPQ = 480.0;
    private static final int TICKS_OFFSET = 10;
    // 8 players with 10 parts each = 80 parts.
    // 12 slots with 10 parts each = 120 parts.
    // No problem :D, but will make it 160 because we can!
    private static final int MAX_TRACKS = 160;
    private Sequence sequence;
    private Set<Integer> packedPresets = new HashSet<>();
    private int channel;
    private int track;

    public MMLToMIDI() {/* NOP */}

    @Override
    public long durationTicks(int mmlNoteLength, boolean dottedLEN)
    {
        double dot = dottedLEN ? 15.0d : 10.0d;
        return (long) (((4.0d / (double) mmlNoteLength) * dot / 10.0d) * PPQ);
    }

    public Sequence getSequence() {return sequence;}
    
    public List<Integer> getPackedPresets()
    {
        return new ArrayList<>(packedPresets);
    }

    @Override
    public void processMObjects(List<MObject> mmlObjects)
    {
        channel = 0;
        track = 0;
        long ticksOffset = TICKS_OFFSET;
        int currentTempo;

        try
        {
            sequence = new Sequence(Sequence.PPQ, (int) PPQ);
            for (int i = 0; i < MAX_TRACKS; i++)
            {
                sequence.createTrack();
            }
            Track[] tracks = sequence.getTracks();

            for (MObject mmo: mmlObjects)
            {
                switch (mmo.getType())
                {
                case INST_BEGIN:
                case REST:
                    break;

                case TEMPO:
                    currentTempo = mmo.getTempo();
                    tracks[0].add(createTempoMetaEvent(currentTempo, mmo.getStartingTicks() + ticksOffset));
                    break;

                case INST:
                    addInstrument(mmo, tracks[track], channel, ticksOffset);
                    break;

                case PART:
                    nextTrack();
                    break;

                case NOTE:
                    addNote(mmo, tracks, track, channel, ticksOffset);
                    break;

                case INST_END:
                    nextTrack();
                    nextChannel();
                    break;

                case DONE:
                    break;

                default:
                    MML_LOGGER.debug("MMLToMIDI#processMObjects Impossible?! An undefined enum?");
                }
            }
        } catch (InvalidMidiDataException e)
        {
            MML_LOGGER.error("MMLToMIDI#processMObjects failed: {}", e);
        }
    }

    private void nextTrack()
    {
        if (track++ > MAX_TRACKS) track = MAX_TRACKS;
    }

    private void nextChannel()
    {
        channel++;
        if (channel == 8) channel = 10; // Skip over percussion channels
        if (channel > 15) channel = 15;
    }

    private void addInstrument(MObject mmo, Track track, int ch, long ticksOffset) throws InvalidMidiDataException
    {
        Patch preset = packedPreset2Patch(mmo.getInstrument());
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
        track.add(createBankSelectEventMSB(ch, bank, mmo.getStartingTicks() + ticksOffset-2L));
        track.add(createBankSelectEventLSB(ch, bank, mmo.getStartingTicks() + ticksOffset-1L));
        track.add(createProgramChangeEvent(ch, programPreset, mmo.getStartingTicks() + ticksOffset));
        packedPresets.add(mmo.getInstrument());
    }

    private void addNote(MObject mmo, Track[] tracks, int track, int channel, long ticksOffset) throws InvalidMidiDataException
    {
        tracks[track].add(createNoteOnEvent(channel, smartClampMIDI(mmo.getMidiNote()), mmo.getNoteVolume(), mmo.getStartingTicks() + ticksOffset));
        tracks[track].add(createNoteOffEvent(channel, smartClampMIDI(mmo.getMidiNote()), mmo.getNoteVolume(), mmo.getStartingTicks() + mmo.getLengthTicks() + ticksOffset - 1));
    }

    private MidiEvent createProgramChangeEvent(int channel, int value, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, value, 0);
        return new MidiEvent(msg, tick);
    }

    private MidiEvent createBankSelectEventMSB(int channel, int value, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 0, value >> 7);
        return new MidiEvent(msg, tick);
    }
    
    private MidiEvent createBankSelectEventLSB(int channel, int value, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 32, value & 0x7F);
        return new MidiEvent(msg, tick);
    }
    
    private MidiEvent createNoteOnEvent(int channel, int pitch, int velocity, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_ON, channel, pitch, velocity);
        return new MidiEvent(msg, tick);
    }

    private MidiEvent createNoteOffEvent(int channel, int pitch, int velocity, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_OFF, channel, pitch, velocity);
        return new MidiEvent(msg, tick);
    }

    private MidiEvent createTempoMetaEvent(int tempo, long tick) throws InvalidMidiDataException
    {
        MetaMessage msg = new MetaMessage();
        byte[] data = ByteBuffer.allocate(4).putInt(1000000 * 60 / tempo).array();
        data[0] = data[1];
        data[1] = data[2];
        data[2] = data[3];
        msg.setMessage(0x51, data, 3);
        return new MidiEvent(msg, tick);
    }

    @SuppressWarnings("unused")
    private MidiEvent createTextMetaEvent(String text, long tick) throws InvalidMidiDataException
    {
        MetaMessage msg = new MetaMessage();
        byte[] data = text.getBytes();
        msg.setMessage(0x01, data, data.length);
        return new MidiEvent(msg, tick);
    }
}
