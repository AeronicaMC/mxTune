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
package net.aeronica.mods.mxtune.options;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public class PlayerMusicOptionsCapability
{
    @CapabilityInject(IPlayerMusicOptions.class)
    private static final Capability<IPlayerMusicOptions> MUSIC_OPTIONS = Util.nonNullInjected();
    
    public static void register()
    {
        CapabilityManager.INSTANCE.register(IPlayerMusicOptions.class, new Storage(), new Factory());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public static class EventHandler
    {
        @SubscribeEvent
        public void onEntityConstruct(final AttachCapabilitiesEvent<Entity> event)
        {
            if (event.getObject() instanceof EntityPlayer)
            {
                event.addCapability(new ResourceLocation(Reference.MOD_ID, "IPlayerMusicOptions"), new ICapabilitySerializable<NBTTagCompound>()
                {
                    final IPlayerMusicOptions optionsInst = MUSIC_OPTIONS.getDefaultInstance();

                    @Override
                    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                    {
                        return capability == MUSIC_OPTIONS;
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                    {
                        return capability == MUSIC_OPTIONS ? (T) optionsInst : null;
                    }

                    public NBTTagCompound serializeNBT()
                    {
                        return (NBTTagCompound) MUSIC_OPTIONS.getStorage().writeNBT(MUSIC_OPTIONS, optionsInst, null);
                    }

                    public void deserializeNBT(NBTTagCompound nbt)
                    {
                        MUSIC_OPTIONS.getStorage().readNBT(MUSIC_OPTIONS, optionsInst, null, nbt);
                    }
                });
            }
        }

        @SubscribeEvent
        public void OnPlayerClone(PlayerEvent.Clone event)
        {
            if(event.isWasDeath())
            {
                EntityPlayer player = event.getEntityPlayer();
                IPlayerMusicOptions dead = event.getOriginal().getCapability(MUSIC_OPTIONS, null);
                IPlayerMusicOptions live = event.getEntityPlayer().getCapability(MUSIC_OPTIONS, null);
                live.setSParams(player, dead.getSParam1(), dead.getSParam2(), dead.getSParam3());
                live.setHudOptions(player, dead.isHudDisabled(), dead.getPositionHud(), dead.getSizeHud());
                live.setMuteOption(player, dead.getMuteOption());
                live.setBlackList(player, dead.getBlackList());
                live.setWhiteList(player, dead.getWhiteList());               
            }
        }

        @SubscribeEvent
        public void onEntityJoinWorld(EntityJoinWorldEvent event)
        {
            if (event.getEntity() instanceof EntityPlayerMP)
            {
                IPlayerMusicOptions inst = event.getEntity().getCapability(MUSIC_OPTIONS, null);
                inst.syncAll((EntityPlayer) event.getEntity());
            }
        }
        
        @SubscribeEvent
        public void onPlayerLoggedInEvent(PlayerLoggedInEvent event)
        {
            IPlayerMusicOptions inst = event.player.getCapability(MUSIC_OPTIONS, null);
            inst.syncAll(event.player);
        }

    }

    private static class Factory implements Callable<IPlayerMusicOptions>
    {
        @Override
        public IPlayerMusicOptions call() throws Exception
        {
            return new PlayerMusicDefImpl();
        }
    }

    public static class Storage implements Capability.IStorage<IPlayerMusicOptions>
    {
        @Override
        public NBTBase writeNBT(Capability<IPlayerMusicOptions> capability, IPlayerMusicOptions instance, EnumFacing side)
        {
            NBTTagCompound properties = new NBTTagCompound();
            properties.setBoolean("disableHud", instance.isHudDisabled());
            properties.setInteger("positionHud", instance.getPositionHud());
            properties.setFloat("sizeHud", instance.getSizeHud());
            properties.setInteger("muteOption", instance.getMuteOption());
            properties.setString("sParam1", instance.getSParam1());
            properties.setString("sParam2", instance.getSParam2());
            properties.setString("sParam3", instance.getSParam3());
            NBTTagList listBlack = new NBTTagList();
            properties.setTag("listBlack", listBlack);
            for (int i=0; i<instance.getBlackList().size(); i++)
            {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setLong("UUIDLeast", instance.getBlackList().get(i).getUuid().getLeastSignificantBits());
                entry.setLong("UUIDMost", instance.getBlackList().get(i).getUuid().getMostSignificantBits());
                entry.setString("playerName", instance.getBlackList().get(i).getPlayerName());
                listBlack.appendTag(entry);
            }
            NBTTagList listWhite = new NBTTagList();
            properties.setTag("listWhite", listWhite);
            for (int i=0; i<instance.getWhiteList().size(); i++)
            {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setLong("UUIDLeast", instance.getWhiteList().get(i).getUuid().getLeastSignificantBits());
                entry.setLong("UUIDMost", instance.getWhiteList().get(i).getUuid().getMostSignificantBits());
                entry.setString("playerName", instance.getWhiteList().get(i).getPlayerName());
                listWhite.appendTag(entry);
            }   
            return properties;
        }

        @Override
        public void readNBT(Capability<IPlayerMusicOptions> capability, IPlayerMusicOptions instance, EnumFacing side, NBTBase nbt)
        {
            NBTTagCompound properties = (NBTTagCompound) nbt;
            instance.setHudOptions(properties.getBoolean("disableHud"), properties.getInteger("positionHud"), properties.getFloat("sizeHud"));
            instance.setMuteOption(properties.getInteger("muteOption"));
            instance.setSParams(properties.getString("sParam1"), properties.getString("sParam2"), properties.getString("sParam3"));
            if (properties.hasKey("listBlack", Constants.NBT.TAG_LIST))
            {
                NBTTagList listBlack = properties.getTagList("listBlack", Constants.NBT.TAG_COMPOUND);
                int count = listBlack.tagCount();
                List<PlayerLists> blackList = new ArrayList<PlayerLists>();
                for (int i = 0; i < count; i++)
                {
                    NBTTagCompound entry = listBlack.getCompoundTagAt(i);
                    PlayerLists plist = new PlayerLists();
                    plist.setPlayerName(entry.getString("playerName"));
                    plist.setUuid(new UUID(entry.getLong("UUIDMost"), entry.getLong("UUIDLeast")));
                    plist.setOnline(false);
                    blackList.add(plist);
                }
                instance.setBlackList((List<PlayerLists>) blackList);
            }
            if (properties.hasKey("listWhite", Constants.NBT.TAG_LIST))
            {
                NBTTagList listWhite = properties.getTagList("listWhite", Constants.NBT.TAG_COMPOUND);
                int count = listWhite.tagCount();
                List<PlayerLists> whiteList = new ArrayList<PlayerLists>();
                for (int i = 0; i < count; i++)
                {
                    NBTTagCompound entry = listWhite.getCompoundTagAt(i);
                    PlayerLists plist = new PlayerLists();
                    plist.setPlayerName(entry.getString("playerName"));
                    plist.setUuid(new UUID(entry.getLong("UUIDMost"), entry.getLong("UUIDLeast")));
                    plist.setOnline(false);
                    whiteList.add(plist);
                }
                instance.setWhiteList((List<PlayerLists>) whiteList);
            }
        }
    }
}
