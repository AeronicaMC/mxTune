package aeronicamc.mods.mxtune.entity;

import aeronicamc.mods.mxtune.init.ModEntities;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.render.entity.InfoRenderer;
import aeronicamc.mods.mxtune.util.InfoPanelType;
import aeronicamc.mods.mxtune.util.MXRegistry;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class MusicVenueInfoEntity extends HangingEntity implements IEntityAdditionalSpawnData {
    public static final String TAG_PANEL = "Panel";
    public static final String TAG_FACING = "Facing";
    private InfoPanelType infoPanelType;
    private boolean newPanel = false;

    public MusicVenueInfoEntity(World level) {
        super(ModEntities.MUSIC_VENUE_INFO.get(), level);
    }

    public MusicVenueInfoEntity(World level, BlockPos pPos, Direction facing)
    {
        super(ModEntities.MUSIC_VENUE_INFO.get(), level, pPos);
        List<InfoPanelType> list = Lists.newArrayList();
        int i = 0;
        this.canUpdate(true);

        for(InfoPanelType infoPanel : MXRegistry.INFO_PANEL_REGISTRY.get().getValues()) {
            this.infoPanelType = infoPanel;
            this.setDirection(facing);
            if (this.survives()) {
                list.add(infoPanel);
                int j = infoPanel.getWidth() * infoPanel.getHeight();
                if (j > i)
                {
                    i = j;
                }
            }
        }

        if (!list.isEmpty()) {
            Iterator<InfoPanelType> iterator = list.iterator();

            while(iterator.hasNext()) {
                InfoPanelType panelType = iterator.next();
                if (panelType.getWidth() * panelType.getHeight() < i) {
                    iterator.remove();
                }
            }
            this.infoPanelType = list.get(Math.max(list.size() - 1, 0));
        }
        this.setDirection(facing);
        this.newPanel = true;
    }

    @OnlyIn(Dist.CLIENT)
    public MusicVenueInfoEntity(World pLevel, PacketBuffer buffer) {
        this(pLevel);
        this.writeSpawnData(buffer);
    }

    @Override
    public int getWidth() {
        return this.infoPanelType == null ? 1 : this.infoPanelType.getWidth();
    }

    @Override
    public int getHeight() {
        return this.infoPanelType == null ? 1 : this.infoPanelType.getHeight();
    }

    /**
     * Called when this entity is broken. Entity parameter may be null.
     * @param pBrokenEntity the broken entity.
     */
    @Override
    public void dropItem(@Nullable Entity pBrokenEntity) {
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
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void addAdditionalSaveData(CompoundNBT pCompound) {
        pCompound.putString(TAG_PANEL, String.valueOf(MXRegistry.INFO_PANEL_REGISTRY.get().getKey(infoPanelType)));
        pCompound.putByte(TAG_FACING, (byte)this.direction.get2DDataValue());
        super.addAdditionalSaveData(pCompound);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundNBT pCompound) {
        this.infoPanelType = MXRegistry.INFO_PANEL_REGISTRY.get().getValue(ResourceLocation.tryParse(pCompound.getString(TAG_PANEL)));
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
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeByte((byte)this.direction.get2DDataValue());
        buffer.writeUtf(String.valueOf(MXRegistry.INFO_PANEL_REGISTRY.get().getKey(infoPanelType)));
        buffer.writeBoolean(this.newPanel);
    }

    /**
     * Called by the client when it receives an Entity spawn packet.
     * Data should be read out of the stream in the same way as it was written.
     *
     * @param buffer The packet data stream
     */
    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
        this.direction = Direction.from2DDataValue(buffer.readByte());
        this.infoPanelType = MXRegistry.INFO_PANEL_REGISTRY.get().getValue(ResourceLocation.tryParse(buffer.readUtf()));
        this.newPanel = buffer.readBoolean();
        this.setDirection(this.direction);
    }

    public boolean isNewPanel()
    {
        return newPanel;
    }

    @Override
    public void onRemovedFromWorld() {
        if (this.level.isClientSide) {
            InfoRenderer.getInstance().close(this);
        }
        super.onRemovedFromWorld();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        if(this.level.isClientSide) {
            InfoRenderer.getInstance().updateInfoTexture(this);
        }
        super.tick();
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
        this.setPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
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

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MusicVenueInfoEntity infoEntity = (MusicVenueInfoEntity) o;
        return new EqualsBuilder()
                .append(infoPanelType, infoEntity.infoPanelType)
                .append(level, infoEntity.level)
                .append(pos, infoEntity.pos)
                .append(direction, infoEntity.direction)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(infoPanelType)
                .append(level)
                .append(pos)
                .append(direction)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("infoPanelType", infoPanelType)
                .append("level", level)
                .append("pos", pos)
                .append("direction", direction)
                .toString();
    }
}
