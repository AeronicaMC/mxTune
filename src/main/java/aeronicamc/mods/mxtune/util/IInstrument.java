package aeronicamc.mods.mxtune.util;

import net.minecraft.item.ItemStack;

public interface IInstrument
{
    String PATCH = "MXTunePatch";
    String KEY_AUTO_SELECT = "MXTuneAutoSelect";
    int getPatch(ItemStack itemStack);
    void setPatch(ItemStack itemStack, int patch);
    boolean getAutoSelect(ItemStack itemStack);
    void setAutoSelect(ItemStack itemStack ,boolean auto);
}
