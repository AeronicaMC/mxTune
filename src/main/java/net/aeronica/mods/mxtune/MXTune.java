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
package net.aeronica.mods.mxtune;

import net.aeronica.mods.mxtune.advancements.ModCriteriaTriggers;
import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.cmds.CommandMxTuneServerUpdate;
import net.aeronica.mods.mxtune.cmds.CommandSoundRange;
import net.aeronica.mods.mxtune.handler.GUIHandler;
import net.aeronica.mods.mxtune.managers.DurationTimer;
import net.aeronica.mods.mxtune.managers.ServerFileManager;
import net.aeronica.mods.mxtune.network.MultiPacketSerializedObjectManager;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.options.PlayerMusicOptionsCapability;
import net.aeronica.mods.mxtune.proxy.ServerProxy;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.MusicTab;
import net.aeronica.mods.mxtune.world.caps.chunk.ModChunkPlaylistCap;
import net.aeronica.mods.mxtune.world.caps.world.ModWorldPlaylistCap;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import static net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import static net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION,
     acceptedMinecraftVersions = Reference.ACCEPTED_MINECRAFT_VERSIONS,
     dependencies = Reference.DEPENDENTS, updateJSON = Reference.UPDATE,
     certificateFingerprint = Reference.CERTIFICATE_FINGERPRINT)

@SuppressWarnings("deprecation")
public class MXTune
{
    @Mod.Instance(Reference.MOD_ID)
    public static MXTune instance;

    @SidedProxy(clientSide = "net.aeronica.mods.mxtune.proxy.ClientProxy", serverSide = "net.aeronica.mods.mxtune.proxy.ServerProxy")
    public static ServerProxy proxy;

    public static final ItemGroup TAB_MUSIC = new MusicTab(ItemGroup.getNextID(), Reference.MOD_NAME);
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        ModLogger.setLogger(event.getModLog());
        ModCriteriaTriggers.init();
        ModWorldPlaylistCap.register();
        ModChunkPlaylistCap.register();
        PlayerMusicOptionsCapability.register();
        PacketDispatcher.registerPackets();
        proxy.preInit();
        proxy.registerEventHandlers();
        proxy.initEntities();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
        proxy.registerKeyBindings();
        proxy.initMML();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, GUIHandler.getInstance());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit();
        proxy.registerHUD();
    }

    @Mod.EventHandler
    public void onEvent(FMLFingerprintViolationEvent event) {
        FMLLog.warning("*** [mxTune] Invalid fingerprint detected! ***");
    }

    @Mod.EventHandler
    public void onEvent(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandSoundRange());
        event.registerServerCommand(new CommandMxTuneServerUpdate());
        CallBackManager.start();
        MultiPacketSerializedObjectManager.start();
        DurationTimer.start();
        FileHelper.initialize();
        ServerFileManager.start();
    }

    @Mod.EventHandler
    public void onEvent(FMLServerStoppingEvent event)
    {
        DurationTimer.shutdown();
        ServerFileManager.shutdown();
        CallBackManager.shutdown();
        MultiPacketSerializedObjectManager.shutdown();
    }

    @SubscribeEvent
    void onEvent(ClientConnectedToServerEvent event)
    {
        proxy.clientConnect(event);
    }

    @SubscribeEvent
    void onEvent(ClientDisconnectionFromServerEvent event)
    {
        proxy.clientDisconnect(event);
    }
}
