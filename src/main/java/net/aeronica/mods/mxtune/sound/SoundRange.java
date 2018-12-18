package net.aeronica.mods.mxtune.sound;

public enum SoundRange
{
    NEAR(1F, "mxtune.sound_range.near"),
    MEDIUM(2F, "mxtune.sound_range.medium"),
    FAR(3F, "mxtune.sound_range.far");

    float range;
    String languageKey;

    SoundRange(float rangeIn, String languageKeyIn)
    {
        range = rangeIn;
        languageKey = languageKeyIn;
    }

    public float getRange() { return range; }

    public String getLanguageKey() { return languageKey; }
}
