package aeronicamc.mods.mxtune.caps.stages;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    private static StageAreaData initStageArea()
    {
        return new StageAreaData(BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO, "", UUID.randomUUID());
    }

    public static StageToolState create(LivingEntity livingEntity)
    {
        StageAreaData tempArea = initStageArea();
        tempArea.setOwnerUUID(livingEntity.getUUID());
        setupArea.putIfAbsent(livingEntity.getId(), tempArea);
        return Corner1;
    }

    public static void reset(LivingEntity livingEntity)
    {
        setupArea.remove(livingEntity.getId());
    }

    public static StageToolState edit(LivingEntity livingEntity, StageAreaData stageAreaData, StageToolState stageToolState)
    {
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
