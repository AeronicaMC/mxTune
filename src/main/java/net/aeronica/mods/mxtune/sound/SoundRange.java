package net.aeronica.mods.mxtune.sound;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

@SuppressWarnings("unused")
public enum SoundRange
{
    NEAR(1F, "mxtune.sound_range.near"),
    MEDIUM(2F, "mxtune.sound_range.medium"),
    FAR(3F, "mxtune.sound_range.far");

    public static final int LENGTH = SoundRange.values().length;
    public static final String SOUND_RANGE_KEY = "soundRange";
    float range;
    String languageKey;

    SoundRange(float rangeIn, String languageKeyIn)
    {
        range = rangeIn;
        languageKey = languageKeyIn;
    }

    public float getRange() { return range; }

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
