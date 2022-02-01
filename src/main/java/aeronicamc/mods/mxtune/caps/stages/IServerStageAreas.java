package aeronicamc.mods.mxtune.caps.stages;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

public interface IServerStageAreas extends INBTSerializable
{
    List<StageAreaData> getStageAreas();

    RegistryKey<World> getDimension();

    Integer getInt();

    void setInt(Integer someInt);
}
