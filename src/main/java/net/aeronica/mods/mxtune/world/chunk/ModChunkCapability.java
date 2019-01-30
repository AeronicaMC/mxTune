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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.Util;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

@Mod.EventBusSubscriber
public class ModChunkCapability
{
    @CapabilityInject(IModChunkData.class)
    private static final Capability<IModChunkData> MOD_CHUNK_DATA = Util.nonNullInjected();

    private ModChunkCapability() { /* NOP */ }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IModChunkData.class, new Storage(), new Factory());
    }

    @SubscribeEvent
    public static void onEvent(AttachCapabilitiesEvent<Chunk> event)
    {
        if (event.getObject() != null)
        {
            event.addCapability(new ResourceLocation(Reference.MOD_ID, "chunk_music"), new ICapabilitySerializable<NBTTagCompound>()
            {
                IModChunkData instance = MOD_CHUNK_DATA.getDefaultInstance();

                @Override
                public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
                {
                    return capability == MOD_CHUNK_DATA;
                }

                @Nullable
                @Override
                public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
                {
                    return capability == MOD_CHUNK_DATA ? MOD_CHUNK_DATA.<T>cast(instance) : null;
                }

                @Override
                public NBTTagCompound serializeNBT()
                {
                    return (NBTTagCompound) MOD_CHUNK_DATA.getStorage().writeNBT(MOD_CHUNK_DATA, instance, null);
                }

                @Override
                public void deserializeNBT(NBTTagCompound nbt)
                {
                    MOD_CHUNK_DATA.getStorage().readNBT(MOD_CHUNK_DATA, instance, null, nbt);
                }
            });
        }
    }

    private static class Factory implements Callable<IModChunkData>
    {
        @Override
        public IModChunkData call()
        {
            return new ModChunkDataImpl();
        }
    }

    private static class Storage implements Capability.IStorage<IModChunkData>
    {
        private static final String KEY_FUNCTIONAL = "functional";
        private static final String KEY_STRING = "string";

        @Nullable
        @Override
        public NBTBase writeNBT(Capability<IModChunkData> capability, IModChunkData instance, EnumFacing side)
        {
            NBTTagCompound properties =  new NBTTagCompound();
            properties.setBoolean(KEY_FUNCTIONAL, instance.isFunctional());
            properties.setString(KEY_STRING, instance.getString());
            return properties;
        }

        @Override
        public void readNBT(Capability<IModChunkData> capability, IModChunkData instance, EnumFacing side, NBTBase nbt)
        {
            NBTTagCompound properties = (NBTTagCompound) nbt;
            instance.setFunctional(properties.getBoolean(KEY_FUNCTIONAL));
            instance.setString(properties.getString(KEY_STRING));
        }
    }
}
