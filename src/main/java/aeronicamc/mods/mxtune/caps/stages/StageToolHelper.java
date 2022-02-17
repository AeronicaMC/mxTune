package aeronicamc.mods.mxtune.caps.stages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StageToolHelper
{
    // entityId to StageAreaData map
    private static final Map<Integer, StageAreaData> wipStages = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Integer, StageToolState> setupState = Collections.synchronizedMap(new HashMap<>());

    private StageToolHelper() { /* NOP */ }
}
