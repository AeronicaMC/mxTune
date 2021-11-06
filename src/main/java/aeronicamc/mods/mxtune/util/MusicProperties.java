package aeronicamc.mods.mxtune.util;

import net.minecraft.util.Tuple;

@SuppressWarnings("unchecked")
public class MusicProperties extends Tuple
{
    public static final MusicProperties INVALID = new MusicProperties("", 0);

    public MusicProperties(String musicText, Integer duration)
    {
        super(musicText, duration);
    }

    public String getMusicText()
    {
        return (String) super.getA();
    }

    public Integer getDuration()
    {
        return (Integer) super.getB();
    }
}
