package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.blocks.IPlacedInstrument;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.PlaySoloMessage;
import aeronicamc.mods.mxtune.network.messages.StopPlayIdMessage;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@SuppressWarnings("unused")
public final class PlayManager
{
    private static final Logger LOGGER = LogManager.getLogger(PlayManager.class.getSimpleName());
    private static final Set<Integer> activePlayIds = new HashSet<>();
    private static final Map<Integer, Integer> livingEntitiesPlayId = new HashMap<>();

    private PlayManager()
    {
        /* NOP */
    }

    private static int getNextPlayID()
    {
        return PlayIdSupplier.PlayType.PLAYERS.getAsInt();
    }


    /**
     * For playing music from an Item
     * @param playerIn who is playing
     * @return a unique play id
     */
    public static int playMusic(PlayerEntity playerIn)
    {
        return playMusic(playerIn, null, false);
    }

    /**
     * For playing music
     * @param playerIn who is playing
     * @param pos position of block instrument
     * @param isPlaced true is this is a block instrument
     * @return a unique play id or null if unable to play
     */
    public static int playMusic(PlayerEntity playerIn, BlockPos pos, boolean isPlaced)
    {
        ItemStack sheetMusic = SheetMusicHelper.getIMusicFromIInstrument(playerIn.getMainHandItem().getStack());
        if (!sheetMusic.isEmpty())
        {

            Integer playerID = playerIn.getId();
            String title = SheetMusicHelper.getMusicTitleAsString(sheetMusic);
            String mml = SheetMusicHelper.getMusic(sheetMusic);
            int duration = SheetMusicHelper.getMusicDuration(sheetMusic);

            //mml = mml.replace("MML@", "MML@I" + getPresetIndex(pos, playerIn, isPlaced));
            LOGGER.debug("MML Title: {}", title);
            LOGGER.debug("MML Sub25: {}", mml.substring(0, Math.min(25, mml.length())));

            return playSolo(playerIn, mml, duration, playerID);
        }
        return PlayIdSupplier.INVALID;
    }

    private static int playSolo(PlayerEntity playerIn, String mml, int duration, Integer playerID)
    {
        int playId = getNextPlayID();
        int livingEntityId = playerIn.getId();

        DurationTimer.scheduleStop(playId, duration);
        addActivePlayId(livingEntityId, playId);
        PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playId, playerIn.getId() , mml);
        PacketDispatcher.sendToTrackingEntityAndSelf(packetPlaySolo, playerIn);
        return playId;
    }

    private static int getPresetIndex(BlockPos pos, PlayerEntity playerIn, boolean isPlaced)
    {
        int presetIndex = 0;
        if (isPlaced)
        {
            if (playerIn.level.getBlockState(pos).getBlock() instanceof IPlacedInstrument)
            {
                IPlacedInstrument placedInst = (IPlacedInstrument) playerIn.level.getBlockState(pos).getBlock();
                presetIndex =  placedInst.getPatch();
            }
        } else
        {
            IInstrument inst = (IInstrument) playerIn.getMainHandItem().getItem();
            presetIndex =  inst.getPatch(playerIn.getMainHandItem());
        }
        return presetIndex;
    }

    public static void stopPlayId(int playId)
    {
        removeActivePlayId(playId);
        PacketDispatcher.sendToAll(new StopPlayIdMessage(playId));
    }

    public static <T extends LivingEntity> void stopPlayingLivingEntity(T pLivingEntity)
    {
        stopPlayingLivingEntity(pLivingEntity.getId());
    }

    // TODO: Fix here and in ItemMultiInst. Stops playing but starts again.
    private static void stopPlayingLivingEntity(Integer entityId)
    {
        if (isLivingEntityPlaying(entityId))
        {
            stopPlayId(livingEntitiesPlayId.get(entityId));
        }
    }

    private static int getLivingEntityPlayId(@Nullable Integer livingEntityId)
    {
        return (livingEntityId != null) ? livingEntitiesPlayId.getOrDefault(livingEntityId, PlayIdSupplier.INVALID) : PlayIdSupplier.INVALID;
    }

    private static boolean isLivingEntityPlaying(@Nullable Integer entityId)
    {
        return entityId != null && livingEntitiesPlayId.containsKey(entityId);
    }

    private static void addActivePlayId(int livingEntityId, int playId)
    {
        if ((playId != PlayIdSupplier.INVALID))
        {
            activePlayIds.add(playId);
            if (livingEntitiesPlayId.containsKey(livingEntityId))
                livingEntitiesPlayId.replace(livingEntityId, playId);
            else
                livingEntitiesPlayId.putIfAbsent(livingEntityId, playId);
        }
    }

    private static void removeActivePlayId(int playId)
    {
        if ((playId != PlayIdSupplier.INVALID) && !activePlayIds.isEmpty())
        {
            activePlayIds.remove(playId);
        }
    }

    public static boolean isActivePlayId(int playId)
    {
        return (playId >= 0) && activePlayIds.contains(playId);
    }

}
