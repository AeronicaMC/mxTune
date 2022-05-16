package aeronicamc.mods.mxtune.blocks;

import java.util.UUID;

public interface ILockable
{
    boolean isLocked();

    void setLock(boolean lock);

    boolean isOwner(UUID owner);

    void setOwner(UUID owner);

    UUID getOwner();
}
