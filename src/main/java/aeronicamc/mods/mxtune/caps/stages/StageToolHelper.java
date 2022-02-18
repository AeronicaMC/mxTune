package aeronicamc.mods.mxtune.caps.stages;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StageToolHelper
{
    // entityId to StageToolState map
    private static final Map<Integer, StageToolState> setupState = Collections.synchronizedMap(new HashMap<>());

    private StageToolHelper() { /* NOP */ }

    public static StageToolState create(LivingEntity livingEntity)
    {
        StageToolState toolState = StageToolState.create(livingEntity);
        setupState.putIfAbsent(livingEntity.getId(), toolState);
        return toolState;
    }

    public static StageToolState edit(LivingEntity livingEntity, StageAreaData stageArea, StageToolState toolState, Object object)
    {
        StageToolState nextState = StageToolState.edit(livingEntity, stageArea, toolState).apply(object, livingEntity.getId());
        if (setupState.containsKey(livingEntity.getId())) {setupState.replace(livingEntity.getId(), nextState);}
        else {setupState.putIfAbsent(livingEntity.getId(), nextState);}
        return nextState;
    }

    @Nullable
    public StageAreaData get(LivingEntity livingEntity)
    {
        if (StageToolState.has(livingEntity))
        {
            return StageToolState.get(livingEntity);
        }
        return null;
    }
}
