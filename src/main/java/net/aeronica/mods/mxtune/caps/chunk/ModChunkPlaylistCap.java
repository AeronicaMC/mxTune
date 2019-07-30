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

package net.aeronica.mods.mxtune.caps.chunk;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.caps.SerializableCapabilityProvider;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModChunkPlaylistCap
{
    private static final Logger LOGGER = LogManager.getLogger();

    @CapabilityInject(IModChunkPlaylist.class)
    private static final Capability<IModChunkPlaylist> MOD_CHUNK_DATA = Miscellus.nonNullInjected();
    private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "chunk_music");

    private ModChunkPlaylistCap() { /* NOP */ }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IModChunkPlaylist.class, new Storage(), ModChunkPlaylistImpl::new);
    }

    public static LazyOptional<IModChunkPlaylist> getChunkCap(Chunk chunk)
    {
        return chunk.getCapability(MOD_CHUNK_DATA, null);
    }

    @SubscribeEvent
    public static void onEvent(final ChunkWatchEvent.Watch event)
    {
        final ServerPlayerEntity player = event.getPlayer();
        final Chunk chunk = event.getWorld().getChunk(event.getPos().x, event.getPos().z);

        chunk.getCapability(MOD_CHUNK_DATA).ifPresent(
                chunkPlaylist -> PacketDispatcher.sendTo(
                        new UpdateChunkMusicData(event.getPos().x, event.getPos().z, chunkPlaylist.getPlaylistGuid()), player));
    }

    @SubscribeEvent
    public static void onEvent(AttachCapabilitiesEvent<Chunk> event)
    {
        final ModChunkPlaylistImpl chunkPlaylist = new ModChunkPlaylistImpl(event.getObject());
        event.addCapability(ID, new SerializableCapabilityProvider<>(MOD_CHUNK_DATA, null, chunkPlaylist));
        event.addListener(()->getChunkCap(event.getObject()).invalidate());
        LOGGER.debug("ModChunkPlaylistCap AttachCapabilitiesEvent {}", event.getObject());
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
