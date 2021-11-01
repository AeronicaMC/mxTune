package aeronicamc.mods.mxtune.entity;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static aeronicamc.mods.mxtune.init.ModEntities.SITTABLE_ENTITY;

public class SittableEntity extends Entity
{
    private static final DataParameter<Boolean> SHOULD_SIT = EntityDataManager.defineId(SittableEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<BlockPos> BLOCK_POS = EntityDataManager.defineId(SittableEntity.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Integer> PLAY_ID = EntityDataManager.defineId(SittableEntity.class, DataSerializers.INT);
    private final BlockPos pos;

    public SittableEntity(EntityType<?> entityType, World level)
    {
        super(entityType, level);
        pos = BlockPos.ZERO;
        this.entityData.set(SHOULD_SIT, Boolean.TRUE);
        this.entityData.set(BLOCK_POS, pos);
    }

    public SittableEntity(World level, BlockPos blockPos, double yOffset, boolean shouldRiderSit)
    {
        super(SITTABLE_ENTITY.get(), level);
        pos = blockPos;
        setPos(pos.getX() + 0.5D, pos.getY() + yOffset, pos.getZ() + 0.5D);
        this.entityData.set(SHOULD_SIT, shouldRiderSit);
        this.entityData.set(BLOCK_POS, pos);
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(SHOULD_SIT, Boolean.TRUE);
        this.entityData.define(BLOCK_POS, pos);
        this.entityData.define(PLAY_ID, PlayIdSupplier.INVALID);
    }

    public void setPlayId(int playID)
    {
        entityData.set(PLAY_ID, playID);
    }

    public int getPlayId()
    {
        return entityData.get(PLAY_ID);
    }

    public BlockPos getBlockPos()
    {
        return (this.entityData.get(BLOCK_POS)).immutable();
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     *
     * @param pCompound
     */
    @Override
    protected void readAdditionalSaveData(CompoundNBT pCompound)
    {

    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT pCompound)
    {

    }

    @Nullable
    @Override
    public IPacket<?> getAddEntityPacket()
    {
        return null;
    }
}
