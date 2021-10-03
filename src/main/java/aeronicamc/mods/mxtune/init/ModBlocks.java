package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.MXTune;
import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.InvTestBlock;
import aeronicamc.mods.mxtune.blocks.MusicBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 *
 Licenses for assets can be found in ASSETS_LICENSE.txt. All other files are licensed under the terms below:

 The MIT License (MIT)

 Test Mod 3 - Copyright (c) 2015-2021 Choonster

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 *
 * @author Choonster
 */
public class ModBlocks
{
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);
    private static boolean isInitialized;

    public static final RegistryObject<MusicBlock> MUSIC_BLOCK = registerBlock("music_block", MusicBlock::new);

    public static final RegistryObject<InvTestBlock> INV_TEST_BLOCK = registerBlock("inv_test_block",
        () -> new InvTestBlock(Block.Properties.of(Material.WOOD).strength(1.5F)));

    private static <BLOCK extends Block> RegistryObject<BLOCK> registerBlock(final String name, final Supplier<BLOCK> blockFactory) {
        return registerBlock(name, blockFactory, block -> new BlockItem(block, defaultItemProperties()));
    }

    private static <BLOCK extends Block> RegistryObject<BLOCK> registerBlock(final String name, final Supplier<BLOCK> blockFactory, final IBlockItemFactory<BLOCK> itemFactory) {
        final RegistryObject<BLOCK> block = BLOCKS.register(name, blockFactory);

        ITEMS.register(name, () -> itemFactory.create(block.get()));

        return block;
    }

    /**
     * Gets an {@link Item.Properties} instance with the {@link ItemGroup} set to {@link MXTune#ITEM_GROUP}.
     *
     * @return The item properties
     */
    private static Item.Properties defaultItemProperties() {
        return new Item.Properties().tab(MXTune.ITEM_GROUP);
    }

    /**
     * A factory function used to create block items.
     *
     * @param <BLOCK> The block type
     */
    @FunctionalInterface
    private interface IBlockItemFactory<BLOCK extends Block> {
        Item create(BLOCK block);
    }

    /**
     * Registers the {@link DeferredRegister} instance with the mod event bus.
     * <p>
     * This should be called during mod construction.
     *
     * @param modEventBus The mod event bus
     */
    public static void registerToModEventBus(final IEventBus modEventBus)
    {
        if (isInitialized) {
            throw new IllegalStateException("Already initialized");
        }

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        isInitialized = true;
    }
}
