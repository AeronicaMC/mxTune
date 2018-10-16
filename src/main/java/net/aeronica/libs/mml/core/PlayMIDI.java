package net.aeronica.libs.mml.core;

import javax.sound.midi.*;

public class PlayMIDI implements MetaEventListener
{
    private Sequencer sequencer;
    private Synthesizer synthesizer;

    PlayMIDI() {/* NOP */}

     // TEMPO = 81 End of Track = 47
    @Override
    public void meta(MetaMessage event)
    {
        if (event.getType() == 47)
        { // end of stream
            sequencer.stop();
            sequencer.setMicrosecondPosition(0L);
            sequencer.removeMetaEventListener(this);
            MMLUtil.MML_LOGGER.info("MetaMessage EOS event");
            try
            {
                Thread.sleep(250);
            } catch (InterruptedException e)
            {
                MMLUtil.MML_LOGGER.error(e);
                Thread.currentThread().interrupt();
            } finally {
                if (sequencer != null && sequencer.isOpen()) sequencer.close();
                if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();
            }
        }
        if (event.getType() == 81)
        {
            int tempo = decodeTempo(event);
            MMLUtil.MML_LOGGER.info("{\"Tempo\": " + tempo + "}");
        }
        if (event.getType() == 1)
        {
            String text = decodeText(event);
            MMLUtil.MML_LOGGER.info(text);
        }

    }

    private int decodeTempo(MetaMessage event)
    {
        byte[] abData = event.getData();
        // tempo in microseconds per beat
        int nTempo = ((abData[0] & 0xFF) << 16) | ((abData[1] & 0xFF) << 8) | (abData[2] & 0xFF);

        float bpm = convertTempo(nTempo);
        // truncate it to 2 digits after dot
        return (int) (Math.round(bpm * 100.0f) / 100.0f);
    }

    private String decodeText(MetaMessage event)
    {
        byte[] abData = event.getData();
        return new String(abData);
    }

    // convert from microseconds per quarter note to beats per minute and vice versa
    private float convertTempo(float valueIn)
    {
        float value = valueIn;
        if (value <= 0)
        {
            value = 0.1f;
        }
        return 60000000.0f / value;
    }

    void mmlPlay(Sequence sequence)
    {
        try
        {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();

            sequencer = MidiSystem.getSequencer();
            sequencer.addMetaEventListener(this);
            sequencer.setMicrosecondPosition(0L);
            sequencer.setTempoInBPM((float) 120);

            sequencer.open();
            for (Transmitter t : sequencer.getTransmitters())
            {
                t.setReceiver(synthesizer.getReceiver());
            }

            sequencer.setSequence(sequence);
            sequencer.start();

        } catch (Exception e)
        {
            if (sequencer != null && sequencer.isOpen()) sequencer.close();
            if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();
            MMLUtil.MML_LOGGER.error(e);
        }
    }
}
