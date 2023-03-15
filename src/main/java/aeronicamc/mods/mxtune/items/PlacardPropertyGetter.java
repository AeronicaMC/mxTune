package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.Reference;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 *
 *  The MIT License (MIT)
 *
 * Test Mod 3 - Copyright (c) 2015-2021 Choonster
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * @author Choonster
 *
 * @implNote 2021-Dec-01, Aeronica a.k.a Paul Boese modified to suit the needs of mxTune.
 * Used to age the sheet music over time and select progressively more yellowed and torn textures.
 */
public class PlacardPropertyGetter
{
    public static final ResourceLocation NAME = new ResourceLocation(Reference.MOD_ID, "placard_state");

    private static final IItemPropertyGetter GETTER = (pItemStack, pLevel, pEntity) ->
            !pItemStack.isEmpty() ? MathHelper.clamp(pItemStack.getDamageValue(), 0F, 6F) : 6F;

    public static void registerToItem(Item pItem)
    {
        ItemModelsProperties.register(pItem, NAME, GETTER);
    }
}
