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

package net.aeronica.mods.mxtune.world.caps.chunk;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.UpdateChunkMusicData;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.NBTHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class ModChunkPlaylistCap
{
    @CapabilityInject(IModChunkPlaylist.class)
    private static final Capability<IModChunkPlaylist> MOD_CHUNK_DATA = Miscellus.nonNullInjected();

    private ModChunkPlaylistCap() { /* NOP */ }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IModChunkPlaylist.class, new Storage(), new Factory());
        MinecraftForge.EVENT_BUS.register(ModChunkPlaylistCap.class);
    }

    // TODO: Review for better ways to sync this data.
    @SubscribeEvent
    public static void onEvent(final ChunkWatchEvent.Watch event)
    {
        final ServerPlayerEntity player = event.getPlayer();
        final Chunk chunk = event.getWorld().getChunk(event.getPos().x, event.getPos().z);

        if (chunk != null && chunk.getCapability(MOD_CHUNK_DATA)
        {
            PacketDispatcher.sendTo(new UpdateChunkMusicData(chunk.getPos().x, chunk.getPos().z, ModChunkPlaylistHelper.getPlaylistGuid(chunk)), player);
        }
    }

    @SubscribeEvent
    public static void onEvent(AttachCapabilitiesEvent<Chunk> event)
    {
        if (event.getObject() != null)
        {
            event.addCapability(new ResourceLocation(Reference.MOD_ID, "chunk_music"), new ICapabilitySerializable<CompoundNBT>()
            {
                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap)
                {
                    return MOD_CHUNK_DATA.orEmpty(cap, (LazyOptional<IModChunkPlaylist>) instance);
                }

                IModChunkPlaylist instance = MOD_CHUNK_DATA.getDefaultInstance();

//                @Nullable
//                @Override
//                public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
//                {
//                    return capability == MOD_CHUNK_DATA ? MOD_CHUNK_DATA.<T>cast(instance) : null;
//                }

                @Override
                public CompoundNBT serializeNBT()
                {
                    return (CompoundNBT) MOD_CHUNK_DATA.getStorage().writeNBT(MOD_CHUNK_DATA, instance, null);
                }

                @Override
                public void deserializeNBT(CompoundNBT nbt)
                {
                    MOD_CHUNK_DATA.getStorage().readNBT(MOD_CHUNK_DATA, instance, null, nbt);
                }
            });
        }
    }

    private static class Factory implements Callable<IModChunkPlaylist>
    {
        @Override
        public IModChunkPlaylist call()
        {
            return new ModChunkPlaylistImpl();
        }
    }

    private static class Storage implements Capability.IStorage<IModChunkPlaylist>
    {
        @Nullable
        @Override
        public INBT writeNBT(Capability<IModChunkPlaylist> capability, IModChunkPlaylist instance, Direction side)
        {
            CompoundNBT properties =  new CompoundNBT();
            NBTHelper.setGuidToCompound(properties, instance.getPlaylistGuid());
            return properties;
        }

        @Override
        public void readNBT(Capability<IModChunkPlaylist> capability, IModChunkPlaylist instance, Direction side, INBT nbt)
        {
            CompoundNBT properties = (CompoundNBT) nbt;
            instance.setPlaylistGuid(NBTHelper.getGuidFromCompound(properties));
        }
    }
}
