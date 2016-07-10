/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.capabilities;

import java.util.concurrent.Callable;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class JamPlayerCapabilityProvider
{
    public static void register()
    {
        CapabilityManager.INSTANCE.register(IJamPlayer.class, new Storage(), new Factory());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public static class EventHandler
    {
        @SubscribeEvent
        public void onEntityConstruct(final AttachCapabilitiesEvent.Entity evt)
        {
            if (evt.getEntity() instanceof EntityPlayer)
            {
                evt.addCapability(new ResourceLocation(MXTuneMain.MODID, "JamPlayer"), new ICapabilitySerializable<NBTTagCompound>()
                {
                    final IJamPlayer jamPlayerInst = new JamDefaultImpl((EntityLivingBase) evt.getEntity());

                    @Override
                    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                    {
                        return capability == MXTuneMain.JAM_PLAYER;
                    }

                    @Override
                    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                    {
                        return capability == MXTuneMain.JAM_PLAYER ? MXTuneMain.JAM_PLAYER.<T> cast(jamPlayerInst) : null;
                    }

                    public NBTTagCompound serializeNBT()
                    {
                        return (NBTTagCompound) MXTuneMain.JAM_PLAYER.getStorage().writeNBT(MXTuneMain.JAM_PLAYER, jamPlayerInst, null);
                    }

                    public void deserializeNBT(NBTTagCompound nbt)
                    {
                        MXTuneMain.JAM_PLAYER.getStorage().readNBT(MXTuneMain.JAM_PLAYER, jamPlayerInst, null, nbt);
                    }
                });
            }
        }

        @SubscribeEvent
        public void OnPlayerClone(PlayerEvent.Clone evt)
        {
            IJamPlayer dead = evt.getOriginal().getCapability(MXTuneMain.JAM_PLAYER, null);
            IJamPlayer live = evt.getEntityPlayer().getCapability(MXTuneMain.JAM_PLAYER, null);
            live.setJam(dead.getJam());
        }

    }

    private static class Factory implements Callable<IJamPlayer>
    {
        @Override
        public IJamPlayer call() throws Exception
        {
            ModLogger.logInfo("+++ +++ +++ Factory#call: return new JamDefaultImpl(null); ! ! !");
            return new JamDefaultImpl(null);
        }
    }

    public static class Storage implements Capability.IStorage<IJamPlayer>
    {
        @Override
        public NBTBase writeNBT(Capability<IJamPlayer> capability, IJamPlayer instance, EnumFacing side)
        {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<IJamPlayer> capability, IJamPlayer instance, EnumFacing side, NBTBase nbt)
        {
            instance.deserializeNBT((NBTTagCompound) nbt);
        }
    }
}