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

package net.aeronica.mods.mxtune.caps.world;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.caps.SerializableCapabilityProvider;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModWorldPlaylistCap
{
    private static final Logger LOGGER = LogManager.getLogger();

    @CapabilityInject(IModWorldPlaylist.class)
    private static final Capability<IModWorldPlaylist> MOD_WORLD_DATA = Miscellus.nonNullInjected();
    private static final ResourceLocation ID =  new ResourceLocation(Reference.MOD_ID, "world_music");

    private ModWorldPlaylistCap() { /* NOP */ }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IModWorldPlaylist.class, new Storage(), ModWorldPlaylistImpl::new);
    }

    public static LazyOptional<IModWorldPlaylist> getWorldCap(World world)
    {
        return world.getCapability(MOD_WORLD_DATA, null);
    }

    @SubscribeEvent
    public static void onEvent(AttachCapabilitiesEvent<World> event)
    {
        final ModWorldPlaylistImpl worldPlaylist = new ModWorldPlaylistImpl(event.getObject());
        event.addCapability(ID, new SerializableCapabilityProvider<>(MOD_WORLD_DATA, null, worldPlaylist));
        LOGGER.debug("ModWorldPlaylistCap AttachCapabilitiesEvent {}", event.getObject());
    }

    private static class Storage implements Capability.IStorage<IModWorldPlaylist>
    {
        @Nullable
        @Override
        public INBT writeNBT(Capability<IModWorldPlaylist> capability, IModWorldPlaylist instance, Direction side)
        {
            CompoundNBT properties =  new CompoundNBT();
            NBTHelper.setGuidToCompound(properties, instance.getPlaylistGuid());
            return properties;
        }

        @Override
        public void readNBT(Capability<IModWorldPlaylist> capability, IModWorldPlaylist instance, Direction side, INBT nbt)
        {
            CompoundNBT properties = (CompoundNBT) nbt;
            instance.setPlaylistGuid(NBTHelper.getGuidFromCompound(properties));
        }
    }
}
