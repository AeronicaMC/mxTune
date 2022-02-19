package aeronicamc.mods.mxtune.caps.stages;

import aeronicamc.mods.mxtune.util.Color3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum StageToolState
{
    Corner1 {
        @Override
        public <T> StageToolState apply(T object, Integer id)
        {
            setupArea.get(id).setStartPos((BlockPos) object);
            return Corner2;
        }
    },
    Corner2 {
        @Override
        public <T> StageToolState apply(T object, Integer id)
        {
            setupArea.get(id).setEndPos((BlockPos) object);
            return AudienceSpawn;
        }
    },
    AudienceSpawn {
        @Override
        public <T> StageToolState apply(T object, Integer id)
        {
            setupArea.get(id).setAudienceSpawn((BlockPos) object);
            return StageSpawn;
        }
    },
    StageSpawn {
        @Override
        public <T> StageToolState apply(T object, Integer id)
        {
            setupArea.get(id).setPerformerSpawn((BlockPos) object);
            return Name;
        }
    },
    Name {
        @Override
        public <T> StageToolState apply(T object, Integer id)
        {
            setupArea.get(id).setName((String) object);
            return Done;
        }
    },
    Corner1Edit {
        @Override
        public <T> StageToolState apply(T object, Integer id)
        {
            setupArea.get(id).setStartPos((BlockPos) object);
            return Done;
        }
    },
    Corner2Edit {
        @Override
        public <T> StageToolState apply(T object, Integer id)
        {
            setupArea.get(id).setEndPos((BlockPos) object);
            return Done;
        }
    },
    AudienceSpawnEdit {
        @Override
        public <T> StageToolState apply(T object, Integer id)
        {
            setupArea.get(id).setAudienceSpawn((BlockPos) object);
            return Done;
        }
    },
    StageSpawnEdit {
        @Override
        public <T> StageToolState apply(T object, Integer id)
        {
            setupArea.get(id).setPerformerSpawn((BlockPos) object);
            return Done;
        }
    },
    NameEdit {
        @Override
        public <T> StageToolState apply(T object, Integer id)
        {
            setupArea.get(id).setName((String) object);
            return Done;
        }
    },
    Done {
        @Override
        public <T> StageToolState apply(T object, Integer id)
        {
            return Done;
        }
    };

    private static final Map<Integer, StageAreaData> setupArea = Collections.synchronizedMap(new HashMap<>());

    public abstract <T extends Object> StageToolState apply(T object, Integer id);

    private static StageAreaData initStageArea(LivingEntity livingEntity)
    {
        Color3f color = Color3f.rainbowFactory();
        return new StageAreaData(BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO, color.toString(), livingEntity.getUUID(), color.getR(), color.getG(), color.getB());
    }

    public static StageToolState create(LivingEntity livingEntity)
    {
        StageAreaData tempStage = initStageArea(livingEntity);
        tempStage.setToolState(Corner1);
        setupArea.putIfAbsent(livingEntity.getId(), tempStage);
        return Corner1;
    }

    public static void reset(LivingEntity livingEntity)
    {
        setupArea.remove(livingEntity.getId());
    }

    public static StageToolState edit(LivingEntity livingEntity, StageAreaData stageAreaData, StageToolState stageToolState)
    {
        stageAreaData.setToolState(stageToolState);
        setupArea.remove(livingEntity.getId());
        setupArea.putIfAbsent(livingEntity.getId(), stageAreaData);
        return stageToolState;
    }

    public static boolean has(LivingEntity livingEntity)
    {
        return setupArea.containsKey(livingEntity.getId());
    }

    @Nullable
    public static StageAreaData get(LivingEntity livingEntity)
    {
        return setupArea.get(livingEntity.getId());
    }
}
