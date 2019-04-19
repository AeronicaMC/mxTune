/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.managers;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.managers.records.Area;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.GetServerDataMessage;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.aeronica.mods.mxtune.sound.IAudioStatusCallback;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.aeronica.mods.mxtune.world.caps.chunk.ModChunkPlaylistHelper;
import net.aeronica.mods.mxtune.world.caps.world.ModWorldPlaylistHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

import static net.aeronica.mods.mxtune.Reference.EMPTY_GUID;
import static net.aeronica.mods.mxtune.managers.PlayIdSupplier.PlayType;
import static net.aeronica.mods.mxtune.managers.PlayIdSupplier.PlayType.BACKGROUND;
import static net.aeronica.mods.mxtune.managers.PlayIdSupplier.PlayType.INVALID;
import static net.aeronica.mods.mxtune.managers.PlayIdSupplier.getTypeForPlayId;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientPlayManager implements IAudioStatusCallback
{
    private static final ClientPlayManager INSTANCE = new ClientPlayManager();
    private static Minecraft mc = Minecraft.getMinecraft();
    private static WeakReference<Chunk> currentChunkRef;
    private static int currentPlayId = INVALID;
    private static GUID currentPlaylistGUID = EMPTY_GUID;
    private static GUID previousPlaylistGUID = EMPTY_GUID;
    private static String lastSongLine01 = "";
    private static String lastSongLine02 = "";

    public static final int FADE_TIME_CHANGE_SONG = 2;
    public static final int FADE_TIME_RESET = 2;
    public static final int FADE_TIME_STOP_SONG = 1;


    // BACKGROUND Song Shuffling
    private static List<SongProxy> songProxies = new ArrayList<>();
    private static final Random rand = new Random();
    private static Deque<String> lastSongs  = new ArrayDeque<>();
    private static final int NUM_LAST_SONGS = 1000;
    private static int failedNewSongs;

    // Inter-song delay
    private static final int MAX_DELAY = 16;
    private static final int MIN_DELAY = 8;
    private static int delay = MIN_DELAY;
    private static int counter = 0;
    private static int ticks = 0;
    private static boolean wait = false;

    // Miscellus
    private static boolean night;

    private ClientPlayManager() { /* NOP */ }

    public static void reset()
    {
        if (currentPlayId != PlayType.INVALID)
        {
            ClientAudio.fadeOut(currentPlayId, FADE_TIME_RESET);
            currentPlayId = PlayType.INVALID;
        }
        ClientPlayManager.resetTimer(2);
        lastSongs.clear();
        clearLastSongInfo();
    }

    public static GUID getCurrentPlaylistGUID()
    {
        return currentPlaylistGUID;
    }

    @SubscribeEvent
    public static void onEvent(TickEvent.ClientTickEvent event)
    {
        if (ClientCSDMonitor.canMXTunesPlay() && (event.phase == TickEvent.Phase.END) && ((ticks++ % 10 == 0) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)))
        {
            // Poll twice per second
            updateChunk();
            updateBiome();
            updateWorld();
            counter++;
        }
    }

    private static void invalidatePlayId()
    {
        synchronized (INSTANCE)
        {
            currentPlayId = INVALID;
        }
    }

    private static void trackLastSongs(@Nonnull SongProxy proxy)
    {
        int size = lastSongs.size();
        boolean songInDeque = lastSongs.contains(proxy.getTitle());
        if (!songInDeque && size < NUM_LAST_SONGS)
        {
            lastSongs.addLast(proxy.getTitle());
        }
        else if (!songInDeque)
        {
            lastSongs.removeFirst();
            lastSongs.addLast(proxy.getTitle());
        }
        Iterator<String> it = lastSongs.iterator();
        for (int i = 0; i < lastSongs.size(); i++)
        {
            String title = it.next();
            ModLogger.debug(".......%02d, title: %s", i+1, title);
        }
    }

    private static boolean heardSong(String title)
    {
        boolean hasSong =  lastSongs.contains(title);
        boolean isEmpty = lastSongs.isEmpty();
        boolean manyFails = failedNewSongs > 100;
        if (isEmpty || manyFails)
        {
            failedNewSongs = 0;
            return false;
        }
        if (hasSong)
        {
            failedNewSongs++;
            return true;
        }
        return false;
    }

    private static void updateTimeOfDay()
    {
        // Day / Night
        long time = mc.world.getWorldTime() % 24000;
        night = time > 13200 && time < 23200;
    }

    @Nullable
    private static Chunk getChunk(WeakReference<Chunk> chunkRef)
    {
        return chunkRef != null ? chunkRef.get() : null;
    }

    private static void updateChunk()
    {
        if (mc.world == null || mc.player == null) return;

        Chunk chunk = mc.world.getChunk(mc.player.getPosition());
        if (mc.world.isChunkGeneratedAt(chunk.x, chunk.z) && chunk.hasCapability(ModChunkPlaylistHelper.MOD_CHUNK_DATA, null))
        {
            WeakReference<Chunk> prevChunkRef = currentChunkRef;
            currentChunkRef = new WeakReference<>(chunk);
            if ((prevChunkRef != null && currentChunkRef.get() != prevChunkRef.get())
                    || (prevChunkRef == null && currentChunkRef.get() != null)
                    || !(ModChunkPlaylistHelper.getPlaylistGuid(currentChunkRef.get())).equals(currentPlaylistGUID))
                chunkChange(currentChunkRef, prevChunkRef);

        }
        updateTimeOfDay();
        changeAreaMusic(false);
    }

    private static void chunkChange(WeakReference<Chunk> current, WeakReference<Chunk> previous)
    {
        Chunk currentChunk = getChunk(current);
        Chunk prevChunk = getChunk(previous);
        if (currentChunk != null && currentChunk.hasCapability(ModChunkPlaylistHelper.MOD_CHUNK_DATA, null))
        {
            currentPlaylistGUID = ModChunkPlaylistHelper.getPlaylistGuid(currentChunk);
        }
        if (prevChunk != null && prevChunk.hasCapability(ModChunkPlaylistHelper.MOD_CHUNK_DATA, null))
        {
            GUID prevAreaGUID = ModChunkPlaylistHelper.getPlaylistGuid(prevChunk);
            previousPlaylistGUID = prevAreaGUID;
        }
        if (!currentPlaylistGUID.equals(previousPlaylistGUID))
            changeAreaMusic(true);
    }

    private static void changeAreaMusic(boolean chunkChanged)
    {
        if (chunkChanged && currentPlayId != PlayType.INVALID)
        {
            ClientAudio.fadeOut(currentPlayId, FADE_TIME_CHANGE_SONG);
            currentPlayId = PlayType.INVALID;
            resetTimer(0);
        }

        // Normal Delayed Song Change if not chunkChanged
        if (!waiting(chunkChanged) && ClientFileManager.songAvailable(currentPlaylistGUID) && !Reference.NO_MUSIC_GUID.equals(currentPlaylistGUID) && currentPlayId == PlayType.INVALID)
        {
            currentPlayId = BACKGROUND.getAsInt();
            GUID guidSong = randomSong(currentPlaylistGUID);
            if (!Reference.EMPTY_GUID.equals(guidSong) && !ClientFileManager.hasSongProxy(guidSong))
            {
                PacketDispatcher.sendToServer(new GetServerDataMessage(guidSong, GetServerDataMessage.GetType.MUSIC, currentPlayId));
                ModLogger.debug("ChangeAreaMusic: Get from SERVER!");
            }
            else if (!Reference.EMPTY_GUID.equals(guidSong) && ClientFileManager.hasSongProxy(guidSong))
            {
                playMusic(guidSong, currentPlayId);
                ModLogger.debug("ChangeAreaMusic: Get from CACHE!");
            }
            else if (!ClientFileManager.isNotBadSong(guidSong))
            {
                resetTimer(0);
                invalidatePlayId();
            }
            else if (Reference.EMPTY_GUID.equals(guidSong) && Reference.NO_MUSIC_GUID.equals(currentPlaylistGUID))
            {
                // The Empty Playlist or No Music
                resetTimer(0);
                invalidatePlayId();
            }
        }
    }

    private static void updateBiome()
    {
       // NOP
    }

    private static void updateWorld()
    {
        if (mc.world == null || mc.player == null) return;

        if (Reference.EMPTY_GUID.equals(currentPlaylistGUID) && currentPlayId == PlayType.INVALID)
        {
            World world = mc.world;
            if (world.hasCapability(ModWorldPlaylistHelper.MOD_WORLD_DATA, null))
            {
                currentPlaylistGUID = ModWorldPlaylistHelper.getPlaylistGuid(world);
                changeAreaMusic(false);
            }
        }
    }

    public static void playMusic(GUID musicId, int playId)
    {
        if (PlayType.INVALID != playId)
        {
            Song song = ClientFileManager.getSongFromCache(musicId);
            if (song != null)
            {
                currentPlayId = playId;
                ClientAudio.playLocal(playId, song.getMml(), INSTANCE);
                ModLogger.debug("BACKGROUND duration: %s, title: %s", SheetMusicUtil.formatDuration(song.getDuration()), song.getTitle());
            }
        }
    }

    private static GUID randomSong(GUID guidArea)
    {
        Area area = ClientFileManager.getArea(guidArea);
        SongProxy songProxy;
        if (area != null)
        {
            songProxies.clear();
            if (night)
                songProxies.addAll(area.getPlayListNight());
            else
                songProxies.addAll(area.getPlayListDay());

            int size = songProxies.size();

            if (size == 0)
                return Reference.EMPTY_GUID; // Playlist is empty

            songProxy = songProxies.get(rand.nextInt(size));
            while (heardSong(songProxy.getTitle()))
            {
                songProxy = songProxies.get(rand.nextInt(size));
            }

            trackLastSongs(songProxy);

            lastSongLine01 = I18n.format("mxtune.info.last_song_line_01", area.getName(), night ? I18n.format("mxtune.info.night") : I18n.format("mxtune.info.day"), SheetMusicUtil.formatDuration(songProxy.getDuration()));
            lastSongLine02 = I18n.format("mxtune.info.last_song_line_02", songProxy.getTitle());
            ModLogger.debug(lastSongLine01);
            ModLogger.debug(lastSongLine02);

        return songProxy.getGUID();
        }
        clearLastSongInfo();
        return Reference.EMPTY_GUID;
    }

    private static void clearLastSongInfo()
    {
        String i18nNull = I18n.format("mxtune.info.null");
        lastSongLine01 = I18n.format("mxtune.info.last_song_line_01", i18nNull, i18nNull, i18nNull);
        lastSongLine02 = I18n.format("mxtune.info.last_song_line_02", I18n.format("mxtune.info.playlist.null_playlist"));
    }

    public static String getLastSongLine01()
    {
        return lastSongLine01;
    }

    public static String getLastSongLine02()
    {
        return lastSongLine02;
    }

    private static boolean waiting(boolean override)
    {
        Boolean canPlay = (override && allInRange(ClientAudio.getActivePlayIDs(), BACKGROUND)||ClientAudio.getActivePlayIDs().isEmpty() && !Reference.EMPTY_GUID.equals(currentPlaylistGUID));
        if (override) resetTimer(0);
        if (canPlay && !wait) startTimer();
        return !canPlay || (counter <= delay);
    }

    /**
     * Test if all the playIDs are within the playType specified.
     * @param playIDs set of playIds to test against.
     * @param playType the playType to test against.
     * @return true if all playIds are of the specified PlayType.
     */
    private static boolean allInRange(Set<Integer> playIDs, PlayType playType)
    {
        int result = 0;
        for (Integer playId : playIDs)
        {
            if (!PlayIdSupplier.getTypeForPlayId(playId).equals(playType))
                result++;
        }
        return result == 0;
    }

    private static void startTimer()
    {
        counter = 0;
        wait = true;
    }

    public static void resetTimer()
    {
        delay = (rand.nextInt(MAX_DELAY - MIN_DELAY) + MIN_DELAY) * 2;
        wait = false;
    }

    public static void resetTimer(int newDelay)
    {
        delay = newDelay < 0 ? 0 : newDelay * 2;
        wait = false;
    }

    public static String getDelayTimerDisplay()
    {
        int normalizedDelay = (delay) / 2;
        int normalizedCounter = Math.min(normalizedDelay, counter / 2);
        return String.format("Waiting: %s, Delay: %03d, timer: %03d", waiting(false), normalizedDelay, normalizedCounter);
    }

    public static void removeLowerPriorityPlayIds(int playId)
    {
        PlayType testType = getTypeForPlayId(playId);
        removePlayTypeBelow(ClientAudio.getActivePlayIDs(), testType);
        resetTimer(0);
    }

    private static void removePlayTypeBelow(Set<Integer> setOfPlayIDS, PlayType playTypeIn)
    {
        for (int pid : setOfPlayIDS)
        {
            PlayType playType = getTypeForPlayId(pid);
            if (PlayIdSupplier.compare(playTypeIn, playType) > 0)
            {
                ClientAudio.fadeOut(pid, FADE_TIME_STOP_SONG);
            }
        }
    }

    @Override
    public void statusCallBack(Status status, int playId)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (currentPlayId == playId && (status == Status.ERROR || status == Status.DONE))
            {
                invalidatePlayId();
                changeAreaMusic(false);
                resetTimer();
            }
        });
    }
}
