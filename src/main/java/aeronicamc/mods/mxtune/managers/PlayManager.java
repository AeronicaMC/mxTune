package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.blocks.IMusicPlayer;
import aeronicamc.mods.mxtune.blocks.IPlacedInstrument;
import aeronicamc.mods.mxtune.caches.ModDataStore;
import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import aeronicamc.mods.mxtune.init.ModSoundEvents;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier.PlayType;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.PlayBlockMusicMessage;
import aeronicamc.mods.mxtune.network.messages.PlaySoloMessage;
import aeronicamc.mods.mxtune.network.messages.StopPlayIdMessage;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.Misc;
import aeronicamc.mods.mxtune.util.MusicProperties;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@SuppressWarnings("unused")
public final class PlayManager
{
    public static final Object THREAD_SYNC = new Object();
    private static final Logger LOGGER = LogManager.getLogger(PlayManager.class);
    private static final Set<Integer> activePlayIds = new HashSet<>();
    private static final Map<Integer, ActiveTune> playIdToActiveTune = new HashMap<>();
    private static final Map<Integer, Integer> playIdToEntityId = new HashMap<>();
    private static final Map<Integer, Integer> entityIdToPlayId = new HashMap<>();

    private PlayManager()
    {
        /* NOP */
    }

    private static int getNextPlayID()
    {
        return PlayType.PLAYERS.getAsInt();
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
     * For playing music from a block, e.g. Band Amp.
     * @param world the world of course
     * @param blockPos position of block instrument
     * @return a unique play id
     */
    public static int playMusic(World world, BlockPos blockPos)
    {
        int playId = PlayType.INVALID.getAsInt();
        IMusicPlayer musicPlayer;
        if (world.getBlockState(blockPos).getBlock() instanceof IMusicPlayer)
        {
            musicPlayer = (IMusicPlayer) world.getBlockEntity(blockPos);
            if (musicPlayer != null)
            {
                MusicProperties musicProperties = musicPlayer.getMusicProperties();
                if (musicProperties.getMusicText().contains("MML@"))
                {
                    if (musicProperties.getDuration() >= 4)
                    {
                        playId = getNextPlayID();
                        MusicSourceEntity musicSource = new MusicSourceEntity(world, blockPos, false);
                        world.addFreshEntity(musicSource);
                        addActivePlayId(musicSource.getId(), blockPos, playId, musicProperties.getMusicText(), musicProperties.getDuration());
                        PlayBlockMusicMessage playBlockMusicMessage = new PlayBlockMusicMessage(playId, blockPos , musicProperties.getMusicText());
                        PacketDispatcher.sendToTrackingEntity(playBlockMusicMessage, musicSource);
                    }
                }
            }
        }
        return playId;
    }


    /**
     * For playing music
     * @param playerIn who is playing
     * @param pos position of block instrument
     * @param isPlaced true is this is a block instrument
     * @return a unique play id or null if unable to play
     */
    public static int playMusic(PlayerEntity playerIn, @Nullable BlockPos pos, boolean isPlaced)
    {
        ItemStack sheetMusic = SheetMusicHelper.getIMusicFromIInstrument(playerIn.getMainHandItem().getStack());
        if (!sheetMusic.isEmpty())
        {
            Integer playerID = playerIn.getId();
            String title = SheetMusicHelper.getMusicTitleAsString(sheetMusic);
            String musicTextKey = SheetMusicHelper.getMusicTextKey(sheetMusic);
            String musicText = ModDataStore.getMusicText(musicTextKey);
            if (musicText != null)
            {
                int duration = SheetMusicHelper.getMusicDuration(sheetMusic);

                musicText = musicText.replace("MML@", "MML@I" + getPresetIndex(pos, playerIn, isPlaced));
                LOGGER.debug("MML Title: {} Duration: {}", title, duration);
                LOGGER.debug("MML Sub25: {}", musicText.substring(0, Math.min(25, musicText.length())));

                return playSolo(playerIn, musicText, duration, playerID);
            } else
            {
                // TODO:
                Misc.audiblePingPlayer(playerIn, ModSoundEvents.FAILURE.get());
                playerIn.displayClientMessage(new TranslationTextComponent("errors.mxtune.sheet_music_too_old", musicTextKey), false);
                LOGGER.debug("Music key not found: {}", musicTextKey);
            }
        }
        return PlayType.INVALID.getAsInt();
    }

    private static int playSolo(PlayerEntity playerIn, String musicText, int duration, Integer playerID)
    {
        int playId = getNextPlayID();
        int entityId = playerIn.getId();

        addActivePlayId(entityId, null, playId, musicText, duration);
        PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playId, LocalDateTime.now(ZoneId.of("GMT0")).toString(), 0, entityId , musicText);
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

    public static void stopAll()
    {
        synchronized (THREAD_SYNC)
        {
            for (Map.Entry<Integer, ActiveTune> entry : playIdToActiveTune.entrySet())
            {
                entry.getValue().cancel(false);
            }
            entityIdToPlayId.clear();
            playIdToActiveTune.clear();
            activePlayIds.clear();
            playIdToEntityId.clear();
        }
    }

    public static void stopPlayId(int playId)
    {
        synchronized (THREAD_SYNC)
        {
            if (playIdToActiveTune.containsKey(playId))
            {
                ActiveTune activeTune = playIdToActiveTune.get(playId);
                activeTune.cancel(false);
            }
            removeActivePlayId(playId);
            PacketDispatcher.sendToAll(new StopPlayIdMessage(playId));
        }
    }

    public static <T extends Entity> void stopPlayingEntity(T pEntity)
    {
        synchronized (THREAD_SYNC)
        {
            stopPlayingEntity(pEntity.getId());
        }
    }

    private static void stopPlayingEntity(Integer entityId)
    {
        synchronized (THREAD_SYNC)
        {
            if (isEntityPlaying(entityId))
            {
                stopPlayId(entityIdToPlayId.get(entityId));
            }
        }
    }

    private static int getEntitiesPlayId(@Nullable Integer entityId)
    {
        synchronized (THREAD_SYNC)
        {
            return (entityId != null) ? entityIdToPlayId.get(entityId) : PlayType.INVALID.getAsInt();
        }
    }

    private static boolean isEntityPlaying(@Nullable Integer entityId)
    {
        synchronized (THREAD_SYNC)
        {
            return entityId != null && entityIdToPlayId.containsKey(entityId);
        }
    }

    private static void addActivePlayId(int entityId, @Nullable BlockPos blockPos, int playId, String musicText, int durationSeconds)
    {
        if ((playId != PlayType.INVALID.getAsInt()))
        {
            activePlayIds.add(playId);
            if (entityId != 0)
            {
                if (entityIdToPlayId.containsKey(entityId))
                    entityIdToPlayId.replace(entityId, playId);
                else
                    entityIdToPlayId.putIfAbsent(entityId, playId);

                playIdToEntityId.put(playId, entityId);
                playIdToActiveTune.putIfAbsent(playId, ActiveTune.newActiveTune(entityId, playId, musicText, durationSeconds).start());
            }
            else if (blockPos != null)
            {
                playIdToActiveTune.putIfAbsent(playId, ActiveTune.newActiveTune(blockPos, playId, musicText, durationSeconds).start());
            }
        }
    }

    private static void removeActivePlayId(int playId)
    {
        if ((playId != PlayType.INVALID.getAsInt()) && !activePlayIds.isEmpty())
        {
            entityIdToPlayId.remove(playIdToEntityId.get(playId));
            playIdToActiveTune.remove(playId);
            activePlayIds.remove(playId);
            playIdToEntityId.remove(playId);
        }
    }

    public static void sendMusicTo(@Nullable ServerPlayerEntity listeningPlayer, @Nullable Entity soundSourceEntity)
    {
        synchronized (THREAD_SYNC)
        {
            if ((listeningPlayer != null) && (soundSourceEntity != null) && hasActivePlayId(soundSourceEntity))
            {
                ActiveTune activeTune = getActiveTuneByEntityId(soundSourceEntity);
                if (activeTune != null && listeningPlayer.level.getServer() != null)
                {
                    int playId = activeTune.getPlayId();

                    PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playId, LocalDateTime.now(ZoneId.of("GMT0")).toString(), activeTune.getSecondsElapsed(), soundSourceEntity.getId(), activeTune.getMusicText());
                    PacketDispatcher.sendTo(packetPlaySolo, listeningPlayer);
                    LOGGER.debug("sendMusicTo {} starting at {}", listeningPlayer.getDisplayName().getString(), SheetMusicHelper.formatDuration(activeTune.getSecondsElapsed()));
                }
                else
                {
                    LOGGER.warn("sendMusicTo -ERROR- No playId: {} for this Entity: {}", getEntitiesPlayId(soundSourceEntity.getId()), soundSourceEntity);
                }
            }
        }
    }

    public static void stopListeningTo(@Nullable ServerPlayerEntity listeningPlayer, @Nullable Entity soundSourceEntity)
    {
        synchronized (THREAD_SYNC)
        {
            if ((listeningPlayer != null) && (soundSourceEntity != null) && hasActivePlayId(soundSourceEntity))
            {
                ActiveTune activeTune = getActiveTuneByEntityId(soundSourceEntity);
                if (activeTune != null && listeningPlayer.level.getServer() != null)
                {
                    int playId = activeTune.getPlayId();

                    StopPlayIdMessage stopPlayIdMessage = new StopPlayIdMessage(playId);
                    PacketDispatcher.sendTo(stopPlayIdMessage, listeningPlayer);
                    LOGGER.debug("{} stopListeningTo {}", listeningPlayer.getDisplayName().getString(), soundSourceEntity.getName().getString());
                }
                else
                {
                    LOGGER.warn("stopListeningTo -ERROR- No playId: {} for this Entity: {}", getEntitiesPlayId(soundSourceEntity.getId()), soundSourceEntity);
                }
            }
        }
    }

    public static boolean hasActivePlayId(@Nullable Entity pEntity)
    {
        synchronized (THREAD_SYNC)
        {
            return pEntity != null && entityIdToPlayId.containsKey(pEntity.getId());
        }
    }

    public static boolean isActivePlayId(int playId)
    {
        synchronized (THREAD_SYNC)
        {
            return (playId >= 0) && activePlayIds.contains(playId);
        }
    }

    @Nullable
    static ActiveTune getActiveTuneByEntityId(@Nullable Entity entity)
    {
        synchronized (THREAD_SYNC)
        {
            return (entity != null) ? playIdToActiveTune.get(entityIdToPlayId.get(entity.getId())) : null;
        }
    }
}
