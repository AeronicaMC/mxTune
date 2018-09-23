package net.aeronica.mods.mxtune.world;

import net.minecraft.world.IWorldNameable;
import net.minecraft.world.LockCode;

public interface IModLockableContainer extends IWorldNameable
{
    boolean isLocked();

    void setLockCode(LockCode code);

    LockCode getLockCode();
}
