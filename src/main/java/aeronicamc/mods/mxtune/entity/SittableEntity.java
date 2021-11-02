package aeronicamc.mods.mxtune.entity;

import aeronicamc.mods.mxtune.init.ModEntities;
import net.minecraft.block.AirBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
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
        this.noPhysics = true;
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
     * Used in model rendering to determine if the entity riding this entity should be in the 'sitting' position.
     *
     * @return false to prevent an entity that is mounted to this entity from displaying the 'sitting' animation.
     */
    @Override
    public boolean shouldRiderSit()
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
                seat.addPassenger(player);
            }
        }
        return ActionResult.pass(player.getItemInHand(hand));
    }

    public static boolean standOnBlock(World world, BlockPos pos, PlayerEntity playerIn, double yOffSet)
    {
        BlockPos underfoot = blockUnderFoot(playerIn);
        List<SittableEntity> sittableEntities = world.getEntitiesOfClass(SittableEntity.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0));
        if (sittableEntities.isEmpty() && (!(world.getBlockState(underfoot).getBlock() instanceof IFluidBlock)) && !(world.getBlockState(underfoot).getBlock() instanceof AirBlock))
        {
            VoxelShape voxelShape = world.getBlockState(underfoot).getShape(world, underfoot);
            double blockHeight = voxelShape.bounds().maxY;
            SittableEntity stand = new SittableEntity(world, underfoot, 0);
            world.addFreshEntity(stand);
            playerIn.startRiding(stand, true);
            //stand.addPassenger(playerIn);
            return true;
        }
        return false;
    }

    private static BlockPos blockUnderFoot(PlayerEntity playerIn)
    {
        int x = (int) Math.floor(playerIn.getX());
        int y = (int) Math.floor(playerIn.getY() - 0.6D);
        int z = (int) Math.floor(playerIn.getZ());
        return new BlockPos(x,y,z);
    }
}
