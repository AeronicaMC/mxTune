package aeronicamc.mods.mxtune.sound;

import aeronicamc.libs.mml.parser.MMLParser;
import aeronicamc.libs.mml.parser.MMLParserFactory;
import aeronicamc.libs.mml.parser.MMLUtil;
import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.config.MXTuneConfig;
import aeronicamc.mods.mxtune.init.ModSoundEvents;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.mixins.MixinSoundEngine;
import aeronicamc.mods.mxtune.util.LoggedTimer;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.sound.midi.Patch;
import javax.sound.sampled.AudioFormat;
import java.util.List;
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

    private static int counter;
    static int MAX_AUDIO_STREAMS = 3;
    private static final int THREAD_POOL_SIZE = 2;
    /* PCM Signed Monaural little endian */
    static final AudioFormat AUDIO_FORMAT_3D = new AudioFormat(48000, 16, 1, true, false);
    /* PCM Signed Stereo little endian */
    static final AudioFormat AUDIO_FORMAT_STEREO = new AudioFormat(48000, 16, 2, true, false);

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
        return String.format("AudioData %d/%d", ActiveAudio.getDistanceSortedSources().size(), ActiveAudio.getDeleteQueueSize());
    }

    public static List<AudioData> getAudioData()
    {
        return ActiveAudio.getDistanceSortedSources();
    }


    public static float getProgress(int entityId)
    {
        return ActiveAudio.getActiveTuneByEntityId(entityId).isPresent() ? ActiveAudio.getActiveTuneByEntityId(entityId).get().getProgress() : 0F;
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
        WAITING, PLAY, YIELD, ERROR, DONE
    }

    private static Optional<AudioData> getAudioData(int playID)
    {
        return Optional.ofNullable(ActiveAudio.getAudioData(playID));
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
            audioData.setAudioFormat(AUDIO_FORMAT_STEREO);
        else
            audioData.setAudioFormat(AUDIO_FORMAT_3D);
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
            boolean isReallyClient = (((mc.player != null) && (mc.player.getId() == entityId)) || isClient);
            AudioData audioData = new AudioData(duration, secondsToSkip, netTransitTime, playID, entityId, isReallyClient, callback);
            parseMML(audioData, musicText);
            ActiveAudio.addEntry(audioData);
            LOGGER.debug("playId: {}, mc.player: {}, entityId: {}, isClient: {}, isReallyClient: {}", playID, mc.player.getId(), entityId, isClient, isReallyClient);
        }
        else
        {
            LOGGER.warn("ClientAudio#play(Integer playID, BlockPos pos, String musicText): playID is null!");
        }
    }

    /**
     * Submit {@link AudioData} source. i.e. restart the tune.
     * @param audioData preexisting source
     */
    static void submitSoundInstance(AudioData audioData)
    {
        if (mc.player != null)
        {
            Entity entity = mc.player.level.getEntity(audioData.getEntityId());
            if (entity != null)
            {
                if (audioData.isClientPlayer())
                    mc.getSoundManager().play(new MusicClient(audioData));
                else
                    mc.getSoundManager().play(new MovingMusic(audioData, entity));
                if (recordsVolumeOn())
                {
                    executorService.execute(new RenderAudio(audioData));
                    stopVanillaMusic();
                }
            }
        }
    }

    private static void stopVanillaMusic()
    {
        soundHandler.stop(null, SoundCategory.MUSIC);
    }

    static boolean recordsVolumeOn()
    {
        return mc.options.getSoundSourceVolume(SoundCategory.MASTER) > 0F && mc.options.getSoundSourceVolume(SoundCategory.RECORDS) > 0F;
    }

    static void stop(int playID)
    {
        if (PlayIdSupplier.INVALID == playID) return;
        getAudioData(playID).filter(audioData -> audioData.getISound() != null).ifPresent(audioData -> soundHandler.stop(audioData.getISound()));
    }

    private static class RenderAudio implements Runnable
    {
        private final AudioData audioData;

        RenderAudio(AudioData audioData)
        {
            this.audioData = audioData;
        }

        @Override
        public void run()
        {
            new MML2PCM(audioData).process();
        }
    }

    private static void parseMML(AudioData audioData, String musicText)
    {
        LoggedTimer timer = new LoggedTimer();
        timer.start(String.format("%d: Parse MML", audioData.getPlayId()));
        MMLParser mmlParser = MMLParserFactory.getMMLParser(musicText);
        MMLToMIDI toMIDI = new MMLToMIDI();
        toMIDI.processMObjects(mmlParser.getMmlObjects());
        audioData.setSequence(toMIDI.getSequence());
        timer.stop();

        // Log bank and program per instrument
        for (int preset : toMIDI.getPresets())
        {
            Patch patchPreset = MMLUtil.packedPreset2Patch(SoundFontProxyManager.getPackedPreset(preset));
            String name = new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(preset)).getString();
            LOGGER.debug("MML2PCM preset: {}, bank: {}, program: {}, name: {}", preset, patchPreset.getBank(),
                         patchPreset.getProgram(), name);
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

    /**
     * Ugly as sin TODO: get actual values from SoundSystem . . .
     * @return the available stream count with a max of 3
     */
    public static int getAvailableStreamCount()
    {
        String[] COUNTS = soundEngine != null ? soundEngine.getDebugString().split("\\D+") : new String[]{"", "0", "0", "0", "0"};
        return Math.min(Integer.parseInt(COUNTS[4]) - Integer.parseInt(COUNTS[3]), MAX_AUDIO_STREAMS);
    }

    private static void updateClientAudio()
    {
        int[] priority = new int[1];
        int[] availableStreams = new int[1];
        availableStreams[0] = getAvailableStreamCount();
        ActiveAudio.getDistanceSortedSources().forEach(audioData -> {

            ClientAudio.Status status = audioData.getStatus();
            if (audioData.getDistanceTo() > (MXTuneConfig.getListenerRange() + 16D))
                audioData.yield();
            else if (priority[0] < availableStreams[0] && (status == Status.YIELD))
                audioData.resume();
            else if (audioData.isEntityRemoved())
                audioData.expire();
            else if (priority[0] >= availableStreams[0])
                audioData.yield();

            priority[0]++;
        });
    }

    /**
     * Causes the specified playID to fade out over the specified number of seconds.
     * @param playID the play session to fade.
     * @param seconds to fade out and stop the song. A value of 0 will stop the song immediately.
     */
    public static void fadeOut(int playID, int seconds)
    {
        if (PlayIdSupplier.INVALID == playID) return;
        getAudioData(playID).ifPresent(audioData -> {
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
            // one update twice per second
            if (counter++ % 20 == 0)
                updateClientAudio();
        }
    }

    // called in SoundEngine CTOR* and reload. Reload: i.e. key-press F3+T
    // *NOTE: The CTOR hook is fired on the FML MOD BUS! On Sound reload this is fired on the FORGE BUS!
    // ClientAudio is registered to the FORGE BUS!
    @SubscribeEvent
    public static void event(SoundLoadEvent event)
    {
        init(event.getManager());
        stopAll();
        LOGGER.debug("SoundLoadEvent");
    }

    // Here we ensure our local SoundEngine reference is initialized AND also where we prevent a new
    // vanilla MUSIC from starting, when mxTune tunes are playing.
    @SubscribeEvent
    public static void event(PlaySoundEvent event)
    {
        init(event.getManager());
        if (event.getSound().getSource().equals(SoundCategory.MUSIC) && ActiveAudio.isPlaying())
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
