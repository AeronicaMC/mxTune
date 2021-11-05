package aeronicamc.mods.mxtune.entity;

import aeronicamc.mods.mxtune.init.ModEntities;
import aeronicamc.mods.mxtune.managers.PlayManager;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class RootedEntity extends Entity
{
    private static final Logger LOGGER = LogManager.getLogger(RootedEntity.class);
    private static final DataParameter<Boolean> SHOULD_SIT = EntityDataManager.defineId(RootedEntity.class, DataSerializers.BOOLEAN);
    private BlockPos source;

    public RootedEntity(World level)
    {
        super(ModEntities.ROOTED_SOURCE.get(), level);
        this.noCulling = true;
        this.noPhysics = true;
        this.entityData.set(SHOULD_SIT, Boolean.TRUE);
    }

    public RootedEntity(World level, BlockPos source, boolean shouldSit)
    {
        this(level);
        this.source = source;
        this.setPos(source.getX() + 0.5, source.getY() + 0.5, source.getZ() + 0.5);
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

        if (source == null) // fix for saved entity music source so they don't NPE on a tick.
        {
            source = this.blockPosition();
        }

        if(!this.level.isClientSide())
        {
            boolean hasPlayId = PlayManager.hasActivePlayId(this);
            if (!this.isAlive() || this.level.isEmptyBlock(this.source) || !(this.hasOnePlayerPassenger()) || !hasPlayId)
            {
                if (PlayManager.hasActivePlayId(this))
                    PlayManager.stopPlayingEntity(this);
                LOGGER.debug("has playId: {}", hasPlayId);
                LOGGER.debug("{} removed from world.", this.getId());
                LOGGER.debug("{} @Block is Air: {}.", this.getId(), this.level.isEmptyBlock(this.source));
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

    public static void standOnBlock(World world, BlockPos pos, PlayerEntity playerIn, double yOffSet, boolean shouldSit)
    {
        if (!world.isClientSide())
        {
            BlockPos blockPosFeet = blockUnderFoot(playerIn);
            BlockState blockStateBelowFoot =  world.getBlockState(blockPosFeet);
            String className = blockStateBelowFoot.getBlock().getClass().getSimpleName();
            VoxelShape voxelShape = world.getBlockState(blockPosFeet).getShape(world, blockPosFeet);
            double blockHeight = !voxelShape.isEmpty() ? voxelShape.bounds().maxY : 0;
            LOGGER.debug("bpuf: {}, bstate: {}, bclass: {}, blockHeight: {}", blockPosFeet, blockStateBelowFoot, className, blockHeight);

            List<RootedEntity> rootedEntities = world.getEntitiesOfClass(RootedEntity.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0));
            if (rootedEntities.isEmpty() && !((blockStateBelowFoot.getBlock() instanceof AirBlock | !(blockStateBelowFoot.getFluidState().isEmpty()))))
            {
                double ridingOffset = shouldSit ? -1 * 0.0625D : playerIn.getMyRidingOffset();
                RootedEntity stand = new RootedEntity(world, blockUnderFoot(playerIn), shouldSit);
                world.addFreshEntity(stand);
                playerIn.startRiding(stand, true);
            }
        }
    }

    private static BlockPos blockUnderFoot(PlayerEntity playerIn)
    {
        int x = (int) Math.floor(playerIn.getX());
        int y = (int) Math.floor(playerIn.getY() - 0.4);
        int z = (int) Math.floor(playerIn.getZ());
        return new BlockPos(x,y,z);
    }
}
