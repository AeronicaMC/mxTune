package aeronicamc.mods.mxtune.caps.stages;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.UUIDCodec;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.UUID;

public class StageAreaData
{
    final static Codec<StageAreaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            World.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(StageAreaData::getDimension),
            BlockPos.CODEC.fieldOf("startPos").forGetter(StageAreaData::getStartPos),
            BlockPos.CODEC.fieldOf("endPos").forGetter(StageAreaData::getEndPos),
            BlockPos.CODEC.fieldOf("performerSpawn").forGetter(StageAreaData::getPerformerSpawn),
            BlockPos.CODEC.fieldOf("audienceSpawn").forGetter(StageAreaData::getAudienceSpawn),
            Codec.STRING.fieldOf("title").forGetter(StageAreaData::getTitle),
            UUIDCodec.CODEC.fieldOf("ownerUUID").forGetter(StageAreaData::getOwnerUUID)
            ).apply(instance, StageAreaData::new));

    final RegistryKey<World> dimension;
    final BlockPos startPos;
    final BlockPos endPos;
    final BlockPos performerSpawn;
    final BlockPos audienceSpawn;
    final String title;
    final UUID ownerUUID;
    final AxisAlignedBB areaAABB;

    StageAreaData(final RegistryKey<World> dimension, final BlockPos startPos, final BlockPos endPos, final BlockPos performerSpawn, final BlockPos audienceSpawn, final String title, final UUID ownerUUID)
    {
        this.dimension = dimension;
        this.startPos = startPos;
        this.endPos = endPos;
        this.performerSpawn = performerSpawn;
        this.audienceSpawn = audienceSpawn;
        this.title = title;
        this.ownerUUID = ownerUUID;
        this.areaAABB = new AxisAlignedBB(this.startPos, this.endPos).inflate(0.5).move(0.5,0.5,0.5);
    }

    public RegistryKey<World> getDimension()
    {
        return dimension;
    }

    public BlockPos getStartPos()
    {
        return startPos;
    }

    public BlockPos getEndPos()
    {
        return endPos;
    }

    public BlockPos getPerformerSpawn()
    {
        return performerSpawn;
    }

    public BlockPos getAudienceSpawn()
    {
        return audienceSpawn;
    }

    public AxisAlignedBB getAreaAABB()
    {
        return areaAABB;
    }

    public String getTitle()
    {
        return title;
    }

    public UUID getOwnerUUID()
    {
        return ownerUUID;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(dimension)
                .append(startPos)
                .append(endPos)
                .append(performerSpawn)
                .append(audienceSpawn)
                .append(title)
                .append(ownerUUID)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StageAreaData stageAreaData = (StageAreaData) o;
        return new EqualsBuilder()
                .append(dimension, stageAreaData.getDimension())
                .append(startPos, stageAreaData.getStartPos())
                .append(endPos, stageAreaData.getEndPos())
                .append(performerSpawn, stageAreaData.getPerformerSpawn())
                .append(audienceSpawn, stageAreaData.getAudienceSpawn())
                .append(title, stageAreaData.getTitle())
                .append(ownerUUID, stageAreaData.getOwnerUUID())
                .isEquals();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("dimension", dimension)
                .append("startPos", startPos)
                .append("endPos", endPos)
                .append("performerSpawn", performerSpawn)
                .append("audienceSpawn", audienceSpawn)
                .append("title", title)
                .append("ownerUUID", ownerUUID)
                .toString();
    }
}
