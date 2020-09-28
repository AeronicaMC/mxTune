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
package net.aeronica.mods.mxtune.world;

import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.util.VillagerUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

public class VillageMusician
{
    private VillageMusician() { /* NOP */ }

    @EventBusSubscriber
    public static class RegistrationHandler
    {
        private RegistrationHandler() { /* NOP */ }

        @SubscribeEvent
        public static void registerVillagers(final RegistryEvent.Register<VillagerProfession> event)
        {
            event.getRegistry().register(villagerMusician());
        }

        private static VillagerProfession villagerMusician()
        {
            VillagerProfession musician = new VillagerProfession("mxtune:musician", "mxtune:textures/entity/musican.png",
                                                                 "minecraft:textures/entity/zombie_villager/zombie_villager.png");
            VillagerCareer musicianCareer = new VillagerCareer(musician, "mxtune:musician");
            VillagerUtils.addSellTrade(musicianCareer, new ItemStack(Items.PAPER, 2), 1, 2);
            VillagerUtils.addSellTrade(musicianCareer, new ItemStack(Items.DYE, 1, 0), 1, 2);
            VillagerUtils.addSellTrade(musicianCareer, new ItemStack(ModItems.ITEM_MUSIC_PAPER, 3), 2, 3);
            VillagerUtils.addSellTrade(2, musicianCareer, new ItemStack(ModItems.ITEM_INGREDIENTS, 1, 0), 3, 5);
            VillagerUtils.addSellTrade(2, musicianCareer, new ItemStack(ModItems.ITEM_INGREDIENTS, 1, 1), 3, 5);
            VillagerUtils.addSellTrade(2, musicianCareer, new ItemStack(Items.REDSTONE, 1), 2, 4);
            VillagerUtils.addSellTrade(3, musicianCareer, new ItemStack(ModItems.ITEM_MULTI_INST, 1, 28), 4, 8);
            VillagerUtils.addSellTrade(5, musicianCareer, new ItemStack(ModItems.ITEM_SPINET_PIANO), 6, 10);
            VillagerUtils.addSellTrade(5, musicianCareer, new ItemStack(ModItems.ITEM_BAND_AMP), 8, 12);
            return musician;
        }
    }
}
