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

package net.aeronica.mods.mxtune.groups;

import net.aeronica.libs.mml.core.TestData;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.aeronica.mods.mxtune.sound.IAudioStatusCallback;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.aeronica.mods.mxtune.util.ModLogger;
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

import static net.aeronica.mods.mxtune.groups.PlayIdSupplier.PlayType;
import static net.aeronica.mods.mxtune.groups.PlayIdSupplier.PlayType.AREA;
import static net.aeronica.mods.mxtune.groups.PlayIdSupplier.PlayType.INVALID;
import static net.aeronica.mods.mxtune.groups.PlayIdSupplier.getTypeForPlayId;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientPlayManager implements IAudioStatusCallback
{
    private static final ClientPlayManager INSTANCE = new ClientPlayManager();
    private static Minecraft mc = Minecraft.getMinecraft();
    private static WeakReference<Chunk> currentChunkRef;
    private static int currentPlayId = INVALID;

    // AREA Song Shuffling
    private static final Random rand = new Random();
    private static Deque<String> lastSongs  = new ArrayDeque<>();
    private static final int NUM_LAST_SONGS = 10;
    private static int failedNewSongs;

    // Inter-song delay
    private static final int MAX_DELAY = 600;
    private static final int MIN_DELAY = 200;
    private static int delay = MAX_DELAY / 2;
    private static int counter = 0;
    private static boolean wait = false;

    private ClientPlayManager() { /* NOP */ }

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

    private static void trackLastSongs(String song)
    {
        int size = lastSongs.size();
        boolean songInDeque = lastSongs.contains(song);
        if (!songInDeque && size < NUM_LAST_SONGS)
        {
            lastSongs.addLast(song);
        }
        else if (!songInDeque)
        {
            lastSongs.removeFirst();
            lastSongs.addLast(song);
        }
        Iterator<String> it = lastSongs.iterator();
        for (int i = 0; i < lastSongs.size(); i++)
        {
            ModLogger.info(".......%d title: %s", i+1, it.next());
        }
    }

    private static boolean heardSong(String song)
    {
        boolean hasSong =  lastSongs.contains(song);
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
    private static Chunk getChunk(WeakReference<Chunk> chunkRef) { return chunkRef != null ? chunkRef.get() : null; }

    private static void updateChunk()
    {
        if (mc.world == null || mc.player == null) return;

        Chunk chunk = mc.world.getChunk(mc.player.getPosition());
        if (mc.world.isChunkGeneratedAt(chunk.x, chunk.z))
        {
            WeakReference<Chunk> prevChunkRef = currentChunkRef;
            currentChunkRef = new WeakReference<>(chunk);
            if ((prevChunkRef != null && currentChunkRef.get() != prevChunkRef.get()) || (prevChunkRef == null && currentChunkRef.get() != null))
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
            String s = ModChunkDataHelper.getString(currentChunk);
            boolean b = ModChunkDataHelper.isFunctional(currentChunk);
            ModLogger.debug("----- Enter Chunk %s, functional: %s, string: %s", currentChunk.getPos(), b, s);
        }
        if (prevChunk != null)
        {
            String s = ModChunkDataHelper.getString(prevChunk);
            boolean b = ModChunkDataHelper.isFunctional(prevChunk);
            ModLogger.debug("----- Exit Chunk %s, functional: %s, string: %s", prevChunk.getPos(), b, s);
        }
    }

    private static void changeAreaMusic()
    {
        if (!waiting())
        {
            currentPlayId = AREA.getAsInt();
            ClientAudio.playLocal(currentPlayId, randomSong(), INSTANCE);
        }
    }

    private static String randomSong()
    {
        TestData testData = TestData.getMML(rand.nextInt(TestData.values().length));
        String title = testData.getTitle();
        while (heardSong(title))
        {
            testData = TestData.getMML(rand.nextInt(TestData.values().length));
            title = testData.getTitle();
        }

        trackLastSongs(title);
        ModLogger.info("------- Song title: %s", title);
        return testData.getMML();
    }

    private static boolean waiting()
    {
        Boolean canPlay = ClientAudio.getActivePlayIDs().isEmpty();
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
