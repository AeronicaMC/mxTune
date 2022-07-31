package aeronicamc.mods.mxtune.caps.venues;

import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.ToolManagerSyncMessage;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ToolManager
{
    public static final Codec<Map<String, MusicVenueTool>> CODEC = Codec.unboundedMap(Codec.STRING, MusicVenueTool.CODEC);
    private static final Map<String, MusicVenueTool> playerTools = new ConcurrentHashMap<>();

    public ToolManager() { /* NOP */ }

    /**
     * Serverside use only
     * @param playerEntity instance
     * @return tool or empty optional depending on side
     */
    public static Optional<MusicVenueTool> getPlayerTool(PlayerEntity playerEntity)
    {
        if (playerTools.containsKey(String.valueOf(playerEntity.getId())))
            return Optional.ofNullable(playerTools.get(String.valueOf(playerEntity.getId())));
        else
        {
            MusicVenueTool tool = MusicVenueTool.factory(playerEntity.getUUID());
            playerTools.put(String.valueOf(playerEntity.getId()), tool);
            return !playerEntity.level.isClientSide() ? Optional.of(tool) : Optional.empty();
        }
    }

    public static void setPosition(PlayerEntity playerEntity, ItemUseContext context)
    {
        if (playerEntity.level.isClientSide()) return;

        getPlayerTool(playerEntity).ifPresent(tool -> {
            EntityVenueState evs = MusicVenueHelper.getEntityVenueState(playerEntity.level, playerEntity.getId());
            if (evs.getVenue().getOwnerUUID().equals(playerEntity.getUUID()) && evs.inVenue())
            {
                tool.setToolState(ToolState.Type.REMOVE);
                sync(playerEntity);
            }
            switch (tool.getToolState())
            {
                case START:
                    tool.getMusicVenue().setStartPos(context.getClickedPos());
                    validate(playerEntity, context, tool).filter(test -> test).ifPresent(test-> tool.setToolState(ToolState.Type.END));
                    sync(playerEntity);
                    break;
                case END:
                    tool.getMusicVenue().setEndPos(context.getClickedPos());
                    validate(playerEntity, context, tool).filter(test -> test).ifPresent(test -> {
                        tool.setToolState(ToolState.Type.DONE);
                        MusicVenueProvider.getMusicVenues(playerEntity.level).ifPresent(
                            venues -> {
                                venues.addMusicVenue(tool.getMusicVenue());
                                venues.sync();
                            });
                    });
                    reset(playerEntity);
                    sync(playerEntity);
                    break;
                case REMOVE:
                    validate(playerEntity, context, tool).ifPresent(test -> {
                        if (test && evs.getVenue().getOwnerUUID().equals(playerEntity.getUUID()) && evs.inVenue())
                            MusicVenueProvider.getMusicVenues(playerEntity.level).ifPresent(
                                venues -> {
                                    venues.removeMusicVenue(evs.getVenue());
                                    venues.sync();
                                });
                            });
                    reset(playerEntity);
                    sync(playerEntity);
                    break;
                case DONE:
                    sync(playerEntity);
                default:
            }
        });
    }

    public static void reset(PlayerEntity playerEntity)
    {
        MusicVenueTool tool = MusicVenueTool.factory(playerEntity.getUUID());
        if(null == playerTools.replace(String.valueOf(playerEntity.getId()), tool))
            playerTools.put(String.valueOf(playerEntity.getId()), tool);
        sync(playerEntity);
    }

    private static Optional<Boolean> validate(PlayerEntity playerEntity, ItemUseContext context, MusicVenueTool tool)
    {
        switch (tool.getToolState())
        {
            case START:
            case END:
                if(MusicVenueHelper.getBlockVenueState(context.getLevel(), context.getClickedPos()).inVenue())
                {
                    playerEntity.displayClientMessage(new TranslationTextComponent("message.mxtune.existing_venue_error"), false);
                    tool.setToolState(ToolState.Type.START);
                    return Optional.of(false);
                } else
                    return Optional.of(true);
            case REMOVE:
                EntityVenueState bvs = MusicVenueHelper.getBlockVenueState(context.getLevel(), context.getClickedPos());
                EntityVenueState pvs = MusicVenueHelper.getEntityVenueState(context.getLevel(), context.getPlayer().getId());

                if (bvs.inVenue() && pvs.inVenue() && pvs.equals(bvs) &&
                        (pvs.getVenue().getOwnerUUID().equals(context.getPlayer().getUUID()) ||
                                 context.getPlayer().isCreative() || (context.getLevel().getServer() != null &&
                                          context.getLevel().getServer().getPlayerList().isOp(context.getPlayer().getGameProfile()))))
                    return Optional.of(true);
                else
                {
                    playerEntity.displayClientMessage(new TranslationTextComponent("message.mxtune.not_owner_of_venue"), false);
                    tool.setToolState(ToolState.Type.DONE);
                    return Optional.of(false);
                }
            case DONE:
                break;
            default:
        }
        return Optional.of(false); // TODO: validations and chat/overlay/tool messages/status
    }

    @Nullable
    public static MusicVenueTool getTool(PlayerEntity playerEntity)
    {
        return (playerTools.get(String.valueOf(playerEntity.getId())));
    }

    public static Optional<MusicVenueTool> getToolOpl(PlayerEntity playerEntity)
    {
        return Optional.ofNullable(getTool(playerEntity));
    }

    public static void sync (PlayerEntity playerEntity)
    {
        if (!playerEntity.level.isClientSide())
        {
            PacketDispatcher.sendToAll(new ToolManagerSyncMessage(serialize()));
        }
    }

    public static INBT serialize()
    {
        CompoundNBT cNbt = new CompoundNBT();
        ListNBT listnbt = new ListNBT();
        NBTDynamicOps.INSTANCE.withEncoder(ToolManager.CODEC)
                .apply(Collections.unmodifiableMap(playerTools)).result().ifPresent(listnbt::add);
        cNbt.put("playerTools", listnbt);
        return cNbt;
    }

    public static void deserialize(@Nullable INBT nbt)
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
