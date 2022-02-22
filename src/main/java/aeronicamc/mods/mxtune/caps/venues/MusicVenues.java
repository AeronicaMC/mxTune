package aeronicamc.mods.mxtune.caps.venues;

import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.MusicVenueSyncMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicVenues implements IMusicVenues
{
    private static final Logger LOGGER = LogManager.getLogger(MusicVenues.class);
    private World level;
    private final List<MusicVenue> musicVenues = new CopyOnWriteArrayList<>();

    private final ToolManager toolManager = new ToolManager();
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
    public ToolManager getToolManager()
    {
        return toolManager;
    }

    @Override
    public void addMusicVenue(MusicVenue musicVenue)
    {
        musicVenues.add(musicVenue);
    }

    @Override
    public List<MusicVenue> getMusicVenues()
    {
        return musicVenues;
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
        getLevel().ifPresent(level -> {
            if (!level.isClientSide())
            {
                PacketDispatcher.sendToDimension(new MusicVenueSyncMessage(serializeNBT()), level.dimension());
                LOGGER.debug("sync: someInt {}, musicVenues {}", this.someInt, this.musicVenues.size());
            }
        });
    }

    @Override
    public INBT serializeNBT()
    {
        final CompoundNBT cNbt = new CompoundNBT();
        ListNBT listnbt = new ListNBT();
        musicVenues.forEach(stageArea -> NBTDynamicOps.INSTANCE.withEncoder(MusicVenue.CODEC)
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
            musicVenues.clear();
            listnbt.forEach(stageAreaNBT -> NBTDynamicOps.INSTANCE.withParser(MusicVenue.CODEC)
                    .apply(stageAreaNBT).result().ifPresent(musicVenues::add));

            setInt(cNbt.getInt("someInt"));
        }
    }
}
