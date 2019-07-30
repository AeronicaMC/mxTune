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


import net.aeronica.mods.mxtune.blocks.BlockBandAmp;
import net.aeronica.mods.mxtune.blocks.BlockPiano;
import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.caps.chunk.ModChunkPlaylistCap;
import net.aeronica.mods.mxtune.caps.player.PlayerMusicOptionsCapability;
import net.aeronica.mods.mxtune.caps.world.ModWorldPlaylistCap;
import net.aeronica.mods.mxtune.config.MXTuneConfig;
import net.aeronica.mods.mxtune.handler.KeyHandler;
import net.aeronica.mods.mxtune.managers.DurationTimer;
import net.aeronica.mods.mxtune.managers.ServerFileManager;
import net.aeronica.mods.mxtune.network.MultiPacketSerializedObjectManager;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.proxy.ServerProxy;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Reference.MOD_ID)
public class MXTune
{
    private static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

    //@SidedProxy(clientSide = "net.aeronica.mods.mxtune.proxy.ClientProxy", serverSide = "net.aeronica.mods.mxtune.proxy.ServerProxy")
    public static ServerProxy proxy;

    public static final ItemGroup TAB =  new ItemGroup(Reference.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ObjectHolders.SPINET_PIANO);
        }
    };

    public MXTune()
    {
        MXTuneConfig.register(ModLoadingContext.get());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        PacketDispatcher.register();
        //ModCriteriaTriggers.init();
        ModWorldPlaylistCap.register();
        ModChunkPlaylistCap.register();
        PlayerMusicOptionsCapability.register();
        proxy.registerEventHandlers();
        proxy.initEntities();
        LOGGER.info("HELLO FROM PREINIT");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());
        MIDISystemUtil.mxTuneInit();
        MinecraftForge.EVENT_BUS.register(ClientAudio.class);
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
    public void onEvent(FMLServerStartingEvent event)
    {
        //event.getCommandDispatcher().register(); // The NEW way plus Brigadier!
        //event.registerServerCommand(new CommandSoundRange());
        //event.registerServerCommand(new CommandMxTuneServerUpdate());
        CallBackManager.start();
        MultiPacketSerializedObjectManager.start();
        DurationTimer.start();
        FileHelper.initialize(event.getServer());
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

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
        {
            blockRegistryEvent.getRegistry().register(new BlockPiano().setRegistryName("spinet_piano"));
            blockRegistryEvent.getRegistry().register(new BlockBandAmp().setRegistryName("band_amp"));
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent)
        {
            Item.Properties properties = new Item.Properties().maxStackSize(1).group(TAB);

            itemRegistryEvent.getRegistry().register(new BlockItem(ObjectHolders.BAND_AMP, properties).setRegistryName("band_amp"));
        }
    }
    // TODO: Need a replacement for these?
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

    @ObjectHolder(Reference.MOD_ID)
    public static class ObjectHolders
    {
        public static final Block SPINET_PIANO = Miscellus.nonNullInjected();
        public static final Block BAND_AMP = Miscellus.nonNullInjected();
    }
}
