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
package net.aeronica.mods.mxtune.sound;

import java.util.concurrent.Callable;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * A simple non-serialized capability for non-persistent data
 * @author Paul Boese aka Aeronica
 *
 */
public class PlayStatusCapabillity {

    public static void register() {
        CapabilityManager.INSTANCE.register(IPlayStatus.class, new Storage(), new Factory());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public static class EventHandler
    {

        @SubscribeEvent
        public void onEntityConstruct(AttachCapabilitiesEvent.Entity event)
        {
            if (event.getEntity() instanceof EntityPlayer)
            {
                event.addCapability(new ResourceLocation(MXTuneMain.MODID, "IPlayStatus"), new ICapabilityProvider()
                {
                    IPlayStatus inst = PlayStatusUtil.PLAY_STATUS.getDefaultInstance();

                    @Override
                    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                    {
                        return capability == PlayStatusUtil.PLAY_STATUS;
                    }

                    @Override
                    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                    {
                        return capability == PlayStatusUtil.PLAY_STATUS ? PlayStatusUtil.PLAY_STATUS.<T> cast(inst) : null;
                    }
                });
            }
        }

        @SubscribeEvent
        public void OnPlayerClone(PlayerEvent.Clone evt) {
            /* IPlayStatus dead = evt.getOriginal().getCapability(PlayStatusUtil.PLAY_STATUS, null); */
            IPlayStatus live = evt.getEntityPlayer().getCapability(PlayStatusUtil.PLAY_STATUS, null);
            live.setPlaying(evt.getEntityPlayer(), false);
        }
        
    }

    private static class Factory implements Callable<IPlayStatus> {

        @Override
        public IPlayStatus call() throws Exception {
            return new PlayStatusImpl();
        }
    }

    private static class Storage implements Capability.IStorage<IPlayStatus> {

        @Override
        public NBTBase writeNBT(Capability<IPlayStatus> capability, IPlayStatus instance, EnumFacing side) {
            return null;
        }

        @Override
        public void readNBT(Capability<IPlayStatus> capability, IPlayStatus instance, EnumFacing side, NBTBase nbt) {
        }
    }
    
}