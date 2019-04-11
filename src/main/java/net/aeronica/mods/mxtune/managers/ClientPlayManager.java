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
import net.aeronica.mods.mxtune.world.chunk.ModChunkDataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

import static net.aeronica.mods.mxtune.Reference.EMPTY_GUID;
import static net.aeronica.mods.mxtune.managers.PlayIdSupplier.PlayType;
import static net.aeronica.mods.mxtune.managers.PlayIdSupplier.PlayType.AREA;
import static net.aeronica.mods.mxtune.managers.PlayIdSupplier.PlayType.INVALID;
import static net.aeronica.mods.mxtune.managers.PlayIdSupplier.getTypeForPlayId;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientPlayManager implements IAudioStatusCallback
{
    private static final ClientPlayManager INSTANCE = new ClientPlayManager();
    private static Minecraft mc = Minecraft.getMinecraft();
    private static WeakReference<Chunk> currentChunkRef;
    private static int currentPlayId = INVALID;
    private static GUID currentAreaGUID = EMPTY_GUID;
    private static String lastSongLine01 = "";
    private static String lastSongLine02 = "";

    // AREA Song Shuffling
    private static List<SongProxy> songProxies = new ArrayList<>();
    private static final Random rand = new Random();
    private static Deque<GUID> lastSongs  = new ArrayDeque<>();
    private static final int NUM_LAST_SONGS = 100;
    private static int failedNewSongs;

    // Inter-song delay
    private static final int MAX_DELAY = 30;
    private static final int MIN_DELAY = 10;
    private static int delay = MAX_DELAY / 2;
    private static int counter = 0;
    private static int ticks = 0;
    private static boolean wait = false;

    private static boolean night;

    private ClientPlayManager() { /* NOP */ }

    public static void reset()
    {
        ClientPlayManager.resetTimer();
        ClientPlayManager.invalidatePlayId();
        lastSongs.clear();
        clearLastSongInfo();
    }

    public static GUID getCurrentAreaGUID()
    {
        return currentAreaGUID;
    }

    @SubscribeEvent
    public static void onEvent(TickEvent.ClientTickEvent event)
    {
        if (ClientCSDMonitor.canMXTunesPlay() && (event.phase == TickEvent.Phase.END) && ticks++ % 20 == 0)
        {
            // Poll once per second
            updateChunk();
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

    private static void trackLastSongs(GUID guid)

    {
        int size = lastSongs.size();
        boolean songInDeque = lastSongs.contains(guid);
        if (!songInDeque && size < NUM_LAST_SONGS)
        {
            lastSongs.addLast(guid);
        }
        else if (!songInDeque)
        {
            lastSongs.removeFirst();
            lastSongs.addLast(guid);
        }
        Iterator<GUID> it = lastSongs.iterator();
        for (int i = 0; i < lastSongs.size(); i++)
        {
            GUID guidSong = it.next();
            SongProxy songProxy = ClientFileManager.getSongProxy(guidSong);
            String title = "---waiting on cache to update---";
            if (songProxy != null)
                title = songProxy.getTitle();
            ModLogger.debug(".......%d guid: %s, title: %s", i+1, guidSong.toString(), title);
        }
    }

    private static boolean heardSong(GUID guid)
    {
        boolean hasSong =  lastSongs.contains(guid);
        boolean isEmpty = lastSongs.isEmpty();
        boolean manyFails = failedNewSongs > NUM_LAST_SONGS * 5;
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
        night = time > 13300 && time < 23200;
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
        if (mc.world.isChunkGeneratedAt(chunk.x, chunk.z) && chunk.hasCapability(ModChunkDataHelper.MOD_CHUNK_DATA, null))
        {
            WeakReference<Chunk> prevChunkRef = currentChunkRef;
            currentChunkRef = new WeakReference<>(chunk);
            if ((prevChunkRef != null && currentChunkRef.get() != prevChunkRef.get())
                    || (prevChunkRef == null && currentChunkRef.get() != null)
                    || !(ModChunkDataHelper.getAreaGuid(currentChunkRef.get())).equals(currentAreaGUID))
                chunkChange(currentChunkRef, prevChunkRef);

        }
        updateTimeOfDay();
        changeAreaMusic();
    }

    private static void chunkChange(WeakReference<Chunk> current, WeakReference<Chunk> previous)
    {
        Chunk currentChunk = getChunk(current);
        Chunk prevChunk = getChunk(previous);
        if (currentChunk != null && currentChunk.hasCapability(ModChunkDataHelper.MOD_CHUNK_DATA, null))
        {
            currentAreaGUID = ModChunkDataHelper.getAreaGuid(currentChunk);
            ModLogger.debug("----- Enter Chunk %s, guid: %s", currentChunk.getPos(), currentAreaGUID.toString());
        }
        if (prevChunk != null && prevChunk.hasCapability(ModChunkDataHelper.MOD_CHUNK_DATA, null))
        {
            GUID prevPlayListGUID = ModChunkDataHelper.getAreaGuid(prevChunk);
            ModLogger.debug("----- Exit Chunk %s, guid: %s", prevChunk.getPos(), prevPlayListGUID.toString());
        }
    }

    private static void changeAreaMusic()
    {
        if (!waiting() && ClientFileManager.songAvailable(currentAreaGUID) && currentPlayId == PlayType.INVALID)
        {
            currentPlayId = AREA.getAsInt();
            GUID guidSong = randomSong(currentAreaGUID);
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
                resetTimer();
                invalidatePlayId();
            }
            else if (Reference.EMPTY_GUID.equals(guidSong))
            {
                // This should never happen unless I screwed something up
                ModLogger.warn("ClientPlayManger: What has Aeronica / Rymor done this time?!, SongProxy guid: %s, playId %d", guidSong.toString(), currentPlayId);
                resetTimer(2);
                invalidatePlayId();
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
                ModLogger.debug("AREA duration: %s, title: %s", SheetMusicUtil.formatDuration(song.getDuration()), song.getTitle());
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
            while (heardSong(songProxy.getGUID()))
            {
                songProxy = songProxies.get(rand.nextInt(size));
            }

            trackLastSongs(songProxy.getGUID());

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
        lastSongLine02 = I18n.format("mxtune.info.last_song_line_02", I18n.format("mxtune.error.undefined_area"));
    }

    public static String getLastSongLine01()
    {
        return lastSongLine01;
    }

    public static String getLastSongLine02()
    {
        return lastSongLine02;
    }

    private static boolean waiting()
    {
        Boolean canPlay = ClientAudio.getActivePlayIDs().isEmpty() && !Reference.EMPTY_GUID.equals(currentAreaGUID);
        if (canPlay && !wait) startTimer();
        return !canPlay || (counter <= delay);
    }

    private static void startTimer()
    {
        counter = 0;
        wait = true;
    }

    public static void resetTimer()
    {
        delay = rand.nextInt(MAX_DELAY - MIN_DELAY) + MIN_DELAY;
        ModLogger.debug("resetTimer: new delay %05d seconds", delay);
        wait = false;
    }

    public static void resetTimer(int newDelay)
    {
        delay = newDelay < 1 ? 1 : newDelay;
        ModLogger.debug("resetTimer: new delay %05d seconds", delay);
        wait = false;
    }

    public static void removeLowerPriorityPlayIds(int playId)
    {
        PlayType testType = getTypeForPlayId(playId);
        removePlayTypeBelow(ClientAudio.getActivePlayIDs(), testType);
        resetTimer();
    }

    private static void removePlayTypeBelow(Set<Integer> setOfPlayIDS, PlayType playTypeIn)
    {
        for (int pid : setOfPlayIDS)
        {
            PlayType playType = getTypeForPlayId(pid);
            if (PlayIdSupplier.compare(playTypeIn, playType) > 0)
            {
                ClientAudio.queueAudioDataRemoval(pid);
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
                changeAreaMusic();
                resetTimer();
            }
        });
    }
}
