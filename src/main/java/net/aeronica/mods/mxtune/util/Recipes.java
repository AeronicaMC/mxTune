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

import net.aeronica.mods.mxtune.init.ModBlocks;
import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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
        int TUBA = ItemInstrument.EnumType.TUBA.getMetadata();
        IRecipe tubaRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, TUBA), new Object[]
        {
                "GGG",
                ".I.",
                ".I.", 
                        'I', "ingotIron",
                        'G', "nuggetGold"
        });
        GameRegistry.addRecipe(tubaRecipe);

        int MANDO = ItemInstrument.EnumType.MANDO.getMetadata();
        IRecipe mandoRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, MANDO), new Object[]
        {
                ".P.",
                ".P.",
                "PSP",
                        'P', "plankWood",
                        'S', "string"
        });
        GameRegistry.addRecipe(mandoRecipe);

        int BONGO = ItemInstrument.EnumType.BONGO.getMetadata();
        IRecipe bongoRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, BONGO), new Object[]
        {
                "LLL",
                "PSP",
                "PPP",
                        'P', "plankWood",
                        'L', "leather",
                        'S', "string"
        });
        GameRegistry.addRecipe(bongoRecipe);

        // Uses OreDictionary lookup for plankWood
        int BALAL = ItemInstrument.EnumType.BALALAIKA.getMetadata();
        IRecipe balalaikaRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, BALAL), new Object[]
        {
                ".P.",
                ".P.",
                "PIP",
                        'P', "plankWood",
                        'I', "ingotIron"
        });
        GameRegistry.addRecipe(balalaikaRecipe);

        int FLUTE = ItemInstrument.EnumType.FLUTE.getMetadata();
        IRecipe fluteRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, FLUTE), new Object[]
        {
                "P",
                "R",
                "R",
                        'P', "plankWood",
                        'R', "sugarcane"
        });
        GameRegistry.addRecipe(fluteRecipe);

        int CLARI = ItemInstrument.EnumType.CLARINET.getMetadata();
        IRecipe clariRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, CLARI), new Object[]
        {
                "R",
                "P",
                "P",
                        'P', "plankWood",
                        'R', "sugarcane"
        });
        GameRegistry.addRecipe(clariRecipe);

        int MUSICBOX = ItemInstrument.EnumType.MUSICBOX.getMetadata();
        IRecipe musicboxRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, MUSICBOX), new Object[]
        {
                "PPP",
                "PIP",
                "PPP",
                        'P', "plankWood",
                        'I', "ingotIron"
        });
        GameRegistry.addRecipe(musicboxRecipe);

        int OCARINA = ItemInstrument.EnumType.OCARINA.getMetadata();
        IRecipe ocarinaRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, OCARINA), new Object[]
        {
                " C ",
                "CCC",
                " C ",
                        'C', Items.CLAY_BALL
        });
        GameRegistry.addRecipe(ocarinaRecipe);

        int SAWTOOTH = ItemInstrument.EnumType.SAWTOOTH.getMetadata();
        IRecipe sawtoothRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, SAWTOOTH), new Object[]
        {
                "I  ",
                "I  ",
                "PP ",
                        'P', "plankWood",
                        'I', "ingotIron"
        });
        GameRegistry.addRecipe(sawtoothRecipe);

        int EGUITAR1 = ItemInstrument.EnumType.EGUITAR1.getMetadata();
        IRecipe jazzguitarRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, EGUITAR1), new Object[]
        {
                ".P.",
                ".P.",
                "PWP",
                        'P', "plankWood",
                        'W', Blocks.WOOL
        });
        GameRegistry.addRecipe(jazzguitarRecipe);
        
        int EGUITAR2 = ItemInstrument.EnumType.EGUITAR2.getMetadata();
        IRecipe mutedguitarRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, EGUITAR2), new Object[]
        {
                ".P.",
                ".P.",
                "PCP",
                        'P', "plankWood",
                        'C', Blocks.CLAY
        });
        GameRegistry.addRecipe(mutedguitarRecipe);
        
        int EGUITAR3 = ItemInstrument.EnumType.EGUITAR3.getMetadata();
        IRecipe overguitarRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, EGUITAR3), new Object[]
        {
                ".P.",
                ".P.",
                "PIP",
                        'P', "plankWood",
                        'I', Blocks.IRON_BARS
        });
        GameRegistry.addRecipe(overguitarRecipe);

        int EGUITAR4 = ItemInstrument.EnumType.EGUITAR4.getMetadata();
        IRecipe distguitarRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, EGUITAR4), new Object[]
        {
                ".P.",
                ".P.",
                "PIP",
                        'P', "plankWood",
                        'I', "dustRedstone"
        });
        GameRegistry.addRecipe(distguitarRecipe);
        
        IRecipe pianoRecipe = new ShapedOreRecipe(new ItemStack(ModBlocks.BLOCK_PIANO, 1, 0), new Object[]
        {
                "PRP",
                "PFP",
                "PPP",
                        'P', "plankWood",
                        'F', Blocks.IRON_BARS,
                        'R', Items.ITEM_FRAME
        });
        GameRegistry.addRecipe(pianoRecipe);

        IRecipe musicPaper = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_MUSIC_PAPER, 4, 0), new Object[]
        {
                "PP",
                "PP",
                "D",
                        'P', "paper",
                        'D', "dyeBlack"
        });
        GameRegistry.addRecipe(musicPaper);
    }
}
