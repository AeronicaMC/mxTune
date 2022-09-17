package aeronicamc.mods.mxtune.sound;

import javax.sound.midi.Sequence;

public class SequenceProxy
{
    Sequence sequence;
    int timeout;

    SequenceProxy(Sequence seq)
    {
        sequence = seq;
        timeout = 1800; // 30 minutes in seconds
    }

    Sequence getSequence()
    {
        return sequence;
    }

    int getTimeout()
    {
        return timeout;
    }

    void tick()
    {
        timeout--;
    }
}
