package aeronicamc.mods.mxtune.sound;

import javax.sound.midi.Sequence;

public class SequenceProxy
{
    Sequence sequence;
    int timeout;

    SequenceProxy(Sequence seq, int duration)
    {
        sequence = seq;
        timeout = duration + 60; // tune duration + 60 seconds for good measure
    }

    Sequence getSequence()
    {
        return sequence;
    }

    int tick()
    {
        return --timeout;
    }
}
