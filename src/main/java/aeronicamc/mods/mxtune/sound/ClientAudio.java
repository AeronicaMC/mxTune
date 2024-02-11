package aeronicamc.mods.mxtune.sound;

import aeronicamc.libs.mml.parser.MMLParser;
import aeronicamc.libs.mml.parser.MMLParserFactory;
import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.venues.EntityVenueState;
import aeronicamc.mods.mxtune.caps.venues.MusicVenueHelper;
import aeronicamc.mods.mxtune.config.MXTuneConfig;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.util.LoggedTimer;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ClientAudio
{
    public static final Logger LOGGER = LogManager.getLogger(ClientAudio.class);
    private static final Minecraft mc = Minecraft.getInstance();
    private static final SoundHandler soundHandler = mc.getSoundManager();
    private static SoundEngine soundEngine;

    static int MAX_AUDIO_STREAMS = 3;
    private static final int THREAD_POOL_SIZE = 3;
    /* PCM Signed Monaural little endian */
    static final AudioFormat AUDIO_FORMAT_3D = new AudioFormat(48000, 16, 1, true, false);
    /* PCM Signed Stereo little endian */
    static final AudioFormat AUDIO_FORMAT_STEREO = new AudioFormat(48000, 16, 2, true, false);

    static final ImmutableSet<Status> PLAYING_STATUSES =
            ImmutableSet.<Status>builder().add(Status.WAITING).add(Status.PLAY).build();
    static final ImmutableSet<Status> DONE_STATUSES =
            ImmutableSet.<Status>builder().add(Status.DONE).add(Status.ERROR).build();

    private static final ExecutorService executorService;
    static {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(Reference.MOD_ID + " ClientAudio-%d")
                .setDaemon(true)
                .setPriority(Thread.NORM_PRIORITY)
                .build();
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, threadFactory);
    }

    private ClientAudio() { /* NOP */ }

    public static String getDebugString()
    {
        return String.format("AudioData %d/%d/%d", ActiveAudio.getDistanceSortedSources().size(), ActiveAudio.getDeleteQueueSize(), ActiveAudio.getCachedMidiSequenceCount());
    }

    public static List<AudioData> getAudioData()
    {
        return ActiveAudio.getDistanceSortedSources();
    }

    public static float getProgress(int entityId) {
        float[] progress = { 0F };
        ActiveAudio.getActiveTuneByEntityId(entityId).ifPresent(audioData -> progress[0] = audioData.getProgress());
        return progress[0];
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

    /**
     * Test against {@link Status} of DONE, ERROR.
     * @param status to test.
     * @return true if DONE or ERROR
     */
    public static boolean isDoneStatus(Status status)
    {
        return DONE_STATUSES.contains(status);
    }

    /**
     * Test against {@link Status} of DONE, ERROR or YIELD.
     * @param status to test.
     * @return true if one of DONE, ERROR or YIELD.
     */
    public static boolean isDoneOrYieldStatus(Status status)
    {
        return isDoneStatus(status) || Status.YIELD.equals(status);
    }

    /**
     * Test against {@link Status} of PLAY or WAITING.
     * @param status to test.
     * @return true if PLAY or WAITING.
     */
    public static boolean isPlayingStatus(Status status)
    {
        return PLAYING_STATUSES.contains(status);
    }

    private static Optional<AudioData> getAudioData(int playID)
    {
        return Optional.ofNullable(ActiveAudio.getAudioData(playID));
    }

    /**
     * For players and source entities.
     *
     * @param duration      of the tune in seconds.
     * @param secondsToSkip seconds to skip forward in the music.
     * @param playID        unique submission identifier.
     * @param entityId      the id of the music source.
     * @param musicText     MML string
     */
    public static void play(int duration, int secondsToSkip, int playID, int entityId, String musicText)
    {
        play(playID, duration, secondsToSkip, entityId, musicText, false, null);
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
        play(playId, duration, 0, Objects.requireNonNull(mc.player).getId(), musicText, true, callback);
    }

    /**
     * The all-in-one #play(...) method of the {@link ClientAudio} class.
     *
     * @param playID         The unique server identifier for each music submission. see {@link PlayIdSupplier}
     * @param duration       of the tune in seconds
     * @param secondsElapsed seconds to skip forward in the music.
     * @param entityId       The unique entity id of the music source. Generally another player.
     * @param musicText      The MML to be played. see "<a href="https://en.wikipedia.org/wiki/MML">MML</a>"
     * @param isClient       if true, the local client player hears their own music in stereo else other players in 3D audio.
     * @param callback       An optional callback that is fired when {@link Status} changes related to {@link AudioData}
     */
    private static void play(int playID, int duration, int secondsElapsed, int entityId, String musicText, boolean isClient, @Nullable IAudioStatusCallback callback)
    {
        if (playID != PlayIdSupplier.INVALID)
        {
            boolean isReallyClient = (((mc.player != null) && (mc.player.getId() == entityId)) || isClient);
            AudioData audioData = new AudioData(duration, secondsElapsed, playID, entityId, isReallyClient, callback);
            parseMML(audioData, musicText);
            ActiveAudio.addEntry(audioData);
            Minecraft.getInstance().submitAsync(ClientAudio::prioritizeAndLimitSources);
        }
        else
        {
            LOGGER.warn("ClientAudio#play(Integer playID, BlockPos pos, String musicText): playId is INVALID!");
        }
    }

    /**
     * Submit {@link AudioData} source. i.e. restart the tune.
     * @param audioData preexisting source
     */
    static void submitAudioData(AudioData audioData)
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
                Minecraft.getInstance().submitAsync(ClientAudio::prioritizeAndLimitSources);
            }
        }
    }

    private static void stopVanillaMusic()
    {
        synchronized (soundHandler)
        {
            soundHandler.stop(null, SoundCategory.MUSIC);
        }
        synchronized (soundHandler)
        {
            soundHandler.stop(null, SoundCategory.MUSIC);
        }
    }

    static boolean recordsVolumeOn()
    {
        return mc.options.getSoundSourceVolume(SoundCategory.MASTER) > 0F && mc.options.getSoundSourceVolume(SoundCategory.RECORDS) > 0F;
    }

    static void stop(ISound iSound)
    {
        synchronized (soundHandler)
        {
            soundHandler.stop(iSound);
        }
        synchronized (soundHandler)
        {
            soundHandler.stop(iSound);
        }
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
        // Optimization: Parse MML once and store the resulting MIDI Sequence.
        if (ActiveAudio.needsMidiSequence(audioData.getPlayId()))
        {
            LoggedTimer timer = new LoggedTimer();
            timer.start(String.format("%d: Parse MML", audioData.getPlayId()));
            MMLParser mmlParser = MMLParserFactory.getMMLParser(musicText);
            MMLToMIDI toMIDI = new MMLToMIDI();
            toMIDI.processMObjects(mmlParser.getMmlObjects());
            ActiveAudio.addSequence(audioData.getPlayId(), audioData.getDurationSeconds(), toMIDI.getSequence());
            timer.stop();
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

    // TODO: Review for race conditions, test SoundEngine for active source instances (ISound) and resubmit or
    // not depending on result (ensure acquisition of stream, and/or confirm release).
    public static void prioritizeAndLimitSources()
    {
        int[] priority = new int[1];
        int[] availableStreams = new int[1];
        World level = mc.level;
        PlayerEntity player = mc.player;
        if (level == null || player == null) return;
        EntityVenueState playerVenueState = MusicVenueHelper.getEntityVenueState(level, player.getId());

        // TODO: Sorting needs to take into account In/Out of venue.
        ActiveAudio.getDistanceSortedSources().forEach(audioData -> {

            ClientAudio.Status status = audioData.getStatus();
            availableStreams[0] = getAvailableStreamCount();
            EntityVenueState sourceVenueState = MusicVenueHelper.getEntityVenueState(level, audioData.getEntityId());

            if (audioData.getDistanceTo() > (MXTuneConfig.getListenerRange() + 16.0D))
                audioData.yieldStream();
            else if (!playerVenueState.equals(sourceVenueState))
                audioData.yieldStream();
            else if (((priority[0] < availableStreams[0]) && (status == Status.YIELD)) && playerVenueState.equals(sourceVenueState))
                audioData.resume();
            else if (priority[0] > availableStreams[0])
                audioData.yieldStream();

            if (PLAYING_STATUSES.contains(audioData.getStatus()))
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
        getAudioData(playID).ifPresent(audioData -> audioData.startFadeInOut(Math.max(seconds, 0), false, true));
    }

    public static void stopAll()
    {
        ActiveAudio.removeAll();
        synchronized (soundHandler)
        {
            soundEngine.stopAll();
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
}
