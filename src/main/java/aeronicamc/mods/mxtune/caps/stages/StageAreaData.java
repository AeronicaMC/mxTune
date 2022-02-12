package aeronicamc.mods.mxtune.caps.stages;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UUIDCodec;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.builder.*;

import java.util.UUID;

public class StageAreaData implements Comparable<StageAreaData>
{
    final static Codec<StageAreaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("startPos").forGetter(StageAreaData::getStartPos),
            BlockPos.CODEC.fieldOf("endPos").forGetter(StageAreaData::getEndPos),
            BlockPos.CODEC.fieldOf("performerSpawn").forGetter(StageAreaData::getPerformerSpawn),
            BlockPos.CODEC.fieldOf("audienceSpawn").forGetter(StageAreaData::getAudienceSpawn),
            Codec.STRING.fieldOf("title").forGetter(StageAreaData::getTitle),
            UUIDCodec.CODEC.fieldOf("ownerUUID").forGetter(StageAreaData::getOwnerUUID)
            ).apply(instance, StageAreaData::new));

    private BlockPos startPos;
    private BlockPos endPos;
    private BlockPos performerSpawn;
    private BlockPos audienceSpawn;
    private String title;
    private UUID ownerUUID;
    private AxisAlignedBB areaAABB;

    public StageAreaData(BlockPos startPos, BlockPos endPos, BlockPos performerSpawn, BlockPos audienceSpawn, String title, UUID ownerUUID)
    {
        this.startPos = startPos;
        this.endPos = endPos;
        this.performerSpawn = performerSpawn;
        this.audienceSpawn = audienceSpawn;
        this.title = title;
        this.ownerUUID = ownerUUID;
        makeAABB();
    }

    private void makeAABB()
    {
        this.areaAABB = new AxisAlignedBB(this.startPos, this.endPos).inflate(0.5).move(0.5,0.5,0.5);
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

    public void setStartPos(BlockPos startPos)
    {
        this.startPos = startPos;
        makeAABB();
    }

    public void setEndPos(BlockPos endPos)
    {
        this.endPos = endPos;
        makeAABB();
    }

    public void setPerformerSpawn(BlockPos performerSpawn)
    {
        this.performerSpawn = performerSpawn;
    }

    public void setAudienceSpawn(BlockPos audienceSpawn)
    {
        this.audienceSpawn = audienceSpawn;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setOwnerUUID(UUID ownerUUID)
    {
        this.ownerUUID = ownerUUID;
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
                .append("startPos", startPos)
                .append("endPos", endPos)
                .append("performerSpawn", performerSpawn)
                .append("audienceSpawn", audienceSpawn)
                .append("title", title)
                .append("ownerUUID", ownerUUID)
                .toString();
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p></p>
     * {@link Comparable}
     * <p></p>
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(StageAreaData o)
    {
        return new CompareToBuilder()
                .append(title, o.getTitle())
                .append(ownerUUID, o.getOwnerUUID())
                .toComparison();
    }
}
