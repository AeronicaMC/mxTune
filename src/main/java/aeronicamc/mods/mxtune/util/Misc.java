package aeronicamc.mods.mxtune.util;

public class Misc
{
    private Misc() { /* NOP */ }

    public static int clamp(int min, int max, int value) {return Math.max(Math.min(max, value), min);}
}
