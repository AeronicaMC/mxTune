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

import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientPlayManager
{
    private static Minecraft mc = Minecraft.getMinecraft();
    private static WeakReference<Chunk> currentChunkWeakReference;
    private static WeakReference<Chunk> previousChunkWeakReference;

    private ClientPlayManager() { /* NOP */ }

    public static void onEvent(TickEvent.ClientTickEvent event)
    {
        if (!ClientCSDMonitor.canMXTunesPlay() && (event.phase != TickEvent.Phase.START)) return;
        updateChunk();




    }

    @Nullable
    private static Chunk getChunk() { return currentChunkWeakReference != null ? currentChunkWeakReference.get() : null; }

    private static void updateChunk()
    {
        if (mc.world == null || mc.player == null) return;

        Chunk chunk = mc.world.getChunk(mc.player.getPosition());
        if (currentChunkWeakReference.get() != previousChunkWeakReference.get())
        {
            previousChunkWeakReference = currentChunkWeakReference;
            currentChunkWeakReference = new WeakReference<>(chunk);
        }
    }
}
