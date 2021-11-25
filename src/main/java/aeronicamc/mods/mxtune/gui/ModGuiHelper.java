package aeronicamc.mods.mxtune.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;

public class ModGuiHelper
{
    public static void RenderGuiItemScaled(ItemRenderer itemRenderer, ItemStack pStack, int posX, int posY, int scale, boolean onCenter)
    {
        RenderSystem.pushMatrix();
        int xRel = (onCenter ? posX - 16*scale/2 : posX)/scale;
        int yRel = (onCenter ? posY - 16*scale/2 : posY)/scale;
        RenderSystem.scalef(scale, scale, 1F);
        itemRenderer.renderAndDecorateItem(pStack, xRel, yRel);
        RenderSystem.popMatrix();
    }
}
