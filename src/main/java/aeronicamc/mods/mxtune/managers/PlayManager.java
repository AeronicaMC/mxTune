package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.IPlacedInstrument;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.PlaySoloMessage;
import aeronicamc.mods.mxtune.network.messages.StopPlayIdMessage;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


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

    // Testing Server Side Tune Management
    public static void main(String[] args) throws Exception
    {
        ThreadFactory threadFactoryScheduled = new ThreadFactoryBuilder()
            .setNameFormat(Reference.MOD_NAME + " ActiveTune-%d")
            .setDaemon(true)
            .setPriority(Thread.NORM_PRIORITY)
            .build();

        ThreadFactory threadFactoryCounter = new ThreadFactoryBuilder()
            .setNameFormat(Reference.MOD_NAME + " ActiveTuneCounter-%d")
            .setDaemon(true)
            .setPriority(Thread.NORM_PRIORITY)
            .build();
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2, threadFactoryScheduled);

        ExecutorService executor = Executors.newCachedThreadPool(threadFactoryCounter);

        ActiveTune tune01 = new ActiveTune(scheduledThreadPool, executor, "Song for YOU", 10);
        ActiveTune tune02 = new ActiveTune(scheduledThreadPool, executor,"You are MINE", 5);
        ActiveTune tune03 = new ActiveTune(scheduledThreadPool, executor,"Water is HOT", 7);
        ActiveTune tune04 = new ActiveTune(scheduledThreadPool, executor,"Pound is UP!", 12);
        ActiveTune tune05 = new ActiveTune(scheduledThreadPool, executor,"Lover Blinds", 8);
        ActiveTune tune06 = new ActiveTune(scheduledThreadPool, executor,"Pork Bellies", 9);
        ActiveTune tune07 = new ActiveTune(scheduledThreadPool, executor,"Bu Boo Ba Bu", 66);
        tune01.start();
        tune02.start();
        tune03.start();
        tune04.start();
        tune05.start();
        tune06.start();
        tune07.start();

        Thread.sleep(2000);
        tune01.cancel();
        Thread.sleep(4000);
        tune04.cancel();

        System.in.read();
        executor.shutdown();
        scheduledThreadPool.shutdown();
    }

    public static class ActiveTune
    {
        ScheduledFuture<?> future;
        final AtomicInteger counter = new AtomicInteger();
        final ScheduledExecutorService scheduledThreadPool;
        final ExecutorService executor;
        boolean done;

        String song;
        int durationSeconds;

        public ActiveTune(ScheduledExecutorService scheduledThreadPool, ExecutorService executor,String song, int durationSeconds)
        {
            this.scheduledThreadPool = scheduledThreadPool;
            this.executor = executor;
            this.song = song;
            this.durationSeconds = durationSeconds;
        }

        public void start()
        {
            executor.execute(() -> counter(scheduledThreadPool));
//            Thread thread = new Thread(() -> counter(scheduledThreadPool));
//            thread.setName(song);
//            thread.start();
        }

        public void cancel()
        {
            synchronized (this)
            {
                System.out.println(song + ": Cancelled at " + getCounter() + " seconds of " + getDurationSeconds());
                future.cancel(true);
                done = true;
            }
        }

        private void counter(ScheduledExecutorService service)
        {
            CountDownLatch lock = new CountDownLatch(durationSeconds);
            future = service.scheduleAtFixedRate(() -> {
                System.out.println(song + ": " + counter.incrementAndGet());
                lock.countDown();
            }, 500, 1000, TimeUnit.MILLISECONDS);
            try
            {
                lock.await(durationSeconds * 1000, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                System.out.println("Oops: " + e.getLocalizedMessage());
            }
            finally
            {
                future.cancel(true);
                if (!done)
                {
                    System.out.println(song + ": Done!" );
                    done = true;
                }
            }
        }

        synchronized int getDurationSeconds()
        {
            return durationSeconds;
        }

        synchronized boolean isDone()
        {
            return done;
        }

        synchronized int getCounter()
        {
            return counter.get();
        }

        synchronized String getSong()
        {
            return song;
        }
    }
}
