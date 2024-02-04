package aeronicamc.mods.mxtune.entity;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.IMusicPlayer;
import aeronicamc.mods.mxtune.init.ModEntities;
import aeronicamc.mods.mxtune.managers.PlayManager;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nullable;
import java.util.List;

public class MusicSourceEntity extends Entity {
    private BlockPos source;

    public MusicSourceEntity(World level) {
        super(ModEntities.MUSIC_SOURCE.get(), level);
        this.noCulling = true;
        this.noPhysics = true;
    }

    public MusicSourceEntity(World level, BlockPos source) {
        this(level);
        this.source = source;
        this.setPos(source.getX() + 0.5, source.getY() + 0.5, source.getZ() + 0.5);
        if (!level.isClientSide())
            ForgeChunkManager.forceChunk((ServerWorld) level, Reference.MOD_ID, this, level.getChunk(source).getPos().x, level.getChunk(source).getPos().z, true, true);
    }

    @Override
    protected void defineSynchedData() {
        /* Nothing to sync */
    }

    public BlockPos getSource()
    {
        return source;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        super.tick();

        // fix for saved entity music source, so they don't NPE on a tick.
        if (source == null) {
            source = this.blockPosition();
        }

        if(!level.isClientSide()) {
            boolean hasActiveTuneEntry = PlayManager.activeTuneEntityExists(this);
            if (!this.isAlive() || level.isEmptyBlock(source) || !(level.getBlockState(source).hasTileEntity() && level.getBlockEntity(source) instanceof IMusicPlayer) || !hasActiveTuneEntry)
            {
                this.remove();
                ForgeChunkManager.forceChunk((ServerWorld) level, Reference.MOD_ID, this, level.getChunk(source).getPos().x, level.getChunk(source).getPos().z, false, true);
                PlayManager.stopPlayingEntity(this);
                level.updateNeighbourForOutputSignal(blockPosition(), level.getBlockState(blockPosition()).getBlock());
            }
        }
    }

    /**
     * Returns the Y Offset of this entity.
     */
    @Override
    public double getMyRidingOffset() {
        return 0D;
    }

    @Override
    protected boolean canRide(Entity pEntity) {
        return false;
    }

    /**
     * Used in model rendering to determine if the entity riding this entity should be in the 'sitting' position.
     *
     * @return false to prevent an entity that is mounted to this entity from displaying the 'sitting' animation.
     */
    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT pCompound) {
        /* No additional save data */
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT pCompound) {
        /* No additional save data */
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // Not needed in this class.  Save for use elsewhere.
    @SuppressWarnings("unused")
    public static void standOnBlock(World world, BlockPos pos, PlayerEntity playerIn, double yOffSet, boolean shouldSit) {
        if (!world.isClientSide()) {
            BlockPos blockPosFeet = blockUnderFoot(playerIn);
            BlockState blockStateBelowFoot =  world.getBlockState(blockPosFeet);
            String className = blockStateBelowFoot.getBlock().getClass().getSimpleName();
            VoxelShape voxelShape = world.getBlockState(blockPosFeet).getShape(world, blockPosFeet);
            double blockHeight = !voxelShape.isEmpty() ? voxelShape.bounds().maxY : 0;

            List<MusicSourceEntity> sittableEntities = world.getEntitiesOfClass(MusicSourceEntity.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0));
            if (sittableEntities.isEmpty() && !(blockStateBelowFoot.getBlock() instanceof AirBlock || !blockStateBelowFoot.getFluidState().isEmpty())) {
                double ridingOffset = shouldSit ? -1 * 0.0625D : playerIn.getMyRidingOffset();
                MusicSourceEntity stand = new MusicSourceEntity(world, blockUnderFoot(playerIn));
                world.addFreshEntity(stand);
                playerIn.startRiding(stand, true);
            }
        }
    }

    private static BlockPos blockUnderFoot(PlayerEntity playerIn) {
        int x = (int) Math.floor(playerIn.getX());
        int y = (int) Math.floor(playerIn.getY() - 0.4);
        int z = (int) Math.floor(playerIn.getZ());
        return new BlockPos(x,y,z);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("source", source)
                .append("level", level)
                .toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        MusicSourceEntity musicSource = (MusicSourceEntity) o;
        return new EqualsBuilder()
                .append(source, musicSource)
                .append(level, musicSource.level)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(source)
                .append(level)
                .toHashCode();
    }
}
