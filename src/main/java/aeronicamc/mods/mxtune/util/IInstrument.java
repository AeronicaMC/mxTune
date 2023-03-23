package aeronicamc.mods.mxtune.util;

import net.minecraft.item.ItemStack;

public interface IInstrument
{
    static final String PATCH = "MXTunePatch";
    final static String KEY_AUTO_SELECT = "MXTuneAutoSelect";
    int getPatch(ItemStack itemStack);
    void setPatch(ItemStack itemStack, int patch);
    boolean getAutoSelect(ItemStack itemStack);
    void setAutoSelect(ItemStack itemStack ,boolean auto);
}
