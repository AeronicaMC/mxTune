package net.aeronica.mods.mxtune.sound;

import net.minecraft.util.SoundCategory;

public class MusicClient extends MxSound
{

    MusicClient(AudioData audioData)
    {
        super(audioData, SoundCategory.MUSIC);
        this.attenuation = AttenuationType.NONE;
    }

    @Override
    public boolean isRelative()
    {
        return true;
    }

    @Override
    protected void onUpdate()
    {
        // NOP
    }
}
