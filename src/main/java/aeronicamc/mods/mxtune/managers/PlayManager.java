package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.blocks.IMusicPlayer;
import aeronicamc.mods.mxtune.blocks.IPlacedInstrument;
import aeronicamc.mods.mxtune.caches.ModDataStore;
import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import aeronicamc.mods.mxtune.init.ModSoundEvents;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier.PlayType;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.PlayMusicMessage;
import aeronicamc.mods.mxtune.network.messages.StopPlayMessage;
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

import static aeronicamc.mods.mxtune.managers.PlayIdSupplier.INVALID;

/**
 * The PlayManger class is used Server Side ONLY!
 */
@SuppressWarnings("unused")
public final class PlayManager
{
    public static final Object THREAD_SYNC = new Object();
    private static final Logger LOGGER = LogManager.getLogger(PlayManager.class);

    private PlayManager() { /* NOP */ }

    private static int getNextPlayID()
    {
        return PlayType.PLAYERS.getAsInt();
    }

    /**
     * For playing music from an Item
     * @param playerIn who is playing
     * @return a unique play id or {@link aeronicamc.mods.mxtune.managers.PlayIdSupplier#INVALID} if unable to play
     */
    public static int playMusic(PlayerEntity playerIn)
    {
        return playMusic(playerIn, null, false);
    }

    /**
     * For playing music from a block, e.g. Band Amp.
     * @param world the world of course
     * @param blockPos position of block instrument
     * @return a unique play id or {@link aeronicamc.mods.mxtune.managers.PlayIdSupplier#INVALID} if unable to play
     */
    public static int playMusic(World world, BlockPos blockPos)
    {
        int playId = INVALID;
        IMusicPlayer musicPlayer;
        if (world.getBlockState(blockPos).hasTileEntity() && world.getBlockEntity(blockPos) instanceof IMusicPlayer)
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
                        musicPlayer.setMusicSourceEntityId(musicSource.getId());
                        addActivePlayId(musicSource.getId(), blockPos, playId, musicProperties.getMusicText(), musicProperties.getDuration());
                        world.addFreshEntity(musicSource);
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
     * @return a unique play id or {@link aeronicamc.mods.mxtune.managers.PlayIdSupplier#INVALID} if unable to play
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
        return INVALID;
    }

    private static int playSolo(PlayerEntity playerIn, String musicText, int duration, Integer playerID)
    {
        int playId = getNextPlayID();
        int entityId = playerIn.getId();
        // TODO: See if we can attach the music source to a player, or maybe make the instrument the source!?
        addActivePlayId(entityId, null, playId, musicText, duration);
        PlayMusicMessage packetPlaySolo = new PlayMusicMessage(playId, LocalDateTime.now(ZoneId.of("GMT0")).toString(), duration, 0, entityId, musicText);
        PacketDispatcher.sendToTrackingEntityAndSelf(packetPlaySolo, playerIn);
        return playId;
    }

    private static int getPresetIndex(@Nullable BlockPos pos, PlayerEntity playerIn, boolean isPlaced)
    {
        int presetIndex = 0;
        if (isPlaced && (pos != null))
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
            PacketDispatcher.sendToAll(new StopPlayMessage());
            ActiveTune.removeAll();
        }
    }

    public static void stopPlayId(int playId)
    {
        synchronized (THREAD_SYNC)
        {
            if (INVALID == playId) return;
            LOGGER.debug("stopPlayId {}", playId);
            PacketDispatcher.sendToAll(new StopPlayMessage(playId));
            removeActivePlayId(playId);
        }
    }

    public static <T extends Entity> void stopPlayingEntity(T pEntity)
    {
        synchronized (THREAD_SYNC)
        {
            stopPlayingEntity(pEntity.getId());
        }
    }

    private static void stopPlayingEntity(int entityId)
    {
        synchronized (THREAD_SYNC)
        {
            if (activeTuneEntityActive(entityId))
            {
                stopPlayId(ActiveTune.getPlayIdForEntity(entityId));
            }
        }
    }

    public static int getEntitiesPlayId(int entityId)
    {
        synchronized (THREAD_SYNC)
        {
            return ActiveTune.getPlayIdForEntity(entityId);
        }
    }

    private static void addActivePlayId(int entityId, @Nullable BlockPos blockPos, int playId, String musicText, int durationSeconds)
    {
        if ((playId != INVALID))
            ActiveTune.addEntry(entityId, blockPos, playId, musicText, durationSeconds);
    }

    private static void removeActivePlayId(int playId)
    {
        if ((playId != INVALID))
            ActiveTune.remove(playId);
    }

    public static void sendMusicTo(@Nullable ServerPlayerEntity listeningPlayer, @Nullable Entity soundSourceEntity)
    {
        synchronized (THREAD_SYNC)
        {
            if ((listeningPlayer != null) && (soundSourceEntity != null) && activeTuneEntityActive(soundSourceEntity))
            {
                ActiveTune.getActiveTuneByEntityId(soundSourceEntity).ifPresent(activeTune-> {
                    if (listeningPlayer.level.getServer() != null && activeTune.isActive())
                    {
                        PacketDispatcher.sendTo(new PlayMusicMessage(activeTune.playId, LocalDateTime.now(ZoneId.of("GMT0")).toString(), activeTune.durationSeconds, activeTune.getSecondsElapsed(), soundSourceEntity.getId(), activeTune.musicText), listeningPlayer);
                        LOGGER.debug("sendMusicTo {} starting at {}", listeningPlayer.getDisplayName().getString(), SheetMusicHelper.formatDuration(activeTune.getSecondsElapsed()));
                    }
                    else
                    {
                        LOGGER.warn("sendMusicTo -ERROR- or -DONE- No playId: {} for this Entity: {}", PlayManager.getEntitiesPlayId(soundSourceEntity.getId()), soundSourceEntity);
                    }
                });
            }
        }
    }

    public static void stopListeningTo(@Nullable ServerPlayerEntity listeningPlayer, @Nullable Entity soundSourceEntity)
    {
        synchronized (THREAD_SYNC)
        {
            if ((listeningPlayer != null) && (soundSourceEntity != null) && activeTuneEntityActive(soundSourceEntity))
            {
                ActiveTune.getActiveTuneByEntityId(soundSourceEntity).ifPresent(activeTune-> {
                     if (listeningPlayer.level.getServer() != null)
                     {
                         PacketDispatcher.sendTo(new StopPlayMessage(activeTune.playId), listeningPlayer);
                         LOGGER.debug("{} stopListeningTo {}", listeningPlayer.getDisplayName().getString(), soundSourceEntity.getName().getString());
                     }
                     else
                     {
                         LOGGER.warn("stopListeningTo -ERROR- No playId: {} for this Entity: {}", getEntitiesPlayId(soundSourceEntity.getId()), soundSourceEntity);
                     }
                 });
            }
        }
    }

    public static boolean activeTuneEntityActive(@Nullable Entity pEntity)
    {
        synchronized (THREAD_SYNC)
        {
            return pEntity != null && ActiveTune.entityActive(pEntity.getId());
        }
    }

    public static boolean activeTuneEntityActive(int entityId)
    {
        synchronized (THREAD_SYNC)
        {
            return ActiveTune.entityActive(entityId);
        }
    }

    public static boolean activeTuneEntityExists(@Nullable Entity pEntity)
    {
        synchronized (THREAD_SYNC)
        {
            return pEntity != null && ActiveTune.entityExists(pEntity.getId());
        }
    }

    public static boolean activeTuneEntityExists(int entityId)
    {
        synchronized (THREAD_SYNC)
        {
            return ActiveTune.entityExists(entityId);
        }
    }

    public static boolean isActivePlayId(int playId)
    {
        synchronized (THREAD_SYNC)
        {
            return ActiveTune.isActivePlayId(playId);
        }
    }
}
