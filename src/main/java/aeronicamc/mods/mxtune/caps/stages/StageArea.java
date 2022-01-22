package aeronicamc.mods.mxtune.caps.stages;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class StageArea
{
    final BlockPos startPos;
    final BlockPos endPos;
    final AxisAlignedBB areaAABB;
    final UUID ownerUUID;

    StageArea(final BlockPos startPos, final BlockPos endPos, UUID ownerUUID)
    {
        this.startPos = startPos;
        this.endPos = endPos;
        this.ownerUUID = ownerUUID;
        this.areaAABB = new AxisAlignedBB(this.startPos, this.endPos);
    }
}
