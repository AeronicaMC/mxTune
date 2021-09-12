package aeronicamc.mods.mxtune.sound;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.sound.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.util.*;
import java.util.concurrent.*;

public class ClientAudio
{
    private static final Logger LOGGER = LogManager.getLogger(ClientAudio.class);
    public static final Object THREAD_SYNC = new Object();
    private static final Minecraft mc = Minecraft.getInstance();
    private static SoundEngine soundEngine;
    private static SoundHandler soundHandler;
    private static MusicTicker musicTicker;
    private static int counter;
    private static final Queue<Integer> delayedAudioDataRemovalQueue = new ConcurrentLinkedDeque<>();

    private static final int THREAD_POOL_SIZE = 2;
    /* PCM Signed Monaural little endian */
    private static final AudioFormat audioFormat3D = new AudioFormat(48000, 16, 1, true, false);
    /* PCM Signed Stereo little endian */
    private static final AudioFormat audioFormatStereo = new AudioFormat(48000, 16, 2, true, false);
    /* Used to track which player/groups queued up music to be played by PlayID */
    private static final Queue<Integer> playIDQueue01 = new ConcurrentLinkedQueue<>(); // Polled in initializeCodec
    private static final Map<Integer, AudioData> playIDAudioData = new ConcurrentHashMap<>();

    private static ExecutorService executorService = null;
    private static ThreadFactory threadFactory = null;

    private static boolean vanillaMusicPaused = false;

    private ClientAudio() { /* NOP */ }

    public static synchronized Set<Integer> getActivePlayIDs()
    {
        return Collections.unmodifiableSet(new HashSet<>(playIDAudioData.keySet()));
    }

    private static void startThreadFactory()
    {
        if (threadFactory == null)
        {
            threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat(Reference.MOD_ID + " ClientAudio-%d")
                    .setDaemon(true)
                    .setPriority(Thread.NORM_PRIORITY)
                    .build();
            executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, threadFactory);
        }
    }

    private static void init(SoundEngine se)
    {
        if (soundHandler == null || soundEngine == null)
        {
            soundEngine = se;
            soundHandler = soundEngine.soundManager;
        }
        if (musicTicker == null)
            musicTicker = mc.getMusicManager();
    }

    public enum Status
    {
        WAITING, READY, ERROR, DONE
    }

    private static synchronized void addPlayIDQueue(int playID)
    {
        playIDQueue01.add(playID);
    }

    private static int pollPlayIDQueue01()
    {
        return playIDQueue01.peek() == null ? PlayIdSupplier.INVALID : playIDQueue01.poll();
    }

    private static int peekPlayIDQueue01()
    {
        return playIDQueue01.peek() == null ? PlayIdSupplier.INVALID : playIDQueue01.peek();
    }

    private static AudioData getAudioData(Integer playID)
    {
        synchronized (THREAD_SYNC)
        {
            return playIDAudioData.get(playID);
        }
    }

    static synchronized void setISound(Integer playID, ISound iSound)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null)
            audioData.setISound(iSound);
    }

    @Nullable
    private static BlockPos getBlockPos(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return (audioData != null) ? audioData.getBlockPos() : null;
    }

    static boolean hasPlayID(int playID)
    {
        return !playIDAudioData.isEmpty() && playIDAudioData.containsKey(playID);
    }

    private static boolean isClientPlayer(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData != null && audioData.isClientPlayer();
    }

    /**
     * For players.
     * @param playID unique submission identifier.
     * @param musicText MML string
     */
    public static void play(Integer playID, String musicText)
    {
        play(playID, null, musicText, false, null);
    }

    /**
     * For TileEntities placed in the world.
     * @param playID unique submission identifier.
     * @param pos block position in the world
     * @param musicText MML string
     */
    public static void play(Integer playID, BlockPos pos, String musicText)
    {
        play(playID, pos, musicText, false, null);
    }

    public static void playLocal(int playId, String musicText, @Nullable IAudioStatusCallback callback)
    {
        play(playId, mc.player == null ? null : mc.player.blockPosition(), musicText, true, callback);
    }

    // Determine if audio is 3D spacial or background
    // Players playing solo, or in JAMS hear their own audio without 3D effects or falloff.
    private static void setAudioFormat(AudioData audioData)
    {
        if (audioData.isClientPlayer())
            audioData.setAudioFormat(audioFormatStereo);
        else audioData.setAudioFormat(audioFormat3D);
    }

    private static void play(int playID, @Nullable BlockPos pos, String musicText, boolean isClient, @Nullable IAudioStatusCallback callback)
    {
        startThreadFactory();
        if(playID != PlayIdSupplier.INVALID)
        {
            addPlayIDQueue(playID);
            AudioData audioData = new AudioData(playID, pos, isClient, callback);
            setAudioFormat(audioData);
            AudioData result = playIDAudioData.putIfAbsent(playID, audioData);
            if (result != null)
            {
                LOGGER.warn("ClientAudio#play: playID: {} has already been submitted", playID);
                return;
            }
            if (isClient)
                mc.getSoundManager().play(new MusicClient(audioData));
            else
                mc.getSoundManager().play(new MusicPositioned(audioData));
            executorService.execute(new ThreadedPlay(audioData, musicText));
            stopVanillaMusic();
        } else
        {
            LOGGER.warn("ClientAudio#play(Integer playID, BlockPos pos, String musicText): playID is null!");
        }
    }

    public static void stop(int playID)
    {
        if (PlayIdSupplier.INVALID == playID) return;
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null && audioData.getISound() != null)
            soundHandler.stop(audioData.getISound());
    }

    private static class ThreadedPlay implements Runnable
    {
        private final AudioData audioData;
        private final String musicText;

        ThreadedPlay(AudioData audioData, String musicText)
        {
            this.audioData = audioData;
            this.musicText = musicText;
        }

        @Override
        public void run()
        {
            MML2PCM mml2PCM = new MML2PCM(audioData, musicText);
            mml2PCM.process();
        }
    }

    private static void cleanup()
    {
        playIDAudioData.keySet().forEach(ClientAudio::queueAudioDataRemoval);
        playIDQueue01.clear();
    }

    // SoundEngine
    // private final Map<ISound, ChannelManager.Entry> playingSoundsChannel = Maps.newHashMap(); // AT this so we can attach the PCM the audio stream
    // private final Multimap<SoundCategory, ISound> instanceBySource = HashMultimap.create(); // AT this for monitoring our ISounds
    // private final List<ITickableSound> tickableSounds = Lists.newArrayList(); // AT this for monitoring
    //

    //    this.audioStreamManager.getStream(sound.getSoundAsOggLocation()).thenAccept((p_217928_1_) -> {
    //        channelmanager$entry.runOnSoundExecutor((p_217935_1_) -> {
    //            p_217935_1_.attachBufferStream(p_217928_1_);
    //            p_217935_1_.play();
    //            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.sound.PlayStreamingSourceEvent(this, isound, p_217935_1_));
    //        });
    //    });

    //    public CompletableFuture<IAudioStream> getStream(ResourceLocation p_217917_1_) {
    //        return CompletableFuture.supplyAsync(() -> {
    //            try {
    //                IResource iresource = this.resourceManager.getResource(p_217917_1_);
    //                InputStream inputstream = iresource.getInputStream();
    //                return new OggAudioStream(inputstream);
    //            } catch (IOException ioexception) {
    //                throw new CompletionException(ioexception);
    //            }
    //        }, Util.getServerExecutor());
    //    }


    /**
     *
     * @return the IAudioStream from the newly created PCMAudioStream
     * @param audioData
     */
    private static CompletableFuture<IAudioStream> submitStream(AudioData audioData)
    {
        return CompletableFuture.supplyAsync(() -> new PCMAudioStream(audioData), Util.backgroundExecutor());
    }

    /**
     * Try to create a
     */
    private static void initializeCodec()
    {
        if (soundEngine != null && soundHandler != null && peekPlayIDQueue01() != PlayIdSupplier.INVALID)
        {
            synchronized (soundEngine.instanceToChannel)
            {
                AudioData audioData = getAudioData(pollPlayIDQueue01());
                if (audioData.getISound() != null)
                {
                    ISound sound = audioData.getISound();
                    ChannelManager.Entry entry = soundEngine.instanceToChannel.get(sound);
                    if (entry != null)
                    {
                        submitStream(audioData).thenAccept(iAudioStream -> entry.execute(soundSource ->
                            {
                                soundSource.attachBufferStream(iAudioStream);
                                soundSource.play();
                            }));
                        int playId = audioData.getPlayId();
                        LOGGER.debug("initializeCodec: PlayID {}, ISound {}", playId, sound.getLocation());
                    }
                    else
                    {
                        int playId = audioData.getPlayId();
                        playIDAudioData.remove(playId);
                        LOGGER.debug("initializeCodec: failed - Queue 3: {}", playId);
                    }
                }
            }
        }
    }

    private static void updateClientAudio()
    {
        if (soundEngine != null)
        {
            if(isVanillaMusicPaused() && playIDAudioData.isEmpty() )
            {
                resumeVanillaMusic();
                setVanillaMusicPaused(false);
            } else if (!playIDAudioData.isEmpty())
            {
                // don't allow the timer to counter down while ClientAudio sessions are playing
                setVanillaMusicTimer(Integer.MAX_VALUE);
            }
            // Remove inactive playIDs
            removeQueuedAudioData();
            for (Map.Entry<Integer, AudioData> entry : playIDAudioData.entrySet())
            {
                AudioData audioData = entry.getValue();
                Status status = audioData.getStatus();
                if (status == Status.ERROR || status == Status.DONE || !soundEngine.instanceToChannel.containsKey(audioData.getISound()))
                {
                    // Stopping playing audio takes 100 milliseconds. e.g. SoundSystem fadeOut(<source>, <delay in ms>)
                    // To prevent audio clicks/pops we have the wait at least that amount of time
                    // before removing the AudioData instance for this playID.
                    // Therefore the removal is queued for 250 milliseconds.
                    // e.g. the client tick setup to trigger once every 1/4 second.
                    queueAudioDataRemoval(entry.getKey());
                    LOGGER.debug("updateClientAudio: AudioData for playID {} queued for removal", entry.getKey());
                }
            }
        }
    }

    private static void removeQueuedAudioData()
    {
        while (!delayedAudioDataRemovalQueue.isEmpty())
            if (delayedAudioDataRemovalQueue.peek() != null)
                playIDAudioData.remove(Objects.requireNonNull(delayedAudioDataRemovalQueue.poll()));
    }

    public static void queueAudioDataRemoval(int playId)
    {
        stop(playId);
        delayedAudioDataRemovalQueue.add(playId);
    }

    private static void stopVanillaMusicTicker()
    {
        if (musicTicker.currentMusic != null)
        {
            soundHandler.stop(musicTicker.currentMusic);
            musicTicker.currentMusic = null;
            musicTicker.nextSongDelay = 0;
        }
    }

    private static void stopVanillaMusic()
    {
        LOGGER.debug("ClientAudio stopVanillaMusic - PAUSED on {} active sessions.", playIDAudioData.size());
        setVanillaMusicPaused(true);
        stopVanillaMusicTicker();
        setVanillaMusicTimer(Integer.MAX_VALUE);
    }

    private static void resumeVanillaMusic()
    {
        LOGGER.debug("ClientAudio resumeVanillaMusic - RESUMED");
        setVanillaMusicTimer(100);
    }

    private static void setVanillaMusicTimer(int value)
    {
        if (musicTicker != null)
            musicTicker.nextSongDelay = value;
    }

    private static void setVanillaMusicPaused(boolean flag)
    {
        vanillaMusicPaused = flag;
    }

    private static boolean isVanillaMusicPaused()
    {
        return vanillaMusicPaused;
    }

    @SubscribeEvent
    public static void event(TickEvent.ClientTickEvent event)
    {
        if (event.side == LogicalSide.CLIENT && event.phase == TickEvent.Phase.END)
        {
            /* once every 1/4 second */
            if (counter++ % 5 == 0)
                initializeCodec();

            if (counter % 20 == 0)
                updateClientAudio();
        }
    }

    @SubscribeEvent
    public static void event(SoundSetupEvent event) // never gets called
    {
        init(event.getManager());
        LOGGER.debug("SoundSetupEvent");
    }

    @SubscribeEvent
    public static void event(SoundLoadEvent event) // only called on sound reload. i.e. key-press F3+T
    {
        cleanup();
        init(event.getManager());
        LOGGER.debug("SoundLoadEvent");
    }

    @SubscribeEvent
    public static void event(SoundEvent.SoundSourceEvent event)
    {
        if (event.getSound().getLocation().equals(ModSoundEvents.PCM_PROXY.getRegistryName()))
            LOGGER.debug("SoundSourceEvent", event.getName());
    }

    @SubscribeEvent
    public static void event(PlaySoundEvent event)
    {
        // Gets called often so pretty much guarantees ClientAudio will get initialized.
        init(event.getManager());
    }

    @SubscribeEvent
    public static void event(PlayStreamingSourceEvent event)
    {
        if (event.getSound().getLocation().equals(ModSoundEvents.PCM_PROXY.getRegistryName()))
        {
            // This will never get called since the consumer of the stream disqualifies non-ogg sound resources.
            LOGGER.debug("pcm-proxy PlayStreamingSourceEvent");
        }
    }
}
