package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.MusicBlockEntity;
import aeronicamc.mods.mxtune.util.Misc;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
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
public class ModBlockEntities
{
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Reference.MOD_ID);

    private static boolean isInitialized;

    public static final RegistryObject<TileEntityType<MusicBlockEntity>>
            INV_MUSIC_BLOCK = registerTileEntityType("inv_music_block",
                                                     MusicBlockEntity::new,
                                                     ModBlocks.MUSIC_BLOCK
                                                    );

    /**
     * Registers a tile entity type with the specified tile entity factory and valid block.
     *
     * @param name              The registry name of the tile entity type
     * @param tileEntityFactory The factory used to create the tile entity instances
     * @param validBlock        The valid block for the tile entity
     * @param <T>               The tile entity class
     * @return A RegistryObject reference to the tile entity type
     */
    private static <T extends TileEntity> RegistryObject<TileEntityType<T>> registerTileEntityType(final String name, final Supplier<T> tileEntityFactory, final RegistryObject<? extends Block> validBlock) {
        return TILE_ENTITY_TYPES.register(name, () -> {
            // dataFixerType will always be null until mod data fixers are implemented
            final TileEntityType<T> tileEntityType;
            tileEntityType = TileEntityType.Builder
                    .of(tileEntityFactory, validBlock.get())
                    .build(Misc.nonNullInjected());

            return tileEntityType;
        });
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

        TILE_ENTITY_TYPES.register(modEventBus);

        isInitialized = true;
    }
}
