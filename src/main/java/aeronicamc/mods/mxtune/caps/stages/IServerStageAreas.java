package aeronicamc.mods.mxtune.caps.stages;

import net.minecraft.nbt.INBT;

import javax.annotation.Nullable;
import java.util.List;

public interface IServerStageAreas
{
    List<StageAreaData> getStageAreas();

    void addArea(StageAreaData stageAreaData);

    int getInt();

    void setInt(Integer someInt);

    @Nullable
    INBT serializeNBT();

    void deserializeNBT(INBT nbt);

    void sync();

    void test();
}
