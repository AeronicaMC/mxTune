package aeronicamc.mods.mxtune.entity;

import aeronicamc.mods.mxtune.init.ModEntities;
import aeronicamc.mods.mxtune.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class MusicVenueInfoEntity extends HangingEntity implements IEntityAdditionalSpawnData
{
    public static final String TAG_FACING = "Facing";
    public MusicVenueInfoEntity(World level)
    {
        super(ModEntities.MUSIC_VENUE_INFO.get(), level);
    }

    public MusicVenueInfoEntity(World level, BlockPos pPos, Direction facing)
    {
        super(ModEntities.MUSIC_VENUE_INFO.get(), level, pPos);
        this.setDirection(facing);
    }

    @Override
    public int getWidth()
    {
        return 16;
    }

    @Override
    public int getHeight()
    {
        return 16;
    }

    /**
     * Called when this entity is broken. Entity parameter may be null.
     *
     * @param pBrokenEntity
     */
    @Override
    public void dropItem(@Nullable Entity pBrokenEntity)
    {
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (pBrokenEntity instanceof PlayerEntity) {
                PlayerEntity playerentity = (PlayerEntity)pBrokenEntity;
                if (playerentity.abilities.instabuild) {
                    return;
                }
            }

            this.spawnAtLocation(ModItems.MUSIC_VENUE_INFO.get());
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void addAdditionalSaveData(CompoundNBT pCompound) {
        pCompound.putByte(TAG_FACING, (byte)this.direction.get2DDataValue());
        super.addAdditionalSaveData(pCompound);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundNBT pCompound) {
        this.direction = Direction.from2DDataValue(pCompound.getByte(TAG_FACING));
        super.readAdditionalSaveData(pCompound);
        this.setDirection(this.direction);
    }

    /**
     * Called by the server when constructing the spawn packet.
     * Data should be added to the provided stream.
     *
     * @param buffer The packet data stream
     */
    @Override
    public void writeSpawnData(PacketBuffer buffer)
    {
        buffer.writeBlockPos(this.pos);
        buffer.writeByte((byte)this.direction.get2DDataValue());
    }

    /**
     * Called by the client when it receives a Entity spawn packet.
     * Data should be read out of the stream in the same way as it was written.
     *
     * @param buffer The packet data stream
     */
    @Override
    public void readSpawnData(PacketBuffer buffer)
    {
        this.pos = buffer.readBlockPos();
        this.direction = Direction.from2DDataValue(buffer.readByte());
        this.setDirection(this.direction);
    }

    /**
     * Sets the location and rotation of the entity in the world.
     */
    @Override
    public void moveTo(double pX, double pY, double pZ, float pYRot, float pXRot) {
        this.setPos(pX, pY, pZ);
    }

    /**
     * Sets a target for the client to interpolate towards over the next few ticks
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pLerpSteps, boolean pTeleport) {
        BlockPos blockpos = this.pos.offset(pX - this.getX(), pY - this.getY(), pZ - this.getZ());
        this.setPos((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
    }

    /**
     * Checks if the entity is in range to render.
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = 16.0D;
        d0 = d0 * 64.0D * getViewScale();
        return distance < d0 * d0;
    }
}