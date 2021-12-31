/*
 * Aeronica's mxTune MOD
 * Copyright 2021, Paul Boese a.k.a. Aeronica
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
package aeronicamc.mods.mxtune;


import aeronicamc.mods.mxtune.blocks.InvTestScreen;
import aeronicamc.mods.mxtune.blocks.MusicBlockScreen;
import aeronicamc.mods.mxtune.caches.FileHelper;
import aeronicamc.mods.mxtune.caches.ModDataStore;
import aeronicamc.mods.mxtune.config.MXTuneConfig;
import aeronicamc.mods.mxtune.gui.InstrumentScreen;
import aeronicamc.mods.mxtune.init.*;
import aeronicamc.mods.mxtune.managers.ActiveTune;
import aeronicamc.mods.mxtune.managers.PlayManager;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.render.MusicSourceRenderer;
import aeronicamc.mods.mxtune.render.RootedRenderer;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import aeronicamc.mods.mxtune.util.KeyHandler;
import aeronicamc.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(Reference.MOD_ID)
public class MXTune
{
    private static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
    public static final ItemGroup ITEM_GROUP = new ItemGroup(Reference.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.SHEET_MUSIC.get());
        }
    };

    public MXTune()
    {
        MXTuneConfig.register(ModLoadingContext.get());

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.registerToModEventBus(modEventBus);
        ModItems.registerToModEventBus(modEventBus);
        ModEntities.registerToModEventBus(modEventBus);
        ModContainers.registerToModEventBus(modEventBus);
        ModTileEntities.registerToModEventBus(modEventBus);
        ModSoundEvents.registerToModEventBus(modEventBus);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            MIDISystemUtil.mxTuneInit();
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::modLoadingComplete);
        }
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        ModEntities.extendEntityClassification();
        PacketDispatcher.register();
        //LivingEntityModCapProvider.register();
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        ScreenManager.register(ModContainers.INV_TEST_CONTAINER.get(), InvTestScreen::new);
        ScreenManager.register(ModContainers.MUSIC_BLOCK_CONTAINER.get(), MusicBlockScreen::new);
        ScreenManager.register(ModContainers.INSTRUMENT_CONTAINER.get(), InstrumentScreen::new);
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(ClientAudio.class);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.MUSIC_SOURCE.get(), MusicSourceRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.ROOTED_SOURCE.get(), RootedRenderer::new);
    }

    private void modLoadingComplete(FMLLoadCompleteEvent event)
    {
        // placeholder
    }

    @SubscribeEvent
    public void event(FMLServerStartingEvent event) {
        FileHelper.initialize(event.getServer());
        ActiveTune.initialize();
        ModDataStore.start();
    }

    @SubscribeEvent
    public void event(FMLServerStoppingEvent event) {
        ActiveTune.shutdown();
        PlayManager.stopAll();
        ModDataStore.shutdown();
    }

    @SubscribeEvent
    public void event(NetworkEvent.GatherLoginPayloadsEvent event) {
        // placeholder
    }

}
