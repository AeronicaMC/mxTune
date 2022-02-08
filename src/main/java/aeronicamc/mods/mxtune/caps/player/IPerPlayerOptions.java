package aeronicamc.mods.mxtune.caps.player;

import net.minecraft.nbt.INBT;

import javax.annotation.Nullable;

public interface IPerPlayerOptions
{
    void setPlayId(int playId);

    int getPlayId();

    @Nullable
    INBT serializeNBT();

    void deserializeNBT(INBT nbt);

    void sync();
}
