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
            Codec.STRING.fieldOf("name").forGetter(StageAreaData::getName),
            UUIDCodec.CODEC.fieldOf("ownerUUID").forGetter(StageAreaData::getOwnerUUID),
            Codec.FLOAT.fieldOf("r").forGetter(StageAreaData::getR),
            Codec.FLOAT.fieldOf("g").forGetter(StageAreaData::getG),
            Codec.FLOAT.fieldOf("b").forGetter(StageAreaData::getB)
            ).apply(instance, StageAreaData::new));

    private BlockPos startPos;
    private BlockPos endPos;
    private BlockPos performerSpawn;
    private BlockPos audienceSpawn;
    private String name;
    private UUID ownerUUID;
    private float r;
    private float g;
    private float b;

    private AxisAlignedBB areaAABB;
    public StageAreaData toolState;

    public StageAreaData(BlockPos startPos, BlockPos endPos, BlockPos performerSpawn, BlockPos audienceSpawn, String name, UUID ownerUUID, float r, float g, float b)
    {
        this.startPos = startPos;
        this.endPos = endPos;
        this.performerSpawn = performerSpawn;
        this.audienceSpawn = audienceSpawn;
        this.name = name;
        this.ownerUUID = ownerUUID;
        this.r = r;
        this.g = g;
        this.b = b;
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

    public void setName(String name)
    {
        this.name = name;
    }

    public void setOwnerUUID(UUID ownerUUID)
    {
        this.ownerUUID = ownerUUID;
    }

    public String getName()
    {
        return name;
    }

    public UUID getOwnerUUID()
    {
        return ownerUUID;
    }

    public float getR()
    {
        return r;
    }

    public float getG()
    {
        return g;
    }

    public float getB()
    {
        return b;
    }

    public StageAreaData getToolState()
    {
        return toolState;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(startPos)
                .append(endPos)
                .append(performerSpawn)
                .append(audienceSpawn)
                .append(name)
                .append(ownerUUID)
                .append(r)
                .append(g)
                .append(b)
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
                .append(name, stageAreaData.getName())
                .append(ownerUUID, stageAreaData.getOwnerUUID())
                .append(r, stageAreaData.getR())
                .append(g, stageAreaData.getG())
                .append(b, stageAreaData.getB())
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
                .append("name", name)
                .append("ownerUUID", ownerUUID)
                .append("r", r)
                .append("g", g)
                .append("b", b)
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
                .append(name, o.getName())
                .append(ownerUUID, o.getOwnerUUID())
                .toComparison();
    }
}
