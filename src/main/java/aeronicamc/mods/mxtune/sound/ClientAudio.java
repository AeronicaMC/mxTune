package aeronicamc.mods.mxtune.sound;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModSoundEvents;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.mixins.MixinSoundEngine;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Util;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ClientAudio
{
    public static final Logger LOGGER = LogManager.getLogger(ClientAudio.class);
    public static final Object THREAD_SYNC = new Object();
    private static final Minecraft mc = Minecraft.getInstance();
    private static final SoundHandler soundHandler = mc.getSoundManager();
    private static final MusicTicker musicTicker = mc.getMusicManager();
    private static SoundEngine soundEngine;
    //private static final Queue<Integer> delayedAudioDataRemovalQueue = new ConcurrentLinkedDeque<>();
    private static int counter;
    private static final int THREAD_POOL_SIZE = 2;
    /* PCM Signed Monaural little endian */
    private static final AudioFormat audioFormat3D = new AudioFormat(48000, 16, 1, true, false);
    /* PCM Signed Stereo little endian */
    private static final AudioFormat audioFormatStereo = new AudioFormat(48000, 16, 2, true, false);
    /* The active mxTune audio streams by playId */
    //private static final Map<Integer, AudioData> playIDAudioData = new ConcurrentHashMap<>();

    private static final ExecutorService executorService;
    static {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(Reference.MOD_ID + " ClientAudio-%d")
                .setDaemon(true)
                .setPriority(Thread.MAX_PRIORITY)
                .build();
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, threadFactory);
    }

    private ClientAudio() { /* NOP */ }

    public static String getDebugString()
    {
        return String.format("AudioData %d/%d", ActiveAudio.getActiveAudioEntries().size(), ActiveAudio.getDeleteQueueSize());
    }

    private static void init(SoundEngine se)
    {
        if (soundEngine != se)
        {
            soundEngine = se;
            LOGGER.info("Starting mxTune ClientAudio System");
        }
    }

    public enum Status
    {
        WAITING, READY, ERROR, DONE
    }

    private static Optional<AudioData> getAudioData(int playID)
    {
        synchronized (THREAD_SYNC)
        {
            return Optional.ofNullable(ActiveAudio.getAudioData(playID));
        }
    }

    /**
     * For players and source entities.
     * @param duration of the tune in seconds.
     * @param secondsToSkip seconds to skip forward in the music.
     * @param netTransitTime time in milliseconds for server packet to reach the client.
     * @param playID unique submission identifier.
     * @param entityId the id of the music source.
     * @param musicText MML string
     */
    public static void play(int duration, int secondsToSkip, long netTransitTime, int playID, int entityId, String musicText)
    {
        play(playID, duration, secondsToSkip, netTransitTime, entityId, musicText, false, null);
    }

    /**
     * For client side only music.
     * @param duration of the tune in seconds.
     * @param playId unique submission identifier.
     * @param musicText MML string.
     * @param callback if used, will send status changes to the calling class that implements {@link IAudioStatusCallback}.
     */
    public static void playLocal(int duration, int playId, String musicText, @Nullable IAudioStatusCallback callback)
    {
        play(playId, duration, 0, 0, Objects.requireNonNull(mc.player).getId(), musicText, true, callback);
    }

    // Determine if audio is 3D spacial or background
    // Players playing solo, or in JAMS hear their own audio without 3D effects or falloff.
    private static void setAudioFormat(AudioData audioData)
    {
        if (audioData.isClientPlayer())
            audioData.setAudioFormat(audioFormatStereo);
        else audioData.setAudioFormat(audioFormat3D);
    }

    /**
     * The all-in-one #play(...) method of the {@link ClientAudio} class.
     * @param playID The unique server identifier for each music submission. see {@link PlayIdSupplier}
     * @param duration of the tune in seconds
     * @param secondsToSkip seconds to skip forward in the music.
     * @param netTransitTime time in milliseconds for server packet to reach the client.
     * @param entityId The unique entity id of the music source. Generally another player.
     * @param musicText The MML {@link <A="https://en.wikipedia.org/wiki/MML">MML</A>} to be played
     * @param isClient if true, the local client player hears their own music in stereo else other players in 3D audio.
     * @param callback An optional callback that is fired when {@link Status} changes related to {@link AudioData}
     */
    private static void play(int playID, int duration, int secondsToSkip, long netTransitTime, int entityId, String musicText, boolean isClient, @Nullable IAudioStatusCallback callback)
    {
        if (playID != PlayIdSupplier.INVALID)
        {
            AudioData audioData = new AudioData(duration, secondsToSkip, netTransitTime, playID, entityId, isClient, callback);
            setAudioFormat(audioData);
            boolean isDuplicatePlayId = ActiveAudio.addEntry(audioData);
            if (isDuplicatePlayId && mc.player != null && isClient && entityId == mc.player.getId())
            {
                LOGGER.warn("ClientAudio#play: playID: {} has already been submitted", playID);
                return;
            }
            if (isClient || (mc.player != null && (mc.player.getId() == entityId))) // This CLIENT Player own music
            {
                // SPECIAL CASE
                // Player (actively playing solo) changed dimension, so we reuse the playId and REPLACE the AudioData.
                // Vanilla stops all sounds on the client when changing dimension. Playing is restarted where
                // it left off. This works because the server keeps track of all active tunes.
                mc.getSoundManager().play(new MusicClient(audioData));
            }
            else
            {
                // Other players instruments, Music Block
                if ((mc.player != null) && (mc.player.level.getEntity(entityId) != null))
                {
                    mc.getSoundManager().play(new MovingMusic(
                            audioData,
                            Objects.requireNonNull(mc.player.level.getEntity(entityId))));
                }
            }
            if (recordsVolumeOn())
            {
                executorService.execute(new ThreadedPlay(audioData, musicText));
                stopVanillaMusic();
            }
        }
        else
        {
            LOGGER.warn("ClientAudio#play(Integer playID, BlockPos pos, String musicText): playID is null!");
        }
    }

    private static void stopVanillaMusic()
    {
        if (musicTicker.currentMusic != null)
            musicTicker.stopPlaying();
    }

    public static boolean recordsVolumeOn()
    {
        return mc.options.getSoundSourceVolume(SoundCategory.MASTER) > 0F && mc.options.getSoundSourceVolume(SoundCategory.RECORDS) > 0F;
    }

    static void stop(int playID)
    {
        if (PlayIdSupplier.INVALID == playID) return;
        getAudioData(playID).filter(audioData -> audioData.getISound() != null).ifPresent(audioData -> soundHandler.stop(audioData.getISound()));
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
     * A helper method used to force the vanilla {@link SoundEngine} to use our {@link IAudioStream}
     * class {@link PCMAudioStream} codec instead of the default {@link OggAudioStream} codec.
     * @return the IAudioStream from the newly created PCMAudioStream
     * @param audioData {@link AudioData} data and settings related to a music submission.
     */
    private static CompletableFuture<IAudioStream> submitStream(AudioData audioData)
    {
        return CompletableFuture.supplyAsync(() -> new PCMAudioStream(audioData), Util.backgroundExecutor());
    }

    /**
     * Called by the {@link MixinSoundEngine} class. The injection point is between the point where the
     * {@link ChannelManager.Entry} set and the {@link IAudioStream} based codec are initialized. It is at this
     * point we can add our own audio stream using our custom codec.
     * @param pISound   The current {@link ISound}
     * @param isStream  True if the ISound is a stream
     * @param entry     The channel entry associated with this ISound.
     */
    public static void submitStream(ISound pISound, boolean isStream, @Nullable ChannelManager.Entry entry)
    {
        if (isStream)
        {
            if (pISound instanceof MxSound) ((MxSound)pISound).getAudioData().filter(audioData -> audioData.getISound() == pISound).ifPresent( audioData ->
            {
                int playId = audioData.getPlayId();
                LOGGER.info("submitStream {}", pISound);
                if (entry != null)
                {
                    submitStream(audioData).thenAccept(iAudioStream -> entry.execute(soundSource ->
                         {
                             soundSource.attachBufferStream(iAudioStream);
                             soundSource.play();
                         }));
                    LOGGER.debug("initializeCodec: playId: {}, ISound: {}", playId, pISound.getLocation());
                }
                else
                {
                    ActiveAudio.remove(playId);
                    LOGGER.debug("initializeCodec: failed - playId: {}", playId);
                }
            });
        }
    }

    private static void updateClientAudio()
    {
        if (soundEngine != null && recordsVolumeOn())
        {
            ActiveAudio.getActiveAudioEntries().forEach((audioData) -> LOGGER.debug("{}", audioData));
        }
    }

    /**
     * Causes the specified playID to fade out over the specified number of seconds.
     * @param playID the play session to fade.
     * @param seconds to fade out and stop the song. A value of 0 will stop the song immediately.
     */
    public static void fadeOut(int playID, int seconds)
    {
        if (PlayIdSupplier.INVALID == playID) return;
        getAudioData(playID).filter(audioData -> soundEngine != null).ifPresent(audioData -> {
            LOGGER.info("fadeOut: {} in {} sec.", playID, seconds);
            if (seconds > 0)
                audioData.startFadeInOut(seconds, false);
            else
                audioData.expire();
        });
    }

    public static void stopAll()
    {
        ActiveAudio.removeAll();
    }

    @SubscribeEvent
    public static void event(TickEvent.ClientTickEvent event)
    {
        if (event.side == LogicalSide.CLIENT && event.phase == TickEvent.Phase.END)
        {
            // one update per second
            if (counter++ % 20 == 0)
                updateClientAudio();
        }
    }

    // called in SoundEngine CTOR and reload. i.e. key-press F3+T
    // NOTE: The CTOR hook is never fired! The mod event buses might not be active at this point.
    @SubscribeEvent
    public static void event(SoundLoadEvent event)
    {
        init(event.getManager());
        stopAll();
        LOGGER.debug("SoundLoadEvent");
    }

    @SubscribeEvent
    public static void event(PlaySoundEvent event)
    {
        init(event.getManager());
        if (event.getSound().getSource().equals(SoundCategory.MUSIC) && !ActiveAudio.getActivePlayIds().isEmpty())
            event.setResultSound(null);
    }

    @SubscribeEvent
    public static void event(PlayStreamingSourceEvent event)
    {
        // This will never get called since the consumer of the stream disqualifies non-ogg sound resources.
        if (event.getSound().getLocation().equals(ModSoundEvents.PCM_PROXY.getId()))
            LOGGER.debug("PlayStreamingSourceEvent {}", event.getSound().getLocation());
    }
}
