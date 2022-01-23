package aeronicamc.mods.mxtune.caps.stages;

import java.util.ArrayList;
import java.util.List;

public class ServerStageAreas implements IServerStageAreas
{
    List<StageArea> stageAreas = new ArrayList<>();
    Integer someInt;

    ServerStageAreas()
    {
        someInt = 0;
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
}
