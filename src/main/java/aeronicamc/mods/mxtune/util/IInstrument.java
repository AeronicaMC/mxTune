package aeronicamc.mods.mxtune.util;

import net.minecraft.item.ItemStack;

public interface IInstrument
{
    static final String PATCH = "MXTunePatch";
    int getPatch(ItemStack itemStack);
    void setPatch(ItemStack itemStack, int patch);
}
