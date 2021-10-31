package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.IMusicPlayer;
import aeronicamc.mods.mxtune.blocks.IPlacedInstrument;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.PlaySoloMessage;
import aeronicamc.mods.mxtune.network.messages.StopPlayIdMessage;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import aeronicamc.mods.mxtune.util.ValidDuration;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static aeronicamc.mods.mxtune.util.SheetMusicHelper.formatDuration;


@SuppressWarnings("unused")
public final class PlayManager
{
    private static final Logger LOGGER = LogManager.getLogger(PlayManager.class);
    private static final Set<Integer> activePlayIds = new HashSet<>();
    private static final Map<Integer, ActiveTune> entityIdToActiveTune = new HashMap<>();
    private static final Map<Integer, Integer> playIdToEntityId = new HashMap<>();

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
     * For playing music from a block, e.g. Band Amp.
     * @param pLevel the world of course
     * @param pBlockPos position of block instrument
     * @return a unique play id
     */
    public static int playMusic(World pLevel, BlockPos pBlockPos)
    {
        int playId = PlayIdSupplier.INVALID;
        IMusicPlayer musicPlayer;
        if (pLevel.getBlockState(pBlockPos).getBlock() instanceof IMusicPlayer)
        {
            musicPlayer = (IMusicPlayer) pLevel.getBlockEntity(pBlockPos);
            if (musicPlayer != null)
            {
                String musicText = musicPlayer.getMML();
                if (musicText.contains("MML@"))
                {
                    ValidDuration validDuration = SheetMusicHelper.validateMML(musicText);
                    if (validDuration.isValidMML())
                    {
                        playId = getNextPlayID();
                        int duration = validDuration.getDuration();
                        addActivePlayId(0, playId, musicText, duration);
                        PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playId, 0 ,musicText);
                        PacketDispatcher.sendToAllAround(packetPlaySolo, pLevel, pBlockPos,64);
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
            String mml = SheetMusicHelper.getMusic(sheetMusic);
            int duration = SheetMusicHelper.getMusicDuration(sheetMusic);

            //mml = mml.replace("MML@", "MML@I" + getPresetIndex(pos, playerIn, isPlaced));
            LOGGER.debug("MML Title: {} Duration: {}", title, duration);
            LOGGER.debug("MML Sub25: {}", mml.substring(0, Math.min(25, mml.length())));

            return playSolo(playerIn, mml, duration, playerID);
        }
        return PlayIdSupplier.INVALID;
    }

    private static int playSolo(PlayerEntity playerIn, String mml, int duration, Integer playerID)
    {
        int playId = getNextPlayID();
        int entityId = playerIn.getId();

        addActivePlayId(entityId, playId, mml, duration);
        PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playId, entityId ,mml);
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

    // FIXME: Need the entityId to get the ActiveTune instance so the activeTune#cancel() can be called before the
    // FIXME: entry is removed from the map. Oh and gee, we need to prevent a loop!
    public static void stopPlayId(int playId)
    {
        removeActivePlayId(playId);
        PacketDispatcher.sendToAll(new StopPlayIdMessage(playId));
    }

    public static <T extends Entity> void stopPlayingEntity(T pEntity)
    {
        stopPlayingEntity(pEntity.getId());
    }

    private static void stopPlayingEntity(Integer entityId)
    {
        if (isEntityPlaying(entityId))
        {
            stopPlayId(entityIdToActiveTune.get(entityId).getPlayId());
        }
    }

    private static int getEntitiesPlayId(@Nullable Integer entityId)
    {
        return (entityId != null) ? entityIdToActiveTune.get(entityId).getPlayId() : PlayIdSupplier.INVALID;
    }

    private static boolean isEntityPlaying(@Nullable Integer entityId)
    {
        return entityId != null && entityIdToActiveTune.containsKey(entityId);
    }

    private static void addActivePlayId(int entityId, int playId, String mml, int durationSeconds)
    {
        if ((playId != PlayIdSupplier.INVALID))
        {
            activePlayIds.add(playId);
            playIdToEntityId.put(playId, entityId);
            if (entityIdToActiveTune.containsKey(entityId))
                entityIdToActiveTune.replace(entityId, ActiveTune.newActiveTune(entityId, playId, mml, durationSeconds).start());
            else
                entityIdToActiveTune.putIfAbsent(entityId, ActiveTune.newActiveTune(entityId, playId, mml, durationSeconds).start());
        }
    }

    private static void removeActivePlayId(int playId)
    {
        if ((playId != PlayIdSupplier.INVALID) && !activePlayIds.isEmpty())
        {
            entityIdToActiveTune.remove(playIdToEntityId.get(playId));
            activePlayIds.remove(playId);
            playIdToEntityId.remove(playId);
        }
    }

    public static void sendPlayersTuneTo(@Nullable ServerPlayerEntity listeningPlayer, @Nullable Entity soundSourceEntity)
    {
        if (listeningPlayer != null && hasActivePlayId(soundSourceEntity))
        {
            ActiveTune activeTune = entityIdToActiveTune.get(soundSourceEntity.getId());
            int playId = activeTune.getPlayId();
            PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playId, activeTune.getSecondsElapsed() , soundSourceEntity.getId() ,activeTune.getMml());
            PacketDispatcher.sendTo(packetPlaySolo, listeningPlayer);
            LOGGER.debug("sendPlayersTuneTo {} starting at {}", listeningPlayer.getDisplayName().getString(), SheetMusicHelper.formatDuration(activeTune.getSecondsElapsed()));
        }
    }

    public static boolean hasActivePlayId(@Nullable Entity pEntity)
    {
        return pEntity != null && entityIdToActiveTune.containsKey(pEntity.getId());
    }

    public static boolean isActivePlayId(int playId)
    {
        return (playId >= 0) && activePlayIds.contains(playId);
    }

    public static class ActiveTune
    {
        private static final Logger LOGGER2 = LogManager.getLogger(ActiveTune.class.getSimpleName());
        private static final ThreadFactory threadFactoryScheduled = new ThreadFactoryBuilder()
                .setNameFormat(Reference.MOD_NAME + " ActiveTune-Scheduled-Counters-%d")
                .setDaemon(true)
                .setPriority(Thread.NORM_PRIORITY)
                .build();
        private static final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2, threadFactoryScheduled);

        private static final ThreadFactory threadFactoryPool = new ThreadFactoryBuilder()
                .setNameFormat(Reference.MOD_NAME + " ActiveTune-pool-%d")
                .setDaemon(true)
                .setPriority(Thread.NORM_PRIORITY)
                .build();
        private static final ExecutorService executor = Executors.newCachedThreadPool(threadFactoryPool);

        private ScheduledFuture<?> future;
        private final AtomicInteger secondsElapsedAI = new AtomicInteger();
        private int secondsElapsed;
        private boolean done;

        protected final int entityId;
        protected final int playId;
        protected final String mml;
        protected final int durationSeconds;

        synchronized int getPlayId()
        {
            return playId;
        }

        private ActiveTune()
        {
            this.entityId = 0;
            this.playId = PlayIdSupplier.INVALID;
            this.mml = "";
            this.durationSeconds = 0;
        }

        private ActiveTune(int entityId, int playId, String mml, int durationSeconds)
        {
            this.entityId= entityId;
            this.playId = playId;
            this.mml = mml;
            this.durationSeconds = durationSeconds;
        }

        public static ActiveTune newActiveTune(int entityId, int playId, String mml, int durationSeconds)
        {
            return new ActiveTune(entityId, playId, mml, durationSeconds);
        }

        public static void shutdown()
        {
            executor.shutdown();
            scheduledThreadPool.shutdown();
        }

        ActiveTune start()
        {
            executor.execute(this::counter);
            return this;
        }

        void cancel()
        {
            synchronized (this)
            {
                LOGGER2.debug("A scheduled or requested cancel was sent for playId: {} that had a duration of {}", playId, formatDuration(durationSeconds));
                LOGGER2.debug("Time elapsed: {}", formatDuration(secondsElapsedAI.get()));
                PlayManager.stopPlayId(playId);
                future.cancel(true);
                done = true;
            }
        }

        private void counter()
        {
            CountDownLatch lock = new CountDownLatch(durationSeconds);
            future = scheduledThreadPool.scheduleAtFixedRate(() -> {
                secondsElapsed = secondsElapsedAI.incrementAndGet();
                lock.countDown();
            }, 500, 1000, TimeUnit.MILLISECONDS);
            try
            {
                lock.await(durationSeconds * 1000, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                LOGGER2.info("Oops: {}", e.getLocalizedMessage());
            }
            finally
            {
                if (!done)
                    cancel();
            }
        }

        synchronized int getEntityId()
        {
            return entityId;
        }

        synchronized int getDurationSeconds()
        {
            return durationSeconds;
        }

        synchronized boolean isDone()
        {
            return done;
        }

        synchronized int getSecondsElapsed()
        {
            return secondsElapsed + 1;
        }

        synchronized String getMml()
        {
            return mml;
        }
    }
}
