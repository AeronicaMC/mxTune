/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.world;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.util.VillagerUtils;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

@ObjectHolder(MXTuneMain.MODID)
public class VillageMusician
{
    
    public static VillagerProfession MUSICIAN;
    
    private static void init()
    {
        VillagerProfession MUSICIAN =
                new VillagerProfession("mxtune:musician", "mxtune:textures/entity/musican.png",
                        "minecraft:textures/entity/zombie_villager/zombie_villager.png");
        VillagerCareer musician_career = new VillagerCareer(MUSICIAN, "mxtune:musician");
        VillagerUtils.addSellTrade(musician_career, new ItemStack(Items.LEATHER, 1), 1, 2);
        VillagerUtils.addSellTrade(musician_career, new ItemStack(Items.STRING, 1), 1, 2);
        VillagerUtils.addSellTrade(musician_career, new ItemStack(ModItems.ITEM_MUSIC_PAPER, 1), 1, 2);
        VillagerUtils.addSellTrade(musician_career, new ItemStack(Blocks.CLAY, 1), 1, 2);
        VillagerUtils.addSellTrade(2, musician_career, new ItemStack(Items.REDSTONE, 1), 2, 4);
        VillagerUtils.addSellTrade(3, musician_career, new ItemStack(ModItems.ITEM_INSTRUMENT, 1, ItemInstrument.EnumType.TUNED_WHISTLE.getMeta()), 2, 4);
        VillagerUtils.addSellTrade(3, musician_career, new ItemStack(ModItems.ITEM_INSTRUMENT, 1, ItemInstrument.EnumType.TUNED_FLUTE.getMeta()), 3, 6);
        VillagerUtils.addSellTrade(4, musician_career, new ItemStack(ModItems.ITEM_INSTRUMENT, 1, ItemInstrument.EnumType.ELECTRIC_GUITAR.getMeta()), 3, 6);
        VillagerUtils.addSellTrade(4, musician_career, new ItemStack(ModItems.ITEM_INSTRUMENT, 1, ItemInstrument.EnumType.HAND_CHIMES.getMeta()), 3, 6);
        VillagerUtils.addSellTrade(5, musician_career, new ItemStack(ModItems.ITEM_PIANO), 6, 10);
    }

//    @Mod.EventBusSubscriber(modid = MXTuneMain.MODID)
//    public static class RegistrationHandler {
//        @SubscribeEvent
//        public static void registerSoundEvents(final RegistryEvent.Register<VillagerProfession> event) {
//            init();
//            event.getRegistry().registerAll(
//                    MUSICIAN
//            );
//        }
//    }
}
