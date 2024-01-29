package aeronicamc.mods.mxtune.util;

import net.minecraft.util.Tuple;

public class ValidDuration extends Tuple<Boolean, Integer>
{
    public static final ValidDuration INVALID = new ValidDuration(false, 0);

    public ValidDuration(Boolean isValidMML, Integer duration)
    {
        super(isValidMML, duration);
    }

    public Boolean isValidMML()
    {
        return super.getA();
    }

    public Integer getDuration()
    {
        return super.getB();
    }
}
