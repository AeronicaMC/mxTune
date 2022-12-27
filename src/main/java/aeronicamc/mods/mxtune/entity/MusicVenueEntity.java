package aeronicamc.mods.mxtune.entity;

import aeronicamc.mods.mxtune.init.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class MusicVenueEntity extends HangingEntity implements IEntityAdditionalSpawnData
{
    public MusicVenueEntity(World level)
    {
        super(ModEntities.MUSIC_VENUE_INFO.get(), level);
    }

    public MusicVenueEntity(World level, BlockPos pPos)
    {
        super(ModEntities.MUSIC_VENUE_INFO.get(), level, pPos);
    }

    @Override
    public int getWidth()
    {
        return 0;
    }

    @Override
    public int getHeight()
    {
        return 0;
    }

    /**
     * Called when this entity is broken. Entity parameter may be null.
     *
     * @param pBrokenEntity
     */
    @Override
    public void dropItem(@Nullable Entity pBrokenEntity)
    {

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

    /**
     * Called by the server when constructing the spawn packet.
     * Data should be added to the provided stream.
     *
     * @param buffer The packet data stream
     */
    @Override
    public void writeSpawnData(PacketBuffer buffer)
    {

    }

    /**
     * Called by the client when it receives a Entity spawn packet.
     * Data should be read out of the stream in the same way as it was written.
     *
     * @param additionalData The packet data stream
     */
    @Override
    public void readSpawnData(PacketBuffer additionalData)
    {

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
