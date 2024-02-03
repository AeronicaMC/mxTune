package aeronicamc.mods.mxtune.caps.venues;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class MusicVenueTool
{
    private static final ZoneId ROOT_ZONE = ZoneId.of("GMT0");
    private static LocalDateTime lastDateTime = LocalDateTime.now(ROOT_ZONE);

    static final Codec<MusicVenueTool> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    MusicVenue.CODEC.fieldOf("musicVenue").forGetter(MusicVenueTool::getMusicVenue),
                    ToolState.Type.CODEC.fieldOf("toolState").forGetter(MusicVenueTool::getToolState)
                                      ).apply(instance, MusicVenueTool::new));
    private final MusicVenue musicVenue;
    private ToolState.Type toolState;
    private int slot;

    public MusicVenueTool(MusicVenue musicVenue, ToolState.Type toolState)
    {
        this.musicVenue = musicVenue;
        this.toolState = toolState;
    }

    public MusicVenue getMusicVenue()
    {
        return musicVenue;
    }

    public ToolState.Type getToolState()
    {
        return toolState;
    }

    public void setToolState(ToolState.Type toolState)
    {
        this.toolState = toolState;
    }

    private static LocalDateTime nextKey()
    {
        LocalDateTime now;
        do {
            now = LocalDateTime.now(ROOT_ZONE);
        } while (now.equals(lastDateTime));
        lastDateTime = now;
        return now;
    }

    public int getSlot()
    {
        return slot;
    }

    public void setSlot(int slot)
    {
        this.slot = slot;
    }

    public static MusicVenueTool factory(UUID uuid)
    {
        return new MusicVenueTool(MusicVenue.factory(uuid, nextKey().toString()), ToolState.Type.START);
    }
}
