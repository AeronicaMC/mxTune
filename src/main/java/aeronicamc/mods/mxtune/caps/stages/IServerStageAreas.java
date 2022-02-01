package aeronicamc.mods.mxtune.caps.stages;

import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

public interface IServerStageAreas extends INBTSerializable
{
    List<StageAreaData> getStageAreas();

    Integer getInt();

    void setInt(Integer someInt);
}
