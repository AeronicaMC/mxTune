package aeronicamc.mods.mxtune.caps.venues;

import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.MusicVenueSyncMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicVenues implements IMusicVenues
{
    private World level;
    private final List<MusicVenue> venueList = new CopyOnWriteArrayList<>();

    private int someInt;

    MusicVenues() { /* NOP */ }

    MusicVenues(World level)
    {
        this();
        this.level = level;
    }

    private Optional<World> getLevel()
    {
        return Optional.of(level);
    }

    @Override
    public void addMusicVenue(MusicVenue musicVenue)
    {
        venueList.add(musicVenue);
    }

    @Override
    public boolean removeMusicVenue(MusicVenue musicVenue)
    {
        return venueList.remove(musicVenue);
    }

    @Override
    public List<MusicVenue> getVenueList()
    {
        return venueList;
    }

    @Override
    public int getInt()
    {
        return someInt;
    }

    @Override
    public void setInt(Integer someInt)
    {
        this.someInt = someInt;
        sync();
    }

    public void sync()
    {
        getLevel().ifPresent(world -> {
            if (!world.isClientSide())
            {
                PacketDispatcher.sendToDimension(new MusicVenueSyncMessage(serializeNBT()), world.dimension());
            }
        });
    }

    @Override
    public INBT serializeNBT()
    {
        final CompoundNBT cNbt = new CompoundNBT();
        ListNBT listnbt = new ListNBT();
        venueList.forEach(stageArea -> NBTDynamicOps.INSTANCE.withEncoder(MusicVenue.CODEC)
                .apply(stageArea).result().ifPresent(listnbt::add));

        cNbt.put("musicVenues", listnbt);
        cNbt.putInt("someInt", getInt());
        return cNbt;
    }

    @Override
    public void deserializeNBT(@Nullable INBT nbt)
    {
        CompoundNBT cNbt = ((CompoundNBT) nbt);
        if (cNbt != null && cNbt.contains("musicVenues"))
        {
            ListNBT listnbt = cNbt.getList("musicVenues", Constants.NBT.TAG_COMPOUND);
            venueList.clear();
            listnbt.forEach(stageAreaNBT -> NBTDynamicOps.INSTANCE.withParser(MusicVenue.CODEC)
                    .apply(stageAreaNBT).result().ifPresent(venueList::add));

            setInt(cNbt.getInt("someInt"));
        }
    }
}
