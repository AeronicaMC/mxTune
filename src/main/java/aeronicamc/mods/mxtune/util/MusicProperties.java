package aeronicamc.mods.mxtune.util;

import net.minecraft.util.Tuple;

public class MusicProperties extends Tuple<String, Integer>
{
    public static final MusicProperties INVALID = new MusicProperties("", 0);

    public MusicProperties(String musicText, Integer duration)
    {
        super(musicText, duration);
    }

    public String getMusicText()
    {
        return super.getA();
    }

    public Integer getDuration()
    {
        return super.getB();
    }
}
