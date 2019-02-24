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
import java.util.Random;
import java.util.Set;

import static net.aeronica.mods.mxtune.groups.PlayIdSupplier.PlayType;
import static net.aeronica.mods.mxtune.groups.PlayIdSupplier.PlayType.AREA;
import static net.aeronica.mods.mxtune.groups.PlayIdSupplier.PlayType.INVALID;
import static net.aeronica.mods.mxtune.groups.PlayIdSupplier.getTypeForPlayId;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientPlayManager implements IAudioStatusCallback
{
    private static Minecraft mc = Minecraft.getMinecraft();
    private static WeakReference<Chunk> currentChunkWeakReference;
    private static int currentPlayId = INVALID;
    private static String songName = "";
    private static final Random rand = new Random();
    private static final ClientPlayManager INSTANCE = new ClientPlayManager();

    private ClientPlayManager() { /* NOP */ }

    @SubscribeEvent
    public static void onEvent(TickEvent.ClientTickEvent event)
    {
        if (!ClientCSDMonitor.canMXTunesPlay() && (event.phase != TickEvent.Phase.START)) return;
        updateChunk();
    }

    private static void invalidatePlayId()
    {
        synchronized (INSTANCE)
        {
            currentPlayId = INVALID;
        }
    }

    @Nullable
    private static Chunk getChunk() { return currentChunkWeakReference != null ? currentChunkWeakReference.get() : null; }

    private static void updateChunk()
    {
        if (mc.world == null || mc.player == null) return;

        Chunk chunk = mc.world.getChunk(mc.player.getPosition());
        if (mc.world.isChunkGeneratedAt(chunk.x, chunk.z))
        {
            WeakReference<Chunk> prevChunkRef = currentChunkWeakReference;
            currentChunkWeakReference = new WeakReference<>(chunk);
            if ((prevChunkRef != null && currentChunkWeakReference.get() != prevChunkRef.get()) || (prevChunkRef == null && currentChunkWeakReference.get() != null))
                chunkChange();

        }
        changeAreaMusic();
    }

    private static void chunkChange()
    {
        Chunk chunk = getChunk();
        if (chunk == null) return;

        String s = ModChunkDataHelper.getString(chunk);
        boolean b = ModChunkDataHelper.isFunctional(chunk);
        ModLogger.info("----- Enter Chunk %s, functional: %s, string: %s", chunk, b, s);
    }

    private static void changeAreaMusic()
    {
        boolean canPlay = GroupHelper.getAllPlayIDs().isEmpty() && GroupHelper.getClientManagedPlayIDs().isEmpty();
        if (canPlay)
        {
            currentPlayId = AREA.getAsInt();
            ClientAudio.playLocal(currentPlayId, randomSong(), INSTANCE);
        }
    }

    private static String randomSong()
    {
        TestData testData = TestData.getMML(rand.nextInt(TestData.values().length));
        String title = testData.getTitle();
            if (title.equals(songName))
                songName = randomSong();

        title = testData.getTitle();
        ModLogger.info("------- Song title: %s", title);
        return testData.getMML();
    }

    public static void higherPriority(int playId)
    {
        PlayType testType = getTypeForPlayId(playId);;
        switch (testType)
        {
            case EVENT:
            case PERSONAL:
                removePlayTypeBelow(GroupHelper.getServerManagedPlayIDS(), playId);
            case PLAYERS:
            case AREA:
                removePlayTypeBelow(GroupHelper.getClientManagedPlayIDs(), playId);
            case WORLD:
        }
    }

    private static void removePlayTypeBelow(Set<Integer> setOfPlayIDS, int playId)
    {
        PlayType playType = getTypeForPlayId(playId);
        for (int pid : setOfPlayIDS)
        {
            if (pid < playType.start)
                setOfPlayIDS.remove(pid);
        }
    }

    @Override
    public void statusCallBack(Status status, int playId)
    {
        if (currentPlayId == playId && (status == Status.ERROR || status == Status.DONE))
        {
            invalidatePlayId();
            changeAreaMusic();
        }
    }
}
