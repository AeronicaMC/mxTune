package net.aeronica.mods.mxtune.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("unused")
public enum SoundRange
{
    NEAR(2F, "mxtune.sound_range.near", true),              // 16 Blocks, linear attenuation, point source
    MEDIUM(3F, "mxtune.sound_range.medium", true),          // 32 Blocks, linear attenuation, point source
    FAR(4F, "mxtune.sound_range.far", true),                // 64 Blocks, linear attenuation, point source (Junk Box)
    INFINITY(2F, "mxtune.sound_range.infinity", false);     // Background music, no attenuation, non-directional

    public static final int LENGTH = SoundRange.values().length;
    public static final String SOUND_RANGE_KEY = "soundRange";
    float range;
    String languageKey;
    boolean useLinear;

    SoundRange(float rangeIn, String languageKeyIn, boolean useLinearIn)
    {
        range = rangeIn;
        languageKey = languageKeyIn;
        useLinear = useLinearIn;
    }

    /**
     * Note: when SoundRange == INFINITY the ClientAudio#getAudioFormat method changes the audio format to stereo
     * which disables the directional 3D sound feature. Thus the sound has no source. Essentially it becomes
     * background music.
     * @return the range selected
     */
    public float getRange() { return range; }

    @SideOnly(Side.CLIENT)
    public ISound.AttenuationType getAttenuationType() { return useLinear ? ISound.AttenuationType.LINEAR : ISound.AttenuationType.NONE; }

    public String getLanguageKey() { return languageKey; }

    // *** SoundRange utility methods ***
    public static SoundRange nextRange(SoundRange soundRange) { return getSoundRange(soundRange.ordinal() + 1); }

    public static int getIndex(SoundRange soundRange) { return soundRange.ordinal(); }

    public static SoundRange getSoundRange(SoundRange soundRange) { return SoundRange.values()[soundRange.ordinal()]; }

    public static SoundRange getSoundRange(int index) { return SoundRange.values()[index % LENGTH]; }

    public void toNBT(NBTTagCompound nbt) { nbt.setString(SOUND_RANGE_KEY, name()); }

    public static SoundRange fromNBT(NBTTagCompound nbt)
    {
        if (nbt.hasKey(SOUND_RANGE_KEY, Constants.NBT.TAG_STRING))
        {
            String s = nbt.getString(SOUND_RANGE_KEY);
            return SoundRange.valueOf(s);
        }
        else
        {
            return SoundRange.NEAR;
        }
    }

}
