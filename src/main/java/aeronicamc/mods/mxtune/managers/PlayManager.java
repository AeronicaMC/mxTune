package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.IPlacedInstrument;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.PlaySoloMessage;
import aeronicamc.mods.mxtune.network.messages.StopPlayIdMessage;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.MXTuneException;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@SuppressWarnings("unused")
public final class PlayManager
{
    private static final Logger LOGGER = LogManager.getLogger(PlayManager.class);
    private static final Set<Integer> activePlayIds = new HashSet<>();
    private static final Map<Integer, Integer> entityIdToPlayId = new HashMap<>();
    private static final Map<Integer, String> activePlayIdsSong = new HashMap<>();

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
        addActivePlayId(livingEntityId, playId, mml);
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

    public static <T extends Entity> void stopPlayingEntity(T pEntity)
    {
        stopPlayingEntity(pEntity.getId());
    }

    private static void stopPlayingEntity(Integer entityId)
    {
        if (isEntityPlaying(entityId))
        {
            stopPlayId(entityIdToPlayId.get(entityId));
        }
    }

    private static int getEntitiesPlayId(@Nullable Integer entityId)
    {
        return (entityId != null) ? entityIdToPlayId.getOrDefault(entityId, PlayIdSupplier.INVALID) : PlayIdSupplier.INVALID;
    }

    private static boolean isEntityPlaying(@Nullable Integer entityId)
    {
        return entityId != null && entityIdToPlayId.containsKey(entityId);
    }

    private static void addActivePlayId(int entityId, int playId, String mml)
    {
        if ((playId != PlayIdSupplier.INVALID))
        {
            activePlayIds.add(playId);
            activePlayIdsSong.putIfAbsent(playId, mml);
            if (entityIdToPlayId.containsKey(entityId))
                entityIdToPlayId.replace(entityId, playId);
            else
                entityIdToPlayId.putIfAbsent(entityId, playId);
        }
    }

    private static void removeActivePlayId(int playId)
    {
        if ((playId != PlayIdSupplier.INVALID) && !activePlayIds.isEmpty())
        {
            activePlayIds.remove(playId);
            activePlayIdsSong.remove(playId);
        }
    }

    public static void sendPlayersTuneTo(@Nullable ServerPlayerEntity listeningPlayer, @Nullable Entity soundSourceEntity)
    {
        if (listeningPlayer != null && hasActivePlayId(soundSourceEntity))
        {
            // TODO: make sendPlayersTuneTo work based on ActiveTune song progress - dis below be ugly
            int playId = entityIdToPlayId.getOrDefault(soundSourceEntity.getId(), PlayIdSupplier.INVALID);
            PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playId, 0, soundSourceEntity.getId() ,activePlayIdsSong.getOrDefault(playId, "@MMLv15t120abcdefg;"));
            PacketDispatcher.sendTo(packetPlaySolo, listeningPlayer);
            LOGGER.debug("sendPlayersTuneTo {}", listeningPlayer.getDisplayName().getString());
        }

    }

    public static boolean hasActivePlayId(@Nullable Entity pEntity)
    {
        return pEntity != null && entityIdToPlayId.containsKey(pEntity.getId());
    }

    public static boolean isActivePlayId(int playId)
    {
        return (playId >= 0) && activePlayIds.contains(playId);
    }

    // Testing Server Side Tune Management
    public static void main(String[] args) throws Exception
    {
        ActiveTune tune01 = ActiveTune.newActiveTune("Song for YOU", 10).start();
        ActiveTune tune02 = ActiveTune.newActiveTune("You are MINE", 5).start();
        ActiveTune tune03 = ActiveTune.newActiveTune("Water is HOT", 7).start();
        ActiveTune tune04 = ActiveTune.newActiveTune("Pound is UP!", 12).start();
        ActiveTune tune05 = ActiveTune.newActiveTune("Bu Boo Ba Bu", 16).start();

        Thread.sleep(2000);
        tune01.cancel();
        Thread.sleep(4000);
        tune04.cancel();

        try
        {
            switch (System.in.read())
            {
                case 'x':
                case 'X':
                default:
                    break;
            }
        }
        catch (IOException e)
        {
            throw new MXTuneException("Ignored IO Exception: " + e.getLocalizedMessage());
        }
        finally
        {
            ActiveTune.shutdown();
        }
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
        private final AtomicInteger secondsElapsed = new AtomicInteger();
        private boolean done;

        private final String song;
        private final int tuneDuration;

        private ActiveTune()
        {
            this.song = "";
            this.tuneDuration = 0;
        }


        private ActiveTune(String song, int tuneDuration)
        {
            this.song = song;
            this.tuneDuration = tuneDuration;
        }

        public static ActiveTune newActiveTune(String song, int tuneDuration)
        {
            return new ActiveTune(song, tuneDuration);
        }

        private static void shutdown()
        {
            executor.shutdown();
            scheduledThreadPool.shutdown();
        }

        public ActiveTune start()
        {
            executor.execute(this::counter);
            return this;
        }

        public void cancel()
        {
            synchronized (this)
            {

                LOGGER2.info("Song: {} Cancelled at {} seconds of {}", song,getSecondsElapsed(), getTuneDuration());
                future.cancel(true);
                done = true;
            }
        }

        private void counter()
        {
            CountDownLatch lock = new CountDownLatch(tuneDuration);
            future = scheduledThreadPool.scheduleAtFixedRate(() -> {
                LOGGER2.info("Song: {} {}", song,  secondsElapsed.incrementAndGet());
                lock.countDown();
            }, 500, 1000, TimeUnit.MILLISECONDS);
            try
            {
                lock.await(tuneDuration * 1000, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                LOGGER2.info("Oops: {}", e.getLocalizedMessage());
            }
            finally
            {
                future.cancel(true);
                if (!done)
                {
                    LOGGER2.info("Song: {} done", song);
                    done = true;
                }
            }
        }

        synchronized int getTuneDuration()
        {
            return tuneDuration;
        }

        synchronized boolean isDone()
        {
            return done;
        }

        synchronized int getSecondsElapsed()
        {
            return secondsElapsed.get();
        }

        synchronized String getSong()
        {
            return song;
        }
    }
}
