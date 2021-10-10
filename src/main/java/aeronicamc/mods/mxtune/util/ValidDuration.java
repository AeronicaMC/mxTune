package aeronicamc.mods.mxtune.util;

import net.minecraft.util.Tuple;

@SuppressWarnings("unchecked")
public class ValidDuration extends Tuple
{
    public static final ValidDuration INVALID = new ValidDuration(false, 0);

    public ValidDuration(Boolean isValidMML, Integer duration)
    {
        super(isValidMML, duration);
    }

    public Boolean isValidMML()
    {
        return (Boolean) super.getA();
    }

    public Integer getDuration()
    {
        return (Integer) super.getB();
    }
}
