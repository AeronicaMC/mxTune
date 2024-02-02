package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.items.*;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 *  The MIT License (MIT)

 * Test Mod 3 - Copyright (c) 2015-2021 Choonster

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.

 * Registers this mod's {@link IItemPropertyGetter}s.
 *
 * @author Choonster
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModItemModelProperties
{
    @SubscribeEvent
    public static void event(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            SheetMusicAgePropertyGetter.registerToItem(ModItems.SHEET_MUSIC.get());
            MusicScoreAgePropertyGetter.registerToItem(ModItems.MUSIC_SCORE.get());
            ScrapAnimationPropertyGetter.registerToItem(ModItems.SCRAP_ITEM.get());
            PlacardPropertyGetter.registerToItem(ModItems.PLACARD_ITEM.get());
            MultiInstModelPropertyGetter.registerToItem(ModItems.MULTI_INST.get());
        });
    }
}
