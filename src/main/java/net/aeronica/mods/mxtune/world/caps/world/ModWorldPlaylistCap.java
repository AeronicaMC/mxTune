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

package net.aeronica.mods.mxtune.world.caps.world;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class ModWorldPlaylistCap
{
    @CapabilityInject(IModWorldPlaylist.class)
    private static final Capability<IModWorldPlaylist> MOD_WORLD_DATA = Miscellus.nonNullInjected();

    private ModWorldPlaylistCap() { /* NOP */ }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IModWorldPlaylist.class, new Storage(), new Factory());
        MinecraftForge.EVENT_BUS.register(ModWorldPlaylistCap.class);
    }

    @SubscribeEvent
    public static void onEvent(AttachCapabilitiesEvent<World> event)
    {
        if (event.getObject() != null)
        {
            event.addCapability(new ResourceLocation(Reference.MOD_ID, "world_music"), new ICapabilitySerializable<CompoundNBT>()
            {
                IModWorldPlaylist instance = MOD_WORLD_DATA.getDefaultInstance();

                @Override
                public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing)
                {
                    return capability == MOD_WORLD_DATA;
                }

                @Nullable
                @Override
                public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
                {
                    return capability == MOD_WORLD_DATA ? MOD_WORLD_DATA.<T>cast(instance) : null;
                }

                @Override
                public CompoundNBT serializeNBT()
                {
                    return (CompoundNBT) MOD_WORLD_DATA.getStorage().writeNBT(MOD_WORLD_DATA, instance, null);
                }

                @Override
                public void deserializeNBT(CompoundNBT nbt)
                {
                    MOD_WORLD_DATA.getStorage().readNBT(MOD_WORLD_DATA, instance, null, nbt);
                }
            });
        }
    }

    private static class Factory implements Callable<IModWorldPlaylist>
    {
        @Override
        public IModWorldPlaylist call()
        {
            return new ModWorldPlaylistImpl();
        }
    }

    private static class Storage implements Capability.IStorage<IModWorldPlaylist>
    {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<IModWorldPlaylist> capability, IModWorldPlaylist instance, Direction side)
        {
            CompoundNBT properties =  new CompoundNBT();
            NBTHelper.setGuidToCompound(properties, instance.getPlaylistGuid());
            return properties;
        }

        @Override
        public void readNBT(Capability<IModWorldPlaylist> capability, IModWorldPlaylist instance, Direction side, NBTBase nbt)
        {
            CompoundNBT properties = (CompoundNBT) nbt;
            instance.setPlaylistGuid(NBTHelper.getGuidFromCompound(properties));
        }
    }
}
