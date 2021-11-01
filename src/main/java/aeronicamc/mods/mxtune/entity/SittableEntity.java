package aeronicamc.mods.mxtune.entity;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static aeronicamc.mods.mxtune.init.ModEntities.SITTABLE_ENTITY;

public class SittableEntity extends Entity
{
    private static final DataParameter<Boolean> SHOULD_SIT = EntityDataManager.defineId(SittableEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<BlockPos> BLOCK_POS = EntityDataManager.defineId(SittableEntity.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Integer> PLAY_ID = EntityDataManager.defineId(SittableEntity.class, DataSerializers.INT);
    private BlockPos pos;

    public SittableEntity(final EntityType<? extends SittableEntity> entityType, World level)
    {
        super(entityType, level);
        pos = BlockPos.ZERO;
        this.entityData.set(SHOULD_SIT, Boolean.TRUE);
        this.entityData.set(BLOCK_POS, pos);
    }

    public SittableEntity(final World level, BlockPos blockPos, double yOffset, boolean shouldRiderSit)
    {
        super(SITTABLE_ENTITY.get(), level);
        pos = blockPos;
        setPos(pos.getX() + 0.5D, pos.getY() + yOffset, pos.getZ() + 0.5D);
        this.entityData.set(SHOULD_SIT, shouldRiderSit);
        this.entityData.set(BLOCK_POS, pos);
        this.noCulling = true;
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
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick()
    {
        super.tick();
        if(pos == null)
        {
            pos = this.getBlockPos();
        }
        if(!this.level.isClientSide())
        {
            if(this.getPassengers().isEmpty() || this.level.isEmptyBlock(pos))
            {
                this.remove();
                level.updateNeighborsAt(getBlockPos(), level.getBlockState(getBlockPos()).getBlock());
            }
        }
    }

    /**
     * Handles updating while riding another entity
     */
    @Override
    public void rideTick()
    {
        super.rideTick();
    }

    /**
     * Returns the Y Offset of this entity.
     */
    @Override
    public double getMyRidingOffset()
    {
        return 0.0D;
    }

    @Override
    protected boolean canRide(Entity pEntity)
    {
        return true;
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

    @Override
    public IPacket<?> getAddEntityPacket()
    {
        return new SSpawnObjectPacket(this);
    }
}
