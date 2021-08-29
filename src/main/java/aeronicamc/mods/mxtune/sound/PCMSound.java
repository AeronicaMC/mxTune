package aeronicamc.mods.mxtune.sound;

import aeronicamc.mods.mxtune.Reference;
import net.minecraft.client.audio.Sound;
import net.minecraft.util.ResourceLocation;

public class PCMSound extends Sound
{
    public PCMSound()
    {
        // String nameIn, float volumeIn, float pitchIn, int weightIn, Type typeIn, boolean streamingIn, boolean preloadIn, int attenuationDistanceIn
        super(Reference.MOD_ID + ":pcm-proxy", 1F, 1F, 0, Type.SOUND_EVENT, true, false, 64);
    }

    @Override
    public ResourceLocation getPath()
    {
        return new ResourceLocation(Reference.MOD_ID, "sounds/" + getLocation().getPath() + ".nul");
    }
}
