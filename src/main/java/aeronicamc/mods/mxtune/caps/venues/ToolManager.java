package aeronicamc.mods.mxtune.caps.venues;

import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.ToolManagerSyncMessage;
import com.mojang.serialization.Codec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ToolManager
{
    public static final Codec<Map<String, MusicVenueTool>> CODEC = Codec.unboundedMap(Codec.STRING, MusicVenueTool.CODEC);
    private final Map<String, MusicVenueTool> playerTools = new ConcurrentHashMap<>();

    public ToolManager() { /* NOP */ }

    private Optional<MusicVenueTool> getPlayerTool(LivingEntity livingEntity)
    {
        if (playerTools.containsKey(String.valueOf(livingEntity.getId())))
            return Optional.ofNullable(playerTools.get(String.valueOf(livingEntity.getId())));
        else
        {
            MusicVenueTool tool = MusicVenueTool.factory(livingEntity.getUUID());
            playerTools.put(String.valueOf(livingEntity.getId()), tool);
            return !livingEntity.level.isClientSide() ? Optional.of(tool) : Optional.empty();
        }
    }

    public void setPosition(LivingEntity livingEntity, ItemUseContext context)
    {
        if (livingEntity.level.isClientSide()) return;

        getPlayerTool(livingEntity).ifPresent(tool -> {
            EntityVenueState evs = MusicVenueHelper.getEntityVenueState(livingEntity.level, livingEntity.getId());
            if (evs.getVenue().getOwnerUUID().equals(livingEntity.getUUID()) && evs.inVenue())
            {
                tool.setToolState(ToolState.Type.REMOVE);
                sync(livingEntity);
            }
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
                    reset(livingEntity);
                    sync(livingEntity);
                    break;
                case REMOVE:
                    validate(livingEntity, context, tool).ifPresent(test-> {
                        if (evs.getVenue().getOwnerUUID().equals(livingEntity.getUUID()) && evs.inVenue())
                            MusicVenueProvider.getMusicVenues(livingEntity.level).ifPresent(
                                venues -> {
                                    venues.removeMusicVenue(evs.getVenue());
                                    venues.sync();
                                });
                            });
                    reset(livingEntity);
                    sync(livingEntity);
                    break;
                case DONE:
                    sync(livingEntity);
                default:
            }
        });
    }

    public void reset(LivingEntity livingEntity)
    {
        MusicVenueTool tool = MusicVenueTool.factory(livingEntity.getUUID());
        if(null == playerTools.replace(String.valueOf(livingEntity.getId()), tool))
            playerTools.put(String.valueOf(livingEntity.getId()), tool);
        sync(livingEntity);
    }

    private Optional<Boolean> validate(LivingEntity livingEntity, ItemUseContext context, MusicVenueTool tool)
    {
        return Optional.of(true); // TODO: validations and chat/overlay/tool messages/status
    }

    @Nullable
    public MusicVenueTool getTool(LivingEntity livingEntity)
    {
        return (playerTools.get(String.valueOf(livingEntity.getId())));
    }

    public Optional<MusicVenueTool> getToolOpl(LivingEntity livingEntity)
    {
        return Optional.ofNullable(getTool(livingEntity));
    }

    public void sync (LivingEntity livingEntity)
    {
        if (!livingEntity.level.isClientSide())
        {
            PacketDispatcher.sendToAll(new ToolManagerSyncMessage(serialize()));
        }
    }

    public INBT serialize()
    {
        CompoundNBT cNbt = new CompoundNBT();
        ListNBT listnbt = new ListNBT();
        NBTDynamicOps.INSTANCE.withEncoder(ToolManager.CODEC)
                .apply(Collections.unmodifiableMap(playerTools)).result().ifPresent(listnbt::add);
        cNbt.put("playerTools", listnbt);
        return cNbt;
    }

    public void deserialize(@Nullable INBT nbt)
    {
        CompoundNBT cNbt = ((CompoundNBT) nbt);
        if (cNbt != null && cNbt.contains("playerTools"))
        {
            ListNBT listnbt = cNbt.getList("playerTools", Constants.NBT.TAG_COMPOUND);
            playerTools.clear();
            listnbt.forEach(playerTool -> NBTDynamicOps.INSTANCE.withParser(ToolManager.CODEC)
                            .apply(playerTool).result().ifPresent(playerTools::putAll));
        }
    }
}
