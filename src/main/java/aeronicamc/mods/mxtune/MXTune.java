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
package aeronicamc.mods.mxtune;


import aeronicamc.mods.mxtune.blocks.*;
import aeronicamc.mods.mxtune.caches.FileHelper;
import aeronicamc.mods.mxtune.caps.LivingEntityModCapProvider;
import aeronicamc.mods.mxtune.config.MXTuneConfig;
import aeronicamc.mods.mxtune.items.GuiTestItem;
import aeronicamc.mods.mxtune.items.MusicItem;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import aeronicamc.mods.mxtune.util.AntiNull;
import aeronicamc.mods.mxtune.util.KeyHandler;
import aeronicamc.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(Reference.MOD_ID)
public class MXTune
{
    private static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
    private static final ItemGroup MOD_TAB = new ItemGroup(Reference.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.SKELETON_SKULL);
        }
    };

    public MXTune()
    {
        MXTuneConfig.register(ModLoadingContext.get());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            MIDISystemUtil.mxTuneInit();
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::modloadingComplete);
        }
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        PacketDispatcher.register();
        LivingEntityModCapProvider.register();
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        ScreenManager.register(ObjectHolders.INV_TEST_CONTAINER, InvTestScreen::new);
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(ClientAudio.class);
    }

    private void modloadingComplete(FMLLoadCompleteEvent event)
    {
        // placeholder
    }

    @SubscribeEvent
    public void event(FMLServerStartingEvent event) {
        FileHelper.initialize(event.getServer());
    }

    @SubscribeEvent
    public void event(NetworkEvent.GatherLoginPayloadsEvent event) {

    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            blockRegistryEvent.getRegistry().register(new MusicBlock().setRegistryName("music_block"));
            blockRegistryEvent.getRegistry().register(new InvTestBlock(Block.Properties.of(Material.WOOD).strength(1.5F)).setRegistryName("inv_test_block"));
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent)
        {
            Item.Properties properties = new Item.Properties().stacksTo(1).tab(MOD_TAB);

            itemRegistryEvent.getRegistry().register(new BlockItem(ObjectHolders.MUSIC_BLOCK, new Item.Properties().stacksTo(64).tab(MOD_TAB)).setRegistryName("music_block"));
            itemRegistryEvent.getRegistry().register(new MusicItem(properties).setRegistryName("music_item"));
            itemRegistryEvent.getRegistry().register(new GuiTestItem(properties).setRegistryName("gui_test_item"));
            itemRegistryEvent.getRegistry().register(new BlockItem(ObjectHolders.INV_TEST_BLOCK, new Item.Properties().stacksTo(64).tab(MOD_TAB)).setRegistryName("inv_test_block"));
        }

        @SubscribeEvent
        public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event)
        {
            event.getRegistry().register(IForgeContainerType.create((windowId, inv, data) -> new InvTestContainer(windowId, inv.player.level, data.readBlockPos(), inv, inv.player)).setRegistryName("inv_test_container"));
        }

        @SubscribeEvent
        public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event)
        {
            event.getRegistry().register(TileEntityType.Builder.of(InvTestTile::new, ObjectHolders.INV_TEST_BLOCK).build(AntiNull.nonNullInjected()).setRegistryName("inv_test_tile"));
        }
    }

    @ObjectHolder(Reference.MOD_ID)
    public static class ObjectHolders
    {
        public static final Block MUSIC_BLOCK = AntiNull.nonNullInjected();

        public static final Block INV_TEST_BLOCK = AntiNull.nonNullInjected();
        public static final ContainerType<InvTestContainer> INV_TEST_CONTAINER = AntiNull.nonNullInjected();
        public static final TileEntityType<InvTestTile> INV_TEST_TILE = AntiNull.nonNullInjected();
    }

}
