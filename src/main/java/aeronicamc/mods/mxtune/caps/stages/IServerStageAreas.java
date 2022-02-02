package aeronicamc.mods.mxtune.caps.stages;

import net.minecraft.nbt.INBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public interface IServerStageAreas
{
    List<StageAreaData> getStageAreas();

    RegistryKey<World> getDimension();

    int getInt();

    void setInt(Integer someInt);

    @Nullable
    INBT serializeNBT();

    void deserializeNBT(INBT nbt);

    void sync();

    void test();
}
