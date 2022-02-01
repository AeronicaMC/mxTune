package aeronicamc.mods.mxtune.caps.stages;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class ServerStageAreas implements IServerStageAreas
{
    //List<StageAreaData> stageAreaData = new ArrayList<>();
    StageAreaData stageAreaData = new StageAreaData(World.OVERWORLD,
                                                    new BlockPos(173,70,-441),
                                                    new BlockPos(177,72,-445),
                                                    new BlockPos(176,70,-441),
                                                    new BlockPos(173,70,-441), "Random Stage", UUID.randomUUID());
    Integer someInt;

    ServerStageAreas()
    {
        someInt = 0;
    }

    @Override
    public StageAreaData getStageAreaData()
    {
        return stageAreaData;
    }

    @Override
    public void setStageAreaData(StageAreaData stageAreaData)
    {
        this.stageAreaData = stageAreaData;
    }

    @Override
    public Integer getInt()
    {
        return someInt;
    }

    @Override
    public void setInt(Integer someInt)
    {
        this.someInt = someInt;
    }

    public void sync(World world)
    {
        if (!world.isClientSide())
        {
            // TODO: Sync to client with network packet.
        }
    }
}
