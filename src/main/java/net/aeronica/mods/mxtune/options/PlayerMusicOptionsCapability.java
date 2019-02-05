/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public class PlayerMusicOptionsCapability
{
    @CapabilityInject(IPlayerMusicOptions.class)
    private static final Capability<IPlayerMusicOptions> MUSIC_OPTIONS = Util.nonNullInjected();

    private PlayerMusicOptionsCapability() { /* NOP */ }

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
                    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
                    {
                        return capability == MUSIC_OPTIONS;
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
                    {
                        return capability == MUSIC_OPTIONS ? (T) optionsInst : null;
                    }

                    @Nullable
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
        public void onPlayerClone(PlayerEvent.Clone event)
        {
            if(event.isWasDeath())
            {
                IPlayerMusicOptions dead = event.getOriginal().getCapability(MUSIC_OPTIONS, null);
                IPlayerMusicOptions live = event.getEntityPlayer().getCapability(MUSIC_OPTIONS, null);
                if (live != null && dead != null)
                {
                    live.setSParams(dead.getSParam1(), dead.getSParam2(), dead.getSParam3());
                    live.setHudOptions(dead.isHudDisabled(), dead.getPositionHud(), dead.getSizeHud());
                    live.setMuteOption(dead.getMuteOption());
                    live.setBlackList(dead.getBlackList());
                    live.setWhiteList(dead.getWhiteList());
                    live.setSoundRangeInfinityAllowed(dead.isSoundRangeInfinityRangeAllowed());
                }
            }
        }

        @SubscribeEvent
        public void onEntityJoinWorld(EntityJoinWorldEvent event)
        {
            if (event.getEntity() instanceof EntityPlayerMP)
            {
                IPlayerMusicOptions inst = event.getEntity().getCapability(MUSIC_OPTIONS, null);
                if (inst != null)
                    MusicOptionsUtil.syncAll((EntityPlayer) event.getEntity());
            }
        }
        
        @SubscribeEvent
        public void onPlayerLoggedInEvent(PlayerLoggedInEvent event)
        {
            IPlayerMusicOptions inst = event.player.getCapability(MUSIC_OPTIONS, null);
            if (inst != null)
                MusicOptionsUtil.syncAll(event.player);
        }
    }

    private static class Factory implements Callable<IPlayerMusicOptions>
    {
        @Override
        public IPlayerMusicOptions call()
        {
            return new PlayerMusicDefImpl();
        }
    }

    public static class Storage implements Capability.IStorage<IPlayerMusicOptions>
    {
        static final String KEY_DISABLE_HUD = "disableHud";
        static final String KEY_POSITION_HUD = "positionHud";
        static final String KEY_SIZE_HUD = "sizeHud";
        static final String KEY_MUTE_OPTION = "muteOption";
        static final String KEY_S_PARAM_1 = "sParam1";
        static final String KEY_S_PARAM_2 = "sParam2";
        static final String KEY_S_PARAM_3 = "sParam3";
        static final String KEY_LIST_BLACK = "listBlack";
        static final String KEY_LIST_WHITE = "listWhite";
        static final String KEY_UUID_LEAST = "UUIDLeast";
        static final String KEY_UUID_MOST = "UUIDMost";
        static final String KEY_PLAYER_NAME = "playerName";
        static final String KEY_SOUND_RANGE_INFINITY_ALLOWED = "soundRangeInfinityAllowed";

        @Override
        public NBTBase writeNBT(Capability<IPlayerMusicOptions> capability, IPlayerMusicOptions instance, EnumFacing side)
        {
            NBTTagCompound properties = new NBTTagCompound();
            properties.setBoolean(KEY_DISABLE_HUD, instance.isHudDisabled());
            properties.setInteger(KEY_POSITION_HUD, instance.getPositionHud());
            properties.setFloat(KEY_SIZE_HUD, instance.getSizeHud());
            properties.setInteger(KEY_MUTE_OPTION, instance.getMuteOption());
            properties.setString(KEY_S_PARAM_1, instance.getSParam1());
            properties.setString(KEY_S_PARAM_2, instance.getSParam2());
            properties.setString(KEY_S_PARAM_3, instance.getSParam3());
            properties.setBoolean(KEY_SOUND_RANGE_INFINITY_ALLOWED, instance.isSoundRangeInfinityRangeAllowed());
            NBTTagList listBlack = new NBTTagList();
            properties.setTag(KEY_LIST_BLACK, listBlack);
            for (int i=0; i<instance.getBlackList().size(); i++)
            {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setLong(KEY_UUID_LEAST, instance.getBlackList().get(i).getUuid().getLeastSignificantBits());
                entry.setLong(KEY_UUID_MOST, instance.getBlackList().get(i).getUuid().getMostSignificantBits());
                entry.setString(KEY_PLAYER_NAME, instance.getBlackList().get(i).getPlayerName());
                listBlack.appendTag(entry);
            }
            NBTTagList listWhite = new NBTTagList();
            properties.setTag(KEY_LIST_WHITE, listWhite);
            for (int i=0; i<instance.getWhiteList().size(); i++)
            {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setLong(KEY_UUID_LEAST, instance.getWhiteList().get(i).getUuid().getLeastSignificantBits());
                entry.setLong(KEY_UUID_MOST, instance.getWhiteList().get(i).getUuid().getMostSignificantBits());
                entry.setString(KEY_PLAYER_NAME, instance.getWhiteList().get(i).getPlayerName());
                listWhite.appendTag(entry);
            }   
            return properties;
        }

        @Override
        public void readNBT(Capability<IPlayerMusicOptions> capability, IPlayerMusicOptions instance, EnumFacing side, NBTBase nbt)
        {
            NBTTagCompound properties = (NBTTagCompound) nbt;
            instance.setHudOptions(properties.getBoolean(KEY_DISABLE_HUD), properties.getInteger(KEY_POSITION_HUD), properties.getFloat(KEY_SIZE_HUD));
            instance.setMuteOption(properties.getInteger(KEY_MUTE_OPTION));
            instance.setSParams(properties.getString(KEY_S_PARAM_1), properties.getString(KEY_S_PARAM_2), properties.getString(KEY_S_PARAM_3));
            instance.setSoundRangeInfinityAllowed(properties.getBoolean(KEY_SOUND_RANGE_INFINITY_ALLOWED));
            if (properties.hasKey(KEY_LIST_BLACK, Constants.NBT.TAG_LIST))
            {
                NBTTagList listBlack = properties.getTagList(KEY_LIST_BLACK, Constants.NBT.TAG_COMPOUND);
                int count = listBlack.tagCount();
                List<ClassifiedPlayer> blackList = new ArrayList<>();
                for (int i = 0; i < count; i++)
                {
                    NBTTagCompound entry = listBlack.getCompoundTagAt(i);
                    ClassifiedPlayer plist = new ClassifiedPlayer();
                    plist.setPlayerName(entry.getString(KEY_PLAYER_NAME));
                    plist.setUuid(new UUID(entry.getLong(KEY_UUID_MOST), entry.getLong(KEY_UUID_LEAST)));
                    plist.setOnline(false);
                    blackList.add(plist);
                }
                instance.setBlackList(blackList);
            }
            if (properties.hasKey(KEY_LIST_WHITE, Constants.NBT.TAG_LIST))
            {
                NBTTagList listWhite = properties.getTagList(KEY_LIST_WHITE, Constants.NBT.TAG_COMPOUND);
                int count = listWhite.tagCount();
                List<ClassifiedPlayer> whiteList = new ArrayList<>();
                for (int i = 0; i < count; i++)
                {
                    NBTTagCompound entry = listWhite.getCompoundTagAt(i);
                    ClassifiedPlayer plist = new ClassifiedPlayer();
                    plist.setPlayerName(entry.getString(KEY_PLAYER_NAME));
                    plist.setUuid(new UUID(entry.getLong(KEY_UUID_MOST), entry.getLong(KEY_UUID_LEAST)));
                    plist.setOnline(false);
                    whiteList.add(plist);
                }
                instance.setWhiteList(whiteList);
            }
        }
    }
}
