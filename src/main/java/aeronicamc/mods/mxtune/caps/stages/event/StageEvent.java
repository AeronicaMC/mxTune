package aeronicamc.mods.mxtune.caps.stages.event;

import aeronicamc.mods.mxtune.caps.stages.StageAreaData;
import aeronicamc.mods.mxtune.caps.stages.StageToolState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class StageEvent extends Event
{
    protected StageAreaData stageArea;
    protected LivingEntity livingEntity;
    protected StageToolState toolState;

    public StageEvent(StageAreaData stageArea, LivingEntity livingEntity, StageToolState toolState) {
        this.stageArea = stageArea;
        this.livingEntity = livingEntity;
        this.toolState = toolState;
    }

    public StageAreaData getStageArea()
    {
        return stageArea;
    }

    public Entity getLivingEntity()
    {
        return livingEntity;
    }

    public StageToolState getToolState()
    {
        return toolState;
    }


    @Cancelable
    public static class SelectPosition extends StageEvent
    {
        private final BlockPos blockPos;

        public SelectPosition(StageAreaData stageArea, LivingEntity livingEntity, BlockPos blockPos, StageToolState toolState)
        {
            super(stageArea, livingEntity, toolState);
            this.livingEntity = livingEntity;
            this.blockPos = blockPos;
            this.toolState = toolState;
        }

        public BlockPos getBlockPos()
        {
            return blockPos;
        }
    }

    @Cancelable
    public static class setName extends StageEvent
    {
        private final String name;

        public setName(StageAreaData stageArea, LivingEntity livingEntity, String name, StageToolState toolState)
        {
            super(stageArea, livingEntity, toolState);
            this.livingEntity = livingEntity;
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }
}
