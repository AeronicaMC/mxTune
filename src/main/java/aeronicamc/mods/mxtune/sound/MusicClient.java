package aeronicamc.mods.mxtune.sound;

public class MusicClient extends MxSound
{

    MusicClient(AudioData audioData)
    {
        super(audioData);
        this.attenuation = AttenuationType.NONE;
    }

    @Override
    public boolean isRelative()
    {
        return true;
    }

    @Override
    public void onUpdate()
    {
        // NOP
    }
}
