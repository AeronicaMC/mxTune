package aeronicamc.mods.mxtune.caps.venues;

import aeronicamc.mods.mxtune.util.Color3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UUIDCodec;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.builder.*;

import javax.annotation.Nullable;
import java.util.UUID;

public class MusicVenue implements Comparable<MusicVenue> {
    public static final BlockPos OUT_OF_BOUNDS = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    public static final MusicVenue EMPTY = MusicVenue.factory(UUID.fromString("00000000-0000-0000-0000-000000000000"),"-999999999-01-01T00:00:00");

    static final Codec<MusicVenue> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                BlockPos.CODEC.fieldOf("startPos").forGetter(MusicVenue::getStartPos),
                BlockPos.CODEC.fieldOf("endPos").forGetter(MusicVenue::getEndPos),
                BlockPos.CODEC.fieldOf("performerSpawn").forGetter(MusicVenue::getPerformerSpawn),
                BlockPos.CODEC.fieldOf("audienceSpawn").forGetter(MusicVenue::getAudienceSpawn),
                Codec.STRING.fieldOf("name").forGetter(MusicVenue::getName),
                UUIDCodec.CODEC.fieldOf("ownerUUID").forGetter(MusicVenue::getOwnerUUID),
                Codec.STRING.fieldOf("id").forGetter(MusicVenue::getId),
                Codec.FLOAT.fieldOf("r").forGetter(MusicVenue::getR),
                Codec.FLOAT.fieldOf("g").forGetter(MusicVenue::getG),
                Codec.FLOAT.fieldOf("b").forGetter(MusicVenue::getB)
                                      ).apply(instance, MusicVenue::new));

    private BlockPos startPos;
    private BlockPos endPos;
    private BlockPos performerSpawn;
    private BlockPos audienceSpawn;
    private final String id;
    private String name;
    private UUID ownerUUID;
    private final float r;
    private final float g;
    private final float b;

    private AxisAlignedBB venueAABB;

    public MusicVenue(BlockPos startPos, BlockPos endPos, BlockPos performerSpawn, BlockPos audienceSpawn, String name, UUID ownerUUID, String id, float r, float g, float b) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.performerSpawn = performerSpawn;
        this.audienceSpawn = audienceSpawn;
        this.name = name;
        this.ownerUUID = ownerUUID;
        this.id = id;
        this.r = r;
        this.g = g;
        this.b = b;
        makeAABB();
    }

    private void makeAABB() {
        this.venueAABB = new AxisAlignedBB(this.startPos, this.endPos).inflate(0.5).move(0.5, 0.5, 0.5);
    }

    public BlockPos getStartPos() {
        return startPos;
    }

    public void setStartPos(BlockPos startPos) {
        this.startPos = startPos;
        makeAABB();
    }

    public BlockPos getEndPos() {
        return endPos;
    }

    public void setEndPos(BlockPos endPos) {
        this.endPos = endPos;
        makeAABB();
    }

    public BlockPos getPerformerSpawn() {
        return performerSpawn;
    }

    public void setPerformerSpawn(BlockPos performerSpawn) {
        this.performerSpawn = performerSpawn;
    }

    public BlockPos getAudienceSpawn() {
        return audienceSpawn;
    }

    public void setAudienceSpawn(BlockPos audienceSpawn) {
        this.audienceSpawn = audienceSpawn;
    }

    public AxisAlignedBB getVenueAABB() {
        return venueAABB;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public String getId() {
        return id;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public static MusicVenue factory(UUID uuid, String id) {
        Color3f rainbow = Color3f.rainbowFactory();
        return new MusicVenue(OUT_OF_BOUNDS, OUT_OF_BOUNDS, OUT_OF_BOUNDS, OUT_OF_BOUNDS, "", uuid, id, rainbow.getR(), rainbow.getG(), rainbow.getB());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(startPos)
                .append(endPos)
                .append(performerSpawn)
                .append(audienceSpawn)
                .append(name)
                .append(ownerUUID)
                .append(id)
                .append(r)
                .append(g)
                .append(b)
                .toHashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MusicVenue musicVenue = (MusicVenue) o;
        return new EqualsBuilder()
                .append(startPos, musicVenue.getStartPos())
                .append(endPos, musicVenue.getEndPos())
                .append(performerSpawn, musicVenue.getPerformerSpawn())
                .append(audienceSpawn, musicVenue.getAudienceSpawn())
                .append(name, musicVenue.getName())
                .append(ownerUUID, musicVenue.getOwnerUUID())
                .append(id, musicVenue.getId())
                .append(r, musicVenue.getR())
                .append(g, musicVenue.getG())
                .append(b, musicVenue.getB())
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("startPos", startPos)
                .append("endPos", endPos)
                .append("performerSpawn", performerSpawn)
                .append("audienceSpawn", audienceSpawn)
                .append("name", name)
                .append("ownerUUID", ownerUUID)
                .append("id",id)
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
    public int compareTo(MusicVenue o) {
        return new CompareToBuilder()
                .append(name, o.getName())
                .append(ownerUUID, o.getOwnerUUID())
                .toComparison();
    }
}
