package aeronicamc.mods.mxtune.sound;

public class MusicClient extends MxSound
{

    MusicClient(AudioData audioData)
    {
        super(audioData);
        this.attenuation = AttenuationType.NONE;
        this.volumeBase = 0.6F;
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
