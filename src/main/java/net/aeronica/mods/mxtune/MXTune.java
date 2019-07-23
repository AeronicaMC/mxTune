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
import net.aeronica.mods.mxtune.init.ModBlocks;
import net.aeronica.mods.mxtune.managers.DurationTimer;
import net.aeronica.mods.mxtune.managers.ServerFileManager;
import net.aeronica.mods.mxtune.network.MultiPacketSerializedObjectManager;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.options.PlayerMusicOptionsCapability;
import net.aeronica.mods.mxtune.proxy.ServerProxy;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.world.caps.chunk.ModChunkPlaylistCap;
import net.aeronica.mods.mxtune.world.caps.world.ModWorldPlaylistCap;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Reference.MOD_ID)
public class MXTune
{
    private static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
    private static SimpleChannel network = PacketDispatcher.getNetworkChannel();

    //@SidedProxy(clientSide = "net.aeronica.mods.mxtune.proxy.ClientProxy", serverSide = "net.aeronica.mods.mxtune.proxy.ServerProxy")
    public static ServerProxy proxy;

    public static final ItemGroup TAB_MUSIC =  new ItemGroup(Reference.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.SPINET_PIANO);
        }
    };

    public MXTune()
    {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ClientAudio.class);
        LOGGER.debug("SimpleChannel: {}", network);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        ModCriteriaTriggers.init();
        ModWorldPlaylistCap.register();
        ModChunkPlaylistCap.register();
        PlayerMusicOptionsCapability.register();
        proxy.registerEventHandlers();
        proxy.initEntities();
        LOGGER.info("HELLO FROM PREINIT");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
        MIDISystemUtil.mxTuneInit();
    }

    // TODO: When ti intialize key bindings?
//    @SubscribeEvent
//    public void init(FMLInitializationEvent event)
//    {
//        proxy.registerKeyBindings();
//
//        // GUI Stuff is all broke.  New ways to do stuff!
//        // NetworkRegistry.INSTANCE.registerGuiHandler(instance, GUIHandler.getInstance());
//    }

    // TODO: GUI HUD stuff?
//    @SubscribeEvent
//    public void postInit(FMLPostInitializationEvent event)
//    {
//        proxy.registerHUD();
//    }

    @SubscribeEvent
    public void onEvent(FMLFingerprintViolationEvent event) {
        LOGGER.warn("*** [mxTune] Invalid fingerprint detected! ***");
    }

    @SubscribeEvent
    public void onEvent(FMLServerStartingEvent event)
    {
        //event.getCommandDispatcher().register(); // The NEW way plus Brigadier!
        //event.registerServerCommand(new CommandSoundRange());
        //event.registerServerCommand(new CommandMxTuneServerUpdate());
        CallBackManager.start();
        MultiPacketSerializedObjectManager.start();
        DurationTimer.start();
        FileHelper.initialize();
        ServerFileManager.start();
    }

    @SubscribeEvent
    public void onEvent(FMLServerStoppingEvent event)
    {
        DurationTimer.shutdown();
        ServerFileManager.shutdown();
        CallBackManager.shutdown();
        MultiPacketSerializedObjectManager.shutdown();
    }
    // TODO: Need a replacement for these
//    @SubscribeEvent
//    void onEvent(ClientConnectedToServerEvent event)
//    {
//        proxy.clientConnect(event);
//    }
//
//    @SubscribeEvent
//    void onEvent(ClientDisconnectionFromServerEvent event)
//    {
//        proxy.clientDisconnect(event);
//    }
}
