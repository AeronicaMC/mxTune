/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.util;

import net.aeronica.mods.mxtune.init.StartupBlocks;
import net.aeronica.mods.mxtune.init.StartupItems;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class Recipes
{
    /*
       TUBA(0, "tuba", 59),
       MANDO(1, "mando", 25),
       FLUTE(2, "flute", 74),
       BONGO(3, "bongo", 117),
       BALAL(4, "balalaika", 28),
       CLARI(5, "clarinet", 72),
       MUSICBOX(6, "musicbox", 11),
       OCARINA(7, "ocarina", 80),
       SAWTOOTH(8, "sawtooth", 82),
       EGUITAR1(9, "eguitarjazz", 27),
       EGUITAR2(10, "eguitarmuted", 29),
       EGUITAR3(11, "eguitarover", 30),
       EGUITAR4(12, "eguitardist", 31);
     */
    public static void register()
    {
        int TUBA = ItemInstrument.EnumInstruments.TUBA.getMetadata();
        IRecipe tubaRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, TUBA), new Object[]
        {
                "GGG",
                ".I.",
                ".I.", 
                        'I', Items.IRON_INGOT,
                        'G', Items.GOLD_NUGGET
        });
        GameRegistry.addRecipe(tubaRecipe);

        int MANDO = ItemInstrument.EnumInstruments.MANDO.getMetadata();
        IRecipe mandoRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, MANDO), new Object[]
        {
                ".P.",
                ".P.",
                "PSP",
                        'P', "plankWood",
                        'S', Items.STRING
        });
        GameRegistry.addRecipe(mandoRecipe);

        int BONGO = ItemInstrument.EnumInstruments.BONGO.getMetadata();
        IRecipe bongoRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, BONGO), new Object[]
        {
                "LLL",
                "PSP",
                "PPP",
                        'P', "plankWood",
                        'L', Items.LEATHER,
                        'S', Items.STRING
        });
        GameRegistry.addRecipe(bongoRecipe);

        // Uses OreDictionary lookup for plankWood
        int BALAL = ItemInstrument.EnumInstruments.BALAL.getMetadata();
        IRecipe balalaikaRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, BALAL), new Object[]
        {
                ".P.",
                ".P.",
                "PIP",
                        'P', "plankWood",
                        'I', Items.IRON_INGOT
        });
        GameRegistry.addRecipe(balalaikaRecipe);

        int FLUTE = ItemInstrument.EnumInstruments.FLUTE.getMetadata();
        IRecipe fluteRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, FLUTE), new Object[]
        {
                "I",
                "I",
                "I",
                        'I', Items.IRON_INGOT
        });
        GameRegistry.addRecipe(fluteRecipe);

        int CLARI = ItemInstrument.EnumInstruments.CLARI.getMetadata();
        IRecipe clariRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, CLARI), new Object[]
        {
                "P",
                "P",
                "P",
                        'P', "plankWood"
        });
        GameRegistry.addRecipe(clariRecipe);

        int MUSICBOX = ItemInstrument.EnumInstruments.MUSICBOX.getMetadata();
        IRecipe musicboxRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, MUSICBOX), new Object[]
        {
                "PPP",
                "PIP",
                "PPP",
                        'P', "plankWood",
                        'I', Items.IRON_INGOT
        });
        GameRegistry.addRecipe(musicboxRecipe);

        int OCARINA = ItemInstrument.EnumInstruments.OCARINA.getMetadata();
        IRecipe ocarinaRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, OCARINA), new Object[]
        {
                " C ",
                "CCC",
                " C ",
                        'C', Items.CLAY_BALL
        });
        GameRegistry.addRecipe(ocarinaRecipe);

        int SAWTOOTH = ItemInstrument.EnumInstruments.SAWTOOTH.getMetadata();
        IRecipe sawtoothRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, SAWTOOTH), new Object[]
        {
                "I  ",
                "I  ",
                "PP ",
                        'P', "plankWood",
                        'I', Items.IRON_INGOT
        });
        GameRegistry.addRecipe(sawtoothRecipe);

        int EGUITAR1 = ItemInstrument.EnumInstruments.BALAL.getMetadata();
        IRecipe jazzguitarRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, EGUITAR1), new Object[]
        {
                ".P.",
                ".P.",
                "PWP",
                        'P', "plankWood",
                        'W', Blocks.WOOL
        });
        GameRegistry.addRecipe(jazzguitarRecipe);
        
        int EGUITAR2 = ItemInstrument.EnumInstruments.BALAL.getMetadata();
        IRecipe mutedguitarRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, EGUITAR2), new Object[]
        {
                ".P.",
                ".P.",
                "PCP",
                        'P', "plankWood",
                        'C', Blocks.CLAY
        });
        GameRegistry.addRecipe(mutedguitarRecipe);
        
        int EGUITAR3 = ItemInstrument.EnumInstruments.BALAL.getMetadata();
        IRecipe overguitarRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, EGUITAR3), new Object[]
        {
                ".P.",
                ".P.",
                "PIP",
                        'P', "plankWood",
                        'I', Blocks.IRON_BARS
        });
        GameRegistry.addRecipe(overguitarRecipe);

        int EGUITAR4 = ItemInstrument.EnumInstruments.BALAL.getMetadata();
        IRecipe distguitarRecipe = new ShapedOreRecipe(new ItemStack(StartupItems.item_instrument, 1, EGUITAR4), new Object[]
        {
                ".P.",
                ".P.",
                "PIP",
                        'P', "plankWood",
                        'I', Items.REDSTONE
        });
        GameRegistry.addRecipe(distguitarRecipe);
        
        IRecipe pianoRecipe = new ShapedOreRecipe(new ItemStack(StartupBlocks.item_piano, 1, 0), new Object[]
        {
                "PRP",
                "PFP",
                "PPP",
                        'P', "plankWood",
                        'F', Blocks.IRON_BARS,
                        'R', Items.ITEM_FRAME
        });
        GameRegistry.addRecipe(pianoRecipe);

        /*
         * Music Paper
         */
        final int BLACK_DYE_DAMAGE_VALUE = EnumDyeColor.BLACK.getDyeDamage();
        GameRegistry.addShapelessRecipe(new ItemStack(StartupItems.item_musicpaper, 4, 0), new Object[]
        {
                new ItemStack(Items.PAPER, 1),
                new ItemStack(Items.PAPER, 1),
                new ItemStack(Items.PAPER, 1),
                new ItemStack(Items.PAPER, 1),
                new ItemStack(Items.DYE, 1, BLACK_DYE_DAMAGE_VALUE)
        });
    }
}
