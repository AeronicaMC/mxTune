package aeronicamc.mods.mxtune.caps.venues;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class MusicVenueTool
{
    final static Codec<MusicVenueTool> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    MusicVenue.CODEC.fieldOf("musicVenue").forGetter(MusicVenueTool::getMusicVenue),
                    ToolState.Type.CODEC.fieldOf("toolState").forGetter(MusicVenueTool::getToolState)
                                      ).apply(instance, MusicVenueTool::new));
    private MusicVenue musicVenue;
    private ToolState.Type toolState;

    public MusicVenueTool(MusicVenue musicVenue, ToolState.Type toolState)
    {
        this.musicVenue = musicVenue;
        this.toolState = toolState;
    }

    public MusicVenue getMusicVenue()
    {
        return musicVenue;
    }

    public void setMusicVenue(MusicVenue musicVenue)
    {
        this.musicVenue = musicVenue;
    }

    public ToolState.Type getToolState()
    {
        return toolState;
    }

    public void setToolState(ToolState.Type toolState)
    {
        this.toolState = toolState;
    }
}
