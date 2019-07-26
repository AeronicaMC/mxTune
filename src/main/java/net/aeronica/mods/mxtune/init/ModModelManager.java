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

//The MIT License (MIT)
//
//        Test Mod 3 - Copyright (c) 2015-2017 Choonster
//
//        Permission is hereby granted, free of charge, to any person obtaining a copy
//        of this software and associated documentation files (the "Software"), to deal
//        in the Software without restriction, including without limitation the rights
//        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//        copies of the Software, and to permit persons to whom the Software is
//        furnished to do so, subject to the following conditions:
//
//        The above copyright notice and this permission notice shall be included in all
//        copies or substantial portions of the Software.
//
//        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//        SOFTWARE.
package net.aeronica.mods.mxtune.init;

import net.aeronica.mods.mxtune.Reference;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid=Reference.MOD_ID, value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModModelManager
{
    public static final ModModelManager INSTANCE =  new ModModelManager();
    private ModModelManager() { /* NOP */ }
    
    @SubscribeEvent
    public static void registerAllModels(ModelRegistryEvent event)
    {
        INSTANCE.registerTileRenderer();
        INSTANCE.registerItemModels();
    }
    
    private void registerTileRenderer() {
        registerTESR(TilePiano.class, new RendererPiano());
    }

    /**
     * Register this mod's {@link Item} models.
     */
    private void registerItemModels()
    {
        // Then register items with default model names
        ModItems.RegistrationHandler.ITEMS.stream().filter(item -> !itemsRegistered.contains(item)).forEach(this::registerItemModel);
    }
    
    private static List<Object> tesrRenderer = new ArrayList<>();
    
    public static List<Object> getTESRRenderer() {return tesrRenderer;}
    
    private <T extends TileEntity> void registerTESR(Class<T> tile, TileEntityRenderer<T> renderer)
    {
        ClientRegistry.bindTileEntitySpecialRenderer(tile, renderer);
        tesrRenderer.add(renderer);
    }

    private <T extends Block> void registerItemModel(T block)
    {
        registerItemModel(Item.REGISTRY.getObject(block.getRegistryName()));
    }
    
    /**
     * The {@link Item}s that have had models registered so far.
     */
    private final Set<Item> itemsRegistered = new HashSet<>();


}
