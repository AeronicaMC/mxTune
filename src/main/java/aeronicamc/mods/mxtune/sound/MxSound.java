package aeronicamc.mods.mxtune.sound;

import aeronicamc.mods.mxtune.init.ModSoundEvents;
import net.minecraft.client.audio.*;
import net.minecraft.util.SoundCategory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class MxSound extends TickableSound
{
    protected int playID;
    // volumeBase: A volume multiplicand that severs as static or calculated base volume.
    // Inheriting classes can use this to add volume at a distance effects, etc.
    protected float volumeBase;
    protected AudioData audioData;
    private final SoundEventAccessor soundEventAccessor;

    protected MxSound(AudioData audioData)
    {
        this(audioData, SoundCategory.RECORDS);
    }

    protected MxSound(AudioData audioData, SoundCategory categoryIn)
    {
        super(ModSoundEvents.PCM_PROXY.get(), categoryIn);
        this.audioData = audioData;
        this.playID = audioData.getPlayId();
        this.volumeBase = 1F;
        this.audioData.setISound(this);
        this.sound = new PCMSound();
        this.volume = 1F;
        this.pitch = 1F;
        this.looping = false;
        this.delay = 0;
        this.x = 0F;
        this.y = 0F;
        this.z = 0F;
        this.attenuation = AttenuationType.LINEAR;
        this.soundEventAccessor = new SoundEventAccessor(this.sound.getLocation(), "subtitle.mxtune.pcm-proxy");
    }

    Optional<AudioData> getAudioData()
    {
        return Optional.ofNullable(audioData);
    }

    @Override
    public SoundEventAccessor resolve(SoundHandler handler)
    {
        return this.soundEventAccessor;
    }

    @Override
    public void tick()
    {
        onUpdate();
        getAudioData().ifPresent(data -> {
            data.updateVolumeFade();
            volume = volumeBase * data.getFadeMultiplier();
        });
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }

    protected void onUpdate() { /* NOP */ }

    protected void setDonePlaying()
    {
        this.stop();
    }

    @Override
    public CompletableFuture<IAudioStream> getStream(AudioStreamManager soundBuffers, Sound sound, boolean looping)
    {
        return CompletableFuture.completedFuture(new PCMAudioStream(audioData));
    }
}
