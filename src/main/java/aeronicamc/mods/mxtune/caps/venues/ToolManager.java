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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
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

    private ToolManager() { /* NOP */ }

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

    public static void apply(ItemUseContext context)
    {
        PlayerEntity player = context.getPlayer();
        if (player == null || context.getLevel().isClientSide()) return;

        getPlayerTool(player).ifPresent(tool -> {
            EntityVenueState pvs = MusicVenueHelper.getEntityVenueState(player.level, player.getId());
            if (pvs.getVenue().getOwnerUUID().equals(player.getUUID()) && pvs.inVenue())
            {
                tool.setToolState(ToolState.Type.REMOVE);
                sync(player);
            }
            switch (tool.getToolState())
            {
                case START:
                    tool.getMusicVenue().setStartPos(context.getClickedPos());
                    validate(player, context, tool).filter(test -> test).ifPresent(test-> tool.setToolState(ToolState.Type.END));
                    sync(player);
                    break;
                case END:
                    tool.getMusicVenue().setEndPos(context.getClickedPos());
                    validate(player, context, tool).filter(test -> test).ifPresent(test -> {
                        tool.setToolState(ToolState.Type.DONE);
                        MusicVenueProvider.getMusicVenues(player.level).ifPresent(
                            venues -> {
                                venues.addMusicVenue(tool.getMusicVenue());
                                venues.sync();
                            });
                    });
                    reset(player);
                    sync(player);
                    break;
                case REMOVE:
                    validate(player, context, tool).ifPresent(test -> {
                        if (test)
                            MusicVenueProvider.getMusicVenues(player.level).ifPresent(
                                venues -> {
                                    venues.removeMusicVenue(pvs.getVenue());
                                    venues.sync();
                                });
                            });
                    reset(player);
                    sync(player);
                    break;
                case DONE:
                    sync(player);
                    break;
                default:
            }
        });
    }

    public static void reset(PlayerEntity player)
    {
        MusicVenueTool tool = MusicVenueTool.factory(player.getUUID());
        if(null == playerTools.replace(String.valueOf(player.getId()), tool))
            playerTools.put(String.valueOf(player.getId()), tool);
        sync(player);
    }

    private static Optional<Boolean> validate(PlayerEntity player, ItemUseContext context, MusicVenueTool tool)
    {
        EntityVenueState pvs = MusicVenueHelper.getEntityVenueState(context.getLevel(), player.getId());
        switch (tool.getToolState())
        {
            case START:
            case END:
                if((tool.getToolState().equals(ToolState.Type.END) || tool.getToolState().equals(ToolState.Type.START)) && MusicVenueHelper.getBlockVenueState(context.getLevel(), context.getClickedPos()).inVenue())
                {
                    player.displayClientMessage(new TranslationTextComponent("message.mxtune.existing_venue_error").withStyle(TextFormatting.BOLD), true);
                    tool.setToolState(ToolState.Type.START);
                    return Optional.of(false);
                } else if (tool.getToolState().equals(ToolState.Type.END) && notIntersects(context, tool.getMusicVenue().getStartPos(), context.getClickedPos()))
                {
                    player.displayClientMessage(new TranslationTextComponent("message.mxtune.intersects_venue").withStyle(TextFormatting.BOLD), true);
                    tool.setToolState(ToolState.Type.START);
                    return Optional.of(false);
                } else if (tool.getToolState().equals(ToolState.Type.END) && tool.getMusicVenue().getStartPos().equals(context.getClickedPos()))
                {
                    player.displayClientMessage(new TranslationTextComponent("message.mxtune.same_block_error").withStyle(TextFormatting.BOLD), true);
                    tool.setToolState(ToolState.Type.START);
                    return Optional.of(false);
                } else if (tool.getToolState().equals(ToolState.Type.END) && notMinimumSize(tool.getMusicVenue().getStartPos(), context.getClickedPos()))
                {
                    player.displayClientMessage(new TranslationTextComponent("message.mxtune.venue_too_small").withStyle(TextFormatting.BOLD), true);
                    tool.setToolState(ToolState.Type.START);
                    return Optional.of(false);
                } else
                    return Optional.of(true);
            case REMOVE:
                if (pvs.inVenue() && (pvs.getVenue().getOwnerUUID().equals(player.getUUID()) || player.isCreative() || isOp(player)))
                    return Optional.of(true);
                else
                {
                    player.displayClientMessage(new TranslationTextComponent("message.mxtune.not_owner_of_venue").withStyle(TextFormatting.BOLD), true);
                    return Optional.of(false);
                }
            case DONE:
                break;
            default:
        }
        return Optional.of(false);
    }

    private static boolean notMinimumSize(BlockPos pos1, BlockPos pos2)
    {
        AxisAlignedBB aabb = new AxisAlignedBB(pos1, pos2).inflate(0.5).move(0.5, 0.5, 0.5);
        return ((Math.abs(aabb.maxX - aabb.minX) <= 1.5) || (Math.abs(aabb.maxY - aabb.minY) <= 1.5) || (Math.abs(aabb.maxZ - aabb.minZ) <= 1.5));
    }

    private static boolean notIntersects(ItemUseContext context, BlockPos pos1, BlockPos pos2)
    {
        AxisAlignedBB aabb = new AxisAlignedBB(pos1, pos2).inflate(0.5).move(0.5, 0.5, 0.5);
        boolean[] result = { false };
        MusicVenueProvider.getMusicVenues(context.getLevel()).ifPresent(
                areas -> result[0] = areas.getVenueList()
                        .stream().anyMatch(area -> area.getVenueAABB().intersects(aabb)));
        return result[0];
    }

    @Nullable
    public static MusicVenueTool getTool(PlayerEntity player)
    {
        return (playerTools.get(String.valueOf(player.getId())));
    }

    public static Optional<MusicVenueTool> getToolOpl(PlayerEntity player)
    {
        return Optional.ofNullable(getTool(player));
    }

    public static void sync (PlayerEntity player)
    {
        if (!player.level.isClientSide())
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

    static boolean isOp(PlayerEntity player)
    {
        return player.level.getServer() != null && player.level.getServer().getPlayerList().isOp(player.getGameProfile());
    }
}
