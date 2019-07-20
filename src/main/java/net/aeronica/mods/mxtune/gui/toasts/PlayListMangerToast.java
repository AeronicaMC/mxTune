/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
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

package net.aeronica.mods.mxtune.gui.toasts;

import net.aeronica.mods.mxtune.init.ModItems;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class PlayListMangerToast implements IToast
{
    private long firstDrawTime;
    private boolean firstShow;
    private ItemStack itemStack = new ItemStack(ModItems.ITEM_STAFF_OF_MUSIC);

    public PlayListMangerToast() {/*  NOP */}

    @Override
    public Visibility draw(ToastGui toastGui, long delta)
    {
        if (this.firstShow)
        {
            this.firstDrawTime = delta;
            this.firstShow = false;
        }
//        if (!inputMode.equals(ModConfig.client.input_mode))
//            return IToast.Visibility.HIDE;
        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0, 32, 160, 32);
        toastGui.getMinecraft().fontRenderer.drawString(I18n.format("mxtune.gui.button.upload"), 30, 7, -11534256);
        toastGui.getMinecraft().fontRenderer.drawString(I18n.format("mxtune.gui.guiPlayListManager.title"), 30, 18, -11534256);
        RenderHelper.enableGUIStandardItemLighting();
        toastGui.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI((LivingEntity)null, this.itemStack, 8, 8);
        return delta - this.firstDrawTime >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }
}
