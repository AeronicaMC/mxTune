package aeronicamc.mods.mxtune.entity;

import aeronicamc.mods.mxtune.init.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.List;

public class SittableEntity extends Entity
{
    private BlockPos source;

    public SittableEntity(World level)
    {
        super(ModEntities.SITTABLE_ENTITY.get(), level);
        this.noCulling = true;
    }

    public SittableEntity(World level, BlockPos source, double yOffset)
    {
        this(level);
        this.source = source;
        this.setPos(source.getX() + 0.5, source.getY() + yOffset, source.getZ() + 0.5);

    }

    @Override
    protected void defineSynchedData()
    {

    }

    public BlockPos getSource()
    {
        return source;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick()
    {
        super.tick();
        if(this.source == null)
        {
            this.source = this.blockPosition();
        }
        if(!this.level.isClientSide)
        {
            if(this.getPassengers().isEmpty() || this.level.isEmptyBlock(this.source))
            {
                this.remove();
                this.level.updateNeighbourForOutputSignal(blockPosition(), this.level.getBlockState(blockPosition()).getBlock());
            }
        }
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
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static ActionResult<ItemStack> create(@Nonnull World level, @Nonnull BlockPos pos, double yOffset, @Nonnull PlayerEntity player, @Nonnull Hand hand)
    {
        if(!level.isClientSide())
        {
            List<SittableEntity> sittableEntities = level.getEntitiesOfClass(SittableEntity.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0));
            if(sittableEntities.isEmpty())
            {
                SittableEntity seat = new SittableEntity(level, pos, yOffset);
                level.addFreshEntity(seat);
                player.startRiding(seat, false);
            }
        }
        return ActionResult.pass(player.getItemInHand(hand));
    }
}
