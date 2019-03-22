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
import net.aeronica.mods.mxtune.managers.records.PlayList;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.GetServerDataMessage;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.aeronica.mods.mxtune.sound.IAudioStatusCallback;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.aeronica.mods.mxtune.world.chunk.ModChunkDataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

import static net.aeronica.mods.mxtune.Reference.EMPTY_UUID;
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
    private static UUID currentPlayListUUID = EMPTY_UUID;

    // AREA Song Shuffling
    private static final Random rand = new Random();
    private static Deque<UUID> lastSongs  = new ArrayDeque<>();
    private static final int NUM_LAST_SONGS = 10;
    private static int failedNewSongs;

    // Inter-song delay
    private static final int MAX_DELAY = 600;
    private static final int MIN_DELAY = 200;
    private static int delay = MAX_DELAY / 2;
    private static int counter = 0;
    private static boolean wait = false;

    private ClientPlayManager() { /* NOP */ }

    public static void start()
    {
        ClientPlayManager.resetTimer();
        ClientPlayManager.invalidatePlayId();
        lastSongs.clear();
    }

    @SubscribeEvent
    public static void onEvent(TickEvent.ClientTickEvent event)
    {
        if (ClientCSDMonitor.canMXTunesPlay() && (event.phase == TickEvent.Phase.END))
        {
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

    private static void trackLastSongs(UUID uuidSong)
    {
        int size = lastSongs.size();
        boolean songInDeque = lastSongs.contains(uuidSong);
        if (!songInDeque && size < NUM_LAST_SONGS)
        {
            lastSongs.addLast(uuidSong);
        }
        else if (!songInDeque)
        {
            lastSongs.removeFirst();
            lastSongs.addLast(uuidSong);
        }
        Iterator<UUID> it = lastSongs.iterator();
        for (int i = 0; i < lastSongs.size(); i++)
        {
            ModLogger.info(".......%d uuid: %s", i+1, it.next().toString());
        }
    }

    private static boolean heardSong(UUID uuidSong)
    {
        boolean hasSong =  lastSongs.contains(uuidSong);
        boolean isEmpty = lastSongs.isEmpty();
        boolean manyFails = failedNewSongs > NUM_LAST_SONGS;
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

    @Nullable
    private static Chunk getChunk(WeakReference<Chunk> chunkRef)
    {
        return chunkRef != null ? chunkRef.get() : null;
    }

    private static void updateChunk()
    {
        if (mc.world == null || mc.player == null) return;

        Chunk chunk = mc.world.getChunk(mc.player.getPosition());
        if (mc.world.isChunkGeneratedAt(chunk.x, chunk.z))
        {
            WeakReference<Chunk> prevChunkRef = currentChunkRef;
            currentChunkRef = new WeakReference<>(chunk);
            if ((prevChunkRef != null && currentChunkRef.get() != prevChunkRef.get())
                    || (prevChunkRef == null && currentChunkRef.get() != null)
                    || !(getUUID(ModChunkDataHelper.getString(currentChunkRef.get())).equals(currentPlayListUUID)))
                chunkChange(currentChunkRef, prevChunkRef);

        }
        changeAreaMusic();
    }

    private static void chunkChange(WeakReference<Chunk> current, WeakReference<Chunk> previous)
    {
        Chunk currentChunk = getChunk(current);
        Chunk prevChunk = getChunk(previous);
        if (currentChunk != null)
        {
            currentPlayListUUID = getUUID(ModChunkDataHelper.getString(currentChunk));
            boolean b = ModChunkDataHelper.isFunctional(currentChunk);
            ModLogger.debug("----- Enter Chunk %s, functional: %s, uuid: %s", currentChunk.getPos(), b, currentPlayListUUID.toString());
        }
        if (prevChunk != null)
        {
            UUID prevPlayListUUID = getUUID(ModChunkDataHelper.getString(prevChunk));
            boolean b = ModChunkDataHelper.isFunctional(prevChunk);
            ModLogger.debug("----- Exit Chunk %s, functional: %s, uuid: %s", prevChunk.getPos(), b, prevPlayListUUID.toString());
        }
    }

    private static UUID getUUID(String uuidString)
    {
        try
        {
            return UUID.fromString(uuidString);
        }
        catch (IllegalArgumentException iao)
        {
            return new UUID(0L,0L);
        }
    }

    private static void changeAreaMusic()
    {
        if (!waiting() && ClientFileManager.songAvailable(currentPlayListUUID) && currentPlayId == PlayType.INVALID)
        {
            currentPlayId = AREA.getAsInt();
            UUID song = randomSong(currentPlayListUUID);
            if (!Reference.EMPTY_UUID.equals(song) && !ClientFileManager.hasMusic(song))
            {
                PacketDispatcher.sendToServer(new GetServerDataMessage(song, GetServerDataMessage.GetType.MUSIC, currentPlayId));
                ModLogger.debug("ChangeAreaMusic: Get from SERVER!");
            }
            else if (!Reference.EMPTY_UUID.equals(song) && ClientFileManager.hasMusic(song))
            {
                playMusic(song, currentPlayId);
                ModLogger.debug("ChangeAreaMusic: Get from CACHE!");
            }
            else if (!ClientFileManager.isNotBadMusic(song))
            {
                resetTimer();
                invalidatePlayId();
            }
        }
    }

    public static void playMusic(UUID musicId, int playId)
    {
        if (PlayType.INVALID != playId)
        {
            Song song = ClientFileManager.getMusicFromCache(musicId);
            if (song != null)
            {
                currentPlayId = playId;
                ClientAudio.playLocal(playId, song.getMml(), INSTANCE);
                ModLogger.debug("AREA duration: %s, title: %s", SheetMusicUtil.formatDuration(song.getDuration()), song.getTitle());
            }
        }
    }

    private static UUID randomSong(UUID uuidPlayList)
    {
        PlayList playList = ClientFileManager.getPlayList(uuidPlayList);
        UUID song;
        if (playList != null)
        {
            List<UUID> songs = playList.getSongUUIDs();
            int size = songs.size();
            song = songs.get(rand.nextInt(size));
            while (heardSong(song))
            {
                song = songs.get(rand.nextInt(size));
            }

        trackLastSongs(song);
        ModLogger.info("------- Song uuid: %s", song.toString());
        return song;
        }
        return Reference.EMPTY_UUID;
    }

    private static boolean waiting()
    {
        Boolean canPlay = ClientAudio.getActivePlayIDs().isEmpty() && !Reference.EMPTY_UUID.equals(currentPlayListUUID) && ClientFileManager.isNotBadPlayList(currentPlayListUUID);
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
        ModLogger.debug("resetTimer: new delay %05d seconds", delay/20);
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
