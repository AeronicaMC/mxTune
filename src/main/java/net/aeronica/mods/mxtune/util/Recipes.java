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

import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class Recipes
{
    /*
        -LUTE(0, "lute", 0),
        -UKULELE(1, "ukulele", 1),
        -MANDOLIN(2, "mandolin", 2),
        -WHISTLE(3, "whistle", 3),
        -RONCADORA(4, "roncadora", 4),
        -FLUTE(5, "flute", 5),
        -CHALAMEU(6, "chalameu", 6),
        -TUBA(7, "tuba", 18),
        -LYRE(8, "lyre", 19),
        -ELECTRIC_GUITAR(9, "electic_guitar", 20),
        -VIOLIN(10, "violin", 22),
        -CELLO(11, "cello", 23),
        -HARP(12, "harp", 24),
        -TUNED_FLUTE(13, "tuned_flute", 55),
        -TUNED_WHISTLE(14, "tuned_whistle", 56),
        -BASS_DRUM(15, "bass_drum", 66),
        -SNARE_DRUM(16, "snare_drum", 67),
        -CYMBELS(17, "cymbels", 68),
        -HAND_CHIMES(18, "hand_chimes", 77),
        -RECORDER(19, "recorder", MMLUtil.preset2PackedPreset(16, 74)),
        -TRUMPET(20, "trumpet", MMLUtil.preset2PackedPreset(16, 56)),
        -HARPSICORD(21, "harpsicord", MMLUtil.preset2PackedPreset(16, 6)),
        -HARPSICORD_COUPLED(22, "harpsicord_coupled", MMLUtil.preset2PackedPreset(16, 7)),
        -STANDARD(23, "standard", MMLUtil.preset2PackedPreset(128, 0)),
        -ORCHESTRA(24, "orchestra", MMLUtil.preset2PackedPreset(128, 48)),
        -PIANO
     */
    public static void register()
    {
        
        IRecipe musicPaper = new ShapelessOreRecipe(new ItemStack(ModItems.ITEM_MUSIC_PAPER, 4, 0), new Object[]
        {
                "paper", "paper", "paper", "paper", "dyeBlack"
        });
        GameRegistry.addRecipe(musicPaper);

        int mandolinMeta = ItemInstrument.EnumType.MANDOLIN.getMetadata();
        IRecipe mandolinRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, mandolinMeta), new Object[]
        {
                ".P.",
                ".P.",
                "PSP",
                        'P', "plankWood",
                        'S', "string"
        });
        GameRegistry.addRecipe(mandolinRecipe);

        int ukuleleMeta = ItemInstrument.EnumType.UKULELE.getMetadata();
        IRecipe ukuleleRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, ukuleleMeta), new Object[]
        {
                ".P.",
                ".P.",
                "PWP",
                        'P', "plankWood",
                        'W', Blocks.WOOL
        });
        GameRegistry.addRecipe(ukuleleRecipe);

        int luteMeta = ItemInstrument.EnumType.LUTE.getMetadata();
        IRecipe luteRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, luteMeta), new Object[]
        {
                ".P.",
                ".P.",
                "PIP",
                        'P', "plankWood",
                        'I', "ingotIron"
        });
        GameRegistry.addRecipe(luteRecipe);

        int whistleMeta = ItemInstrument.EnumType.WHISTLE.getMetadata();
        IRecipe whistleRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, whistleMeta), new Object[]
        {
                "C",
                "C",
                "C",
                        'C', Items.CLAY_BALL
        });
        GameRegistry.addRecipe(whistleRecipe);

        ItemStack whistleStack = new ItemStack(ModItems.ITEM_INSTRUMENT, 1, whistleMeta);
        int tunedWhistleMeta = ItemInstrument.EnumType.TUNED_WHISTLE.getMetadata();
        IRecipe tunedWhistleRecipe = new ShapelessOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, tunedWhistleMeta), new Object[]
        {
                whistleStack,
                "nuggetGold"
        });

        GameRegistry.addRecipe(tunedWhistleRecipe);

        int roncadoraMeta = ItemInstrument.EnumType.RONCADORA.getMetadata();
        IRecipe roncadoraRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, roncadoraMeta), new Object[]
        {
                "R",
                "C",
                "C",
                        'C', Items.CLAY_BALL,
                        'R', "sugarcane"
        });
        GameRegistry.addRecipe(roncadoraRecipe);

        int fluteMeta = ItemInstrument.EnumType.FLUTE.getMetadata();
        IRecipe fluteRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, fluteMeta), new Object[]
        {
                "P",
                "R",
                "R",
                        'P', "plankWood",
                        'R', "sugarcane"
        });
        GameRegistry.addRecipe(fluteRecipe);

        ItemStack fluteStack = new ItemStack(ModItems.ITEM_INSTRUMENT, 1, fluteMeta);
        int tunedFluteMeta = ItemInstrument.EnumType.TUNED_FLUTE.getMetadata();
        IRecipe tuneFluteReciped = new ShapelessOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, tunedFluteMeta), new Object[]
        {
                fluteStack,
                "nuggetGold"
        });
        GameRegistry.addRecipe(tuneFluteReciped);

        int chalameuMeta = ItemInstrument.EnumType.CHALAMEU.getMetadata();
        IRecipe chalameuRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, chalameuMeta), new Object[]
        {
                "R",
                "P",
                "P",
                        'P', "plankWood",
                        'R', "sugarcane"
        });
        GameRegistry.addRecipe(chalameuRecipe);

        int recorderMeta = ItemInstrument.EnumType.RECORDER.getMetadata();
        IRecipe recorderRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, recorderMeta), new Object[]
        {
                "B",
                "P",
                "P",
                        'P', "plankWood",
                        'B', "bone"
        });
        GameRegistry.addRecipe(recorderRecipe);

        int tubaMeta = ItemInstrument.EnumType.TUBA.getMetadata();
        IRecipe tubaRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, tubaMeta), new Object[]
        {
                "GGG",
                ".I.",
                ".I.", 
                        'I', "ingotIron",
                        'G', "nuggetGold"
        });
        GameRegistry.addRecipe(tubaRecipe);

        int trumpetMeta = ItemInstrument.EnumType.TRUMPET.getMetadata();
        IRecipe trumpetRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, trumpetMeta), new Object[]
        {
                "GGG",
                ".I.",
                ".I.", 
                        'I', "nuggetIron",
                        'G', "nuggetGold"
        });
        GameRegistry.addRecipe(trumpetRecipe);

        int lyreMeta = ItemInstrument.EnumType.LYRE.getMetadata();
        IRecipe lyreRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, lyreMeta), new Object[]
        {
                "PP.",
                "SSS",
                ".PP", 
                        'S', "string",
                        'P', "plankWood"
        });
        GameRegistry.addRecipe(lyreRecipe);

        int harpMeta = ItemInstrument.EnumType.HARP.getMetadata();
        IRecipe harpRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, harpMeta), new Object[]
        {
                "PP.",
                "SSS",
                ".II", 
                        'S', "string",
                        'P', "plankWood",
                        'I', "ingotIron",
        });
        GameRegistry.addRecipe(harpRecipe);

        int electricGuitarMeta = ItemInstrument.EnumType.ELECTRIC_GUITAR.getMetadata();
        IRecipe electricGuitarRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, electricGuitarMeta), new Object[]
        {
                ".P.",
                ".P.",
                "PIP",
                        'P', "plankWood",
                        'I', "dustRedstone"
        });
        GameRegistry.addRecipe(electricGuitarRecipe);

        int bassDrumMeta = ItemInstrument.EnumType.BASS_DRUM.getMetadata();
        IRecipe bassDrumRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, bassDrumMeta), new Object[]
        {
                "PPL",
                "PIL",
                "PPL",
                        'P', "plankWood",
                        'I', "ingotIron",
                        'L', "leather"
        });
        GameRegistry.addRecipe(bassDrumRecipe);

        int snareDrumMeta = ItemInstrument.EnumType.SNARE_DRUM.getMetadata();
        IRecipe snareDrumRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, snareDrumMeta), new Object[]
        {
                "LLL",
                "PSP",
                "PPP",
                        'P', "plankWood",
                        'L', "leather",
                        'S', "string"
        });
        GameRegistry.addRecipe(snareDrumRecipe);

        int cymbelsMeta = ItemInstrument.EnumType.CYMBELS.getMetadata();
        IRecipe cymbelsRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, cymbelsMeta), new Object[]
        {
                "GG.",
                "GI.",
                ".II",
                        'G', "nuggetGold",
                        'I', "nuggetIron"
        });
        GameRegistry.addRecipe(cymbelsRecipe);

        int handChimesMeta = ItemInstrument.EnumType.HAND_CHIMES.getMetadata();
        IRecipe handChimesRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, handChimesMeta), new Object[]
        {
                "PPP",
                "III",
                "PPP",
                        'P', "plankWood",
                        'I', "nuggetIron"
        });
        GameRegistry.addRecipe(handChimesRecipe);
        
        int celloMeta = ItemInstrument.EnumType.CELLO.getMetadata();
        IRecipe celloRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, celloMeta), new Object[]
        {
                ".P.",
                "PPP",
                "PBP",
                        'P', "plankWood",
                        'B', "bone"
        });
        GameRegistry.addRecipe(celloRecipe);
        
        int violinMeta = ItemInstrument.EnumType.VIOLIN.getMetadata();
        IRecipe violinRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, violinMeta), new Object[]
        {
                ".P.",
                "PPP",
                "PDP",
                        'P', "plankWood",
                        'D', "dyeRed"
        });
        GameRegistry.addRecipe(violinRecipe);
        
        IRecipe pianoRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_PIANO, 1, 0), new Object[]
        {
                "PRP",
                "PFP",
                "PPP",
                        'P', "plankWood",
                        'F', Blocks.IRON_BARS,
                        'R', Items.ITEM_FRAME
        });
        GameRegistry.addRecipe(pianoRecipe);
        
        int hapsicordMeta = ItemInstrument.EnumType.HARPSICORD.getMetadata();
        IRecipe harsicordRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, hapsicordMeta), new Object[]
        {
                "PRP",
                "PFP",
                "PPP",
                        'P', "plankWood",
                        'F', "feather",
                        'R', Items.ITEM_FRAME
        });
        GameRegistry.addRecipe(harsicordRecipe);
        
        int harpsicordCoupledMeta = ItemInstrument.EnumType.HARPSICORD_COUPLED.getMetadata();
        IRecipe harpsicordCoupledRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, harpsicordCoupledMeta), new Object[]
        {
                "PRP",
                "PFP",
                "PFP",
                        'P', "plankWood",
                        'F', "feather",
                        'R', Items.ITEM_FRAME
        });
        GameRegistry.addRecipe(harpsicordCoupledRecipe);

        int orchestraMeta = ItemInstrument.EnumType.ORCHESTRA.getMetadata();
        IRecipe orchestraRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, orchestraMeta), new Object[]
        {
                "PPL",
                "PIL",
                "PPL",
                        'P', "plankWood",
                        'I', "ingotGold",
                        'L', "leather"
        });
        GameRegistry.addRecipe(orchestraRecipe);
    
        int standardMeta = ItemInstrument.EnumType.STANDARD.getMetadata();
        IRecipe standardRecipe = new ShapedOreRecipe(new ItemStack(ModItems.ITEM_INSTRUMENT, 1, standardMeta), new Object[]
        {
                "LLL",
                "PIP",
                "PPP",
                        'P', "plankWood",
                        'L', "leather",
                        'I', "nuggetGold"
        });
        GameRegistry.addRecipe(standardRecipe);
    }   
}
