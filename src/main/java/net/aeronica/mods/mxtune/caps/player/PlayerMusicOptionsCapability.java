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
package net.aeronica.mods.mxtune.caps.player;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.caps.SerializableCapabilityProvider;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.NBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerMusicOptionsCapability
{
    private static final Logger LOGGER = LogManager.getLogger();

    @CapabilityInject(IPlayerMusicOptions.class)
    private static final Capability<IPlayerMusicOptions> MUSIC_OPTIONS = Miscellus.nonNullInjected();
    private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "music_options");

    private PlayerMusicOptionsCapability() { /* NOP */ }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IPlayerMusicOptions.class, new Storage(), () -> new PlayerMusicDefImpl(null));
    }

    public static LazyOptional<IPlayerMusicOptions> getOptionsCap(final LivingEntity entity)
    {
        return entity.getCapability(MUSIC_OPTIONS, null);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EventHandler
    {
        @SubscribeEvent
        public static void event(final AttachCapabilitiesEvent<Entity> event)
        {
            if (event.getObject() instanceof PlayerEntity)
            {
                // final LivingEntityModCap livingEntityModCap = new LivingEntityModCap((LivingEntity) event.getObject());
                final IPlayerMusicOptions playerMusicOptions = new PlayerMusicDefImpl((LivingEntity) event.getObject());
                event.addCapability(ID, new SerializableCapabilityProvider<>(MUSIC_OPTIONS, null, playerMusicOptions));
                LOGGER.debug("PlayerMusicOptionsCapability");
            }
        }

        @SubscribeEvent
        public static void event(PlayerEvent.Clone event)
        {
            event.getOriginal().revive(); // gighertz workaround for MCForge #5956 PlayerEvent.Clone Capability Provider is invalid
            getOptionsCap(event.getOriginal()).ifPresent(dead ->
                 {
                    getOptionsCap(event.getEntityPlayer()).ifPresent(live ->
                        {
                            if (!event.isWasDeath() || event.getEntityPlayer().world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || event.getOriginal().isSpectator())
                                {
                                  live.setSParams(dead.getSParam1(), dead.getSParam2(), dead.getSParam3());
                                  live.setHudOptions(dead.isHudDisabled(), dead.getPositionHud(), dead.getSizeHud());
                                  live.setMuteOption(dead.getMuteOption());
                                  live.setBlackList(dead.getBlackList());
                                  live.setWhiteList(dead.getWhiteList());
                                  live.setSoundRangeInfinityAllowed(dead.isSoundRangeInfinityRangeAllowed());
                                  live.setMxTuneServerUpdateAllowed(dead.isSoundRangeInfinityRangeAllowed());
                                  live.setSelectedPlayListGuid(dead.getSelectedPlayListGuid());
                                }
                        });
                 });
        }

        @SubscribeEvent
        public static void event(EntityJoinWorldEvent event)
        {
            if (event.getEntity() instanceof ServerPlayerEntity)
            {
                MusicOptionsUtil.syncAll((PlayerEntity) event.getEntity());
            }
        }
        
        @SubscribeEvent
        public static void event(PlayerLoggedInEvent event)
        {
            MusicOptionsUtil.syncAll(event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void playerChangeDimension(final net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event)
    {
        MusicOptionsUtil.syncAll(event.getPlayer());
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
        static final String KEY_MXTUNE_SERVER_UPDATE_ALLOWED = "mxTuneServerUpdateAllowed";
        static final String KEY_SELECTED_PLAY_LIST_GUID = "selectedPlayListGuid";

        @Override
        public INBT writeNBT(Capability<IPlayerMusicOptions> capability, IPlayerMusicOptions instance, Direction side)
        {
            CompoundNBT properties = new CompoundNBT();
            properties.putBoolean(KEY_DISABLE_HUD, instance.isHudDisabled());
            properties.putInt(KEY_POSITION_HUD, instance.getPositionHud());
            properties.putFloat(KEY_SIZE_HUD, instance.getSizeHud());
            properties.putInt(KEY_MUTE_OPTION, instance.getMuteOption());
            properties.putString(KEY_S_PARAM_1, instance.getSParam1());
            properties.putString(KEY_S_PARAM_2, instance.getSParam2());
            properties.putString(KEY_S_PARAM_3, instance.getSParam3());
            properties.putBoolean(KEY_SOUND_RANGE_INFINITY_ALLOWED, instance.isSoundRangeInfinityRangeAllowed());
            properties.putBoolean(KEY_MXTUNE_SERVER_UPDATE_ALLOWED, instance.isMxTuneServerUpdateAllowed());
            NBTHelper.setGuidToTag(instance.getSelectedPlayListGuid(), properties, KEY_SELECTED_PLAY_LIST_GUID);
            ListNBT listBlack = new ListNBT();
            properties.put(KEY_LIST_BLACK, listBlack);
            for (int i=0; i<instance.getBlackList().size(); i++)
            {
                CompoundNBT entry = new CompoundNBT();
                entry.putLong(KEY_UUID_LEAST, instance.getBlackList().get(i).getUuid().getLeastSignificantBits());
                entry.putLong(KEY_UUID_MOST, instance.getBlackList().get(i).getUuid().getMostSignificantBits());
                entry.putString(KEY_PLAYER_NAME, instance.getBlackList().get(i).getPlayerName());
                listBlack.add(entry);
            }
            ListNBT listWhite = new ListNBT();
            properties.put(KEY_LIST_WHITE, listWhite);
            for (int i=0; i<instance.getWhiteList().size(); i++)
            {
                CompoundNBT entry = new CompoundNBT();
                entry.putLong(KEY_UUID_LEAST, instance.getWhiteList().get(i).getUuid().getLeastSignificantBits());
                entry.putLong(KEY_UUID_MOST, instance.getWhiteList().get(i).getUuid().getMostSignificantBits());
                entry.putString(KEY_PLAYER_NAME, instance.getWhiteList().get(i).getPlayerName());
                listWhite.add(entry);
            }   
            return properties;
        }

        @Override
        public void readNBT(Capability<IPlayerMusicOptions> capability, IPlayerMusicOptions instance, Direction side, INBT nbt)
        {
            CompoundNBT properties = (CompoundNBT) nbt;
            instance.setHudOptions(properties.getBoolean(KEY_DISABLE_HUD), properties.getInt(KEY_POSITION_HUD), properties.getFloat(KEY_SIZE_HUD));
            instance.setMuteOption(properties.getInt(KEY_MUTE_OPTION));
            instance.setSParams(properties.getString(KEY_S_PARAM_1), properties.getString(KEY_S_PARAM_2), properties.getString(KEY_S_PARAM_3));
            instance.setSoundRangeInfinityAllowed(properties.getBoolean(KEY_SOUND_RANGE_INFINITY_ALLOWED));
            instance.setMxTuneServerUpdateAllowed(properties.getBoolean(KEY_MXTUNE_SERVER_UPDATE_ALLOWED));
            instance.setSelectedPlayListGuid(NBTHelper.getGuidFromTag(properties, KEY_SELECTED_PLAY_LIST_GUID));
            if (properties.contains(KEY_LIST_BLACK, Constants.NBT.TAG_LIST))
            {
                ListNBT listBlack = properties.getList(KEY_LIST_BLACK, Constants.NBT.TAG_COMPOUND);
                int count = listBlack.size();
                List<ClassifiedPlayer> blackList = new ArrayList<>();
                for (int i = 0; i < count; i++)
                {
                    CompoundNBT entry = listBlack.getCompound(i);
                    ClassifiedPlayer plist = new ClassifiedPlayer();
                    plist.setPlayerName(entry.getString(KEY_PLAYER_NAME));
                    plist.setUuid(new UUID(entry.getLong(KEY_UUID_MOST), entry.getLong(KEY_UUID_LEAST)));
                    plist.setOnline(false);
                    blackList.add(plist);
                }
                instance.setBlackList(blackList);
            }
            if (properties.contains(KEY_LIST_WHITE, Constants.NBT.TAG_LIST))
            {
                ListNBT listWhite = properties.getList(KEY_LIST_WHITE, Constants.NBT.TAG_COMPOUND);
                int count = listWhite.size();
                List<ClassifiedPlayer> whiteList = new ArrayList<>();
                for (int i = 0; i < count; i++)
                {
                    CompoundNBT entry = listWhite.getCompound(i);
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
