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

package net.aeronica.mods.mxtune.world.chunk;

import net.aeronica.mods.mxtune.util.MXTuneRuntimeException;
import net.aeronica.mods.mxtune.util.Util;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nullable;

public class ModChunkDataHelper
{
    @CapabilityInject(IModChunkData.class)
    private static final Capability<IModChunkData> MOD_CHUNK_DATA =  Util.nonNullInjected();

    private ModChunkDataHelper() { /* NOP */ }

    public static void setFunctional(Chunk chunk, boolean functional)
    {
        getImpl(chunk).setFunctional(functional);
        chunk.markDirty();
    }

    public static boolean isFunctional(Chunk chunk)
    {
        return getImpl(chunk).isFunctional();
    }

    public static void setString(Chunk chunk, String string)
    {
        getImpl(chunk).setString(string);
        chunk.markDirty();
    }

    public static String getString(Chunk chunk)
    {
        return getImpl(chunk).getString();
    }

    @Nullable
    private static IModChunkData getImpl(Chunk chunk)
    {
        IModChunkData chunkData;
        if (chunk.hasCapability(MOD_CHUNK_DATA, null))
            chunkData =  chunk.getCapability(MOD_CHUNK_DATA, null);
        else
            throw new MXTuneRuntimeException("IModChunkData capability is null");
        return chunkData;
    }
}
