package aeronicamc.mods.mxtune.entity;

import aeronicamc.mods.mxtune.init.ModEntities;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public class SittableEntity extends Entity
{
    private static final Logger LOGGER = LogManager.getLogger(SittableEntity.class.getSimpleName());
    private static final DataParameter<Boolean> SHOULD_SIT = EntityDataManager.defineId(SittableEntity.class, DataSerializers.BOOLEAN);
    private BlockPos source;

    public SittableEntity(World level)
    {
        super(ModEntities.SITTABLE_ENTITY.get(), level);
        this.noCulling = true;
        this.noPhysics = true;
        this.entityData.set(SHOULD_SIT, Boolean.TRUE);
    }

    public SittableEntity(World level, BlockPos source, double yOffset, boolean shouldSit)
    {
        this(level);
        this.source = source;
        this.setPos(source.getX() + 0.5, source.getY() + yOffset, source.getZ() + 0.5);
        this.entityData.set(SHOULD_SIT, shouldSit);
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(SHOULD_SIT, Boolean.TRUE);
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
        return 0D;
    }

    @Override
    protected boolean canRide(Entity pEntity)
    {
        return true;
    }

    /**
     * Used in model rendering to determine if the entity riding this entity should be in the 'sitting' position.
     *
     * @return false to prevent an entity that is mounted to this entity from displaying the 'sitting' animation.
     */
    @Override
    public boolean shouldRiderSit()
    {
        return entityData.get(SHOULD_SIT);
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
                SittableEntity seat = new SittableEntity(level, pos, yOffset, true);
                level.addFreshEntity(seat);
                player.startRiding(seat, false);
            }
        }
        return ActionResult.pass(player.getItemInHand(hand));
    }

    public static boolean standOnBlock(World world, BlockPos pos, PlayerEntity playerIn, double yOffSet, boolean shouldSit)
    {
        if (!world.isClientSide())
        {
            BlockPos blockPosFeet = blockUnderFoot(playerIn);
            BlockState blockStateBelowFoot =  world.getBlockState(blockPosFeet);
            String className = blockStateBelowFoot.getBlock().getClass().getSimpleName();
            VoxelShape voxelShape = world.getBlockState(blockPosFeet).getShape(world, blockPosFeet);
            double blockHeight = !voxelShape.isEmpty() ? voxelShape.bounds().maxY : 0;
            LOGGER.debug("bpuf: {}, bstate: {}, bclass: {}, blockHeight: {}", blockPosFeet, blockStateBelowFoot, className, blockHeight);


            List<SittableEntity> sittableEntities = world.getEntitiesOfClass(SittableEntity.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0));
            if (sittableEntities.isEmpty() && !((blockStateBelowFoot.getBlock() instanceof AirBlock | !(blockStateBelowFoot.getFluidState().isEmpty()))))
            {
                double ridingOffset = shouldSit ? -1 * 0.0625D : playerIn.getMyRidingOffset();
                SittableEntity stand = new SittableEntity(world, blockUnderFoot(playerIn), blockHeight - ridingOffset, shouldSit);
                world.addFreshEntity(stand);
                playerIn.startRiding(stand, true);
                return true;
            }
        }
        return false;
    }

    private static BlockPos blockUnderFoot(PlayerEntity playerIn)
    {
        int x = (int) Math.floor(playerIn.getX());
        int y = (int) Math.floor(playerIn.getY() - 0.4);
        int z = (int) Math.floor(playerIn.getZ());
        return new BlockPos(x,y,z);
    }
}
