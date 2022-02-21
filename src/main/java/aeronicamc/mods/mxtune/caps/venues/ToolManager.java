package aeronicamc.mods.mxtune.caps.venues;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ToolManager implements INBTSerializable<INBT>
{
    private static final Map<Integer, MusicVenueTool> playerTools = new ConcurrentHashMap<>();

    private static Optional<MusicVenueTool> getPlayerTool(LivingEntity livingEntity)
    {
        if (playerTools.containsKey(livingEntity.getId()))
            return Optional.ofNullable(playerTools.get(livingEntity.getId()));
        else
        {
            MusicVenueTool tool = MusicVenueTool.factory(livingEntity.getUUID());
            playerTools.put(livingEntity.getId(), tool);
            return !livingEntity.level.isClientSide() ? Optional.of(tool) : Optional.empty();
        }
    }

    public static void setPosition(LivingEntity livingEntity, ItemUseContext context)
    {
        if (livingEntity.level.isClientSide()) return;

        getPlayerTool(livingEntity).ifPresent(tool -> {
            switch (tool.getToolState())
            {
                case START:
                    tool.getMusicVenue().setStartPos(context.getClickedPos());
                    validate(livingEntity, context, tool).ifPresent(test-> tool.setToolState(ToolState.Type.END));
                    sync(livingEntity);
                    break;
                case END:
                    tool.getMusicVenue().setEndPos(context.getClickedPos());
                    validate(livingEntity, context, tool).ifPresent(test-> {
                        tool.setToolState(ToolState.Type.DONE);
                        MusicVenueProvider.getMusicVenues(livingEntity.level).ifPresent(
                            venues -> {
                                venues.addMusicVenue(tool.getMusicVenue());
                                venues.sync();
                            });
                    });
                    sync(livingEntity);
                    break;
                case DONE:
                default:
                    reset(livingEntity);
            }
        });
    }

    public static void reset(LivingEntity livingEntity)
    {
        playerTools.remove(livingEntity.getId());
        sync(livingEntity);
    }

    private static Optional<Boolean> validate(LivingEntity livingEntity, ItemUseContext context, MusicVenueTool tool)
    {
        return Optional.of(true); // TODO: validations and chat/overlay/tool messages/status
    }

    private static void sync (LivingEntity livingEntity)
    {
        if (!livingEntity.level.isClientSide())
        {
            // TODO:
        }
    }

    @Override
    public INBT serializeNBT()
    {
        return null; // TODO:
    }

    @Override
    public void deserializeNBT(INBT nbt)
    {
        // TODO:
    }
}
