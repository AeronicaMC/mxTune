package aeronicamc.mods.mxtune.caps.venues;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class MusicVenueHelper
{
    private MusicVenueHelper() { /* */ }

    public static boolean inVenue(World level, int entityId)
    {
        MusicVenue[] activeVenue = new MusicVenue[1];
        activeVenue[0] = MusicVenue.EMPTY;
        Entity entity = level.getEntity(entityId);
        if (entity == null) return false;

        MusicVenueProvider.getMusicVenues(level).ifPresent(
                areas ->
                {
                    areas.getMusicVenues().stream().filter(v->v.getVenueAABB().contains(entity.getEyePosition(1F))).findFirst().ifPresent(area->{
                        activeVenue[0] = area;
                    });
                });
        return !activeVenue[0].equals(MusicVenue.EMPTY);
    }

    public static EntityVenueState getEntityVenueState(World level, int entityId)
    {
        MusicVenue[] activeVenue = new MusicVenue[1];
        activeVenue[0] = MusicVenue.EMPTY;
        Entity entity = level.getEntity(entityId);
        if (entity == null) return EntityVenueState.INVALID;

        MusicVenueProvider.getMusicVenues(level).ifPresent(
                areas ->
                {
                    areas.getMusicVenues().stream().filter(v->v.getVenueAABB().contains(entity.getEyePosition(1F))).findFirst().ifPresent(area->{
                        activeVenue[0] = area;
                    });
                });
        return new EntityVenueState(!activeVenue[0].equals(MusicVenue.EMPTY), activeVenue[0]);
    }

    public static EntityVenueState getBlockVenueState(World level, BlockPos blockPos)
    {
        MusicVenue[] activeVenue = new MusicVenue[1];
        activeVenue[0] = MusicVenue.EMPTY;
        Vector3d EntityPos = new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (!level.isLoaded(blockPos)) return EntityVenueState.INVALID;

        MusicVenueProvider.getMusicVenues(level).ifPresent(
                areas ->
                {
                    areas.getMusicVenues().stream().filter(v->v.getVenueAABB().contains(EntityPos)).findFirst().ifPresent(area->{
                        activeVenue[0] = area;
                    });
                });
        return new EntityVenueState(!activeVenue[0].equals(MusicVenue.EMPTY), activeVenue[0]);
    }
}
