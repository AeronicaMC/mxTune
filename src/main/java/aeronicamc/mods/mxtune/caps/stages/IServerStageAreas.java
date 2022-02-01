package aeronicamc.mods.mxtune.caps.stages;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public interface IServerStageAreas
{
    StageAreaData getStageAreaData();

    AxisAlignedBB getAreaAABB();

    ITextComponent getTitle();

    BlockPos getPerformerSpawn();

    BlockPos getAudienceSpawn();

    void setStageAreaData(StageAreaData stageAreaData);

    Integer getInt();

    void setInt(Integer someInt);
}
