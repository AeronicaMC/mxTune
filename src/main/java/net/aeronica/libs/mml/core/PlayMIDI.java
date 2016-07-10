package net.aeronica.libs.mml.core;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

public class PlayMIDI implements MetaEventListener
{
    static Sequencer sequencer = null;
    static Synthesizer synthesizer = null;

    /**
     * TEMPO = 81 End of Track = 47
     */
    public void meta(MetaMessage event)
    {
        if (event.getType() == 47)
        { // end of stream
            sequencer.stop();
            sequencer.setMicrosecondPosition(0L);
            sequencer.removeMetaEventListener(this);
            System.out.println("MetaMessage EOS event");
            try
            {
                Thread.sleep(250);
            } catch (InterruptedException e)
            {
            }
            if (sequencer != null && sequencer.isOpen()) sequencer.close();
            if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();
        }
        if (event.getType() == 81)
        {
            int tempo = decodeTempo(event);
            System.out.println("{\"Tempo\": " + tempo + "}");
        }
        if (event.getType() == 1)
        {
            String text = decodeText(event);
            System.out.println(text);
        }

    }

    public int decodeTempo(MetaMessage event)
    {
        // byte[] abMessage = event.getMessage();
        byte[] abData = event.getData();
        // int nDataLength = event.getLength();
        // String strMessage = null;

        int nTempo = ((abData[0] & 0xFF) << 16) | ((abData[1] & 0xFF) << 8) | (abData[2] & 0xFF); // tempo
                                                                                                  // in
                                                                                                  // microseconds
                                                                                                  // per
                                                                                                  // beat
        float bpm = convertTempo(nTempo);
        // truncate it to 2 digits after dot
        bpm = (float) (Math.round(bpm * 100.0f) / 100.0f);
        // strMessage = "Set Tempo: "+bpm+" bpm";
        return (int) bpm;
    }

    public String decodeText(MetaMessage event)
    {
        byte[] abData = event.getData();
        return new String(abData);
    }

    // convert from microseconds per quarter note to beats per minute and vice
    // versa
    private static float convertTempo(float value)
    {
        if (value <= 0)
        {
            value = 0.1f;
        }
        return 60000000.0f / value;
    }

    public boolean mmlPlay(Sequence sequence)
    {
        try
        {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();

            sequencer = MidiSystem.getSequencer();
            sequencer.addMetaEventListener(this);
            sequencer.setMicrosecondPosition(0l);
            sequencer.setTempoInBPM((float) 120);

            sequencer.open();
            for (Transmitter t : sequencer.getTransmitters())
            {
                t.setReceiver(synthesizer.getReceiver());
            }

            // sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());

            sequencer.setSequence(sequence);
            sequencer.start();

        } catch (Exception ex)
        {
            if (sequencer != null && sequencer.isOpen()) sequencer.close();
            if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();
            System.out.println("PlayMIDI#mmlPlay failed midi TRY " + ex);
        }
        return true;
    }
}
