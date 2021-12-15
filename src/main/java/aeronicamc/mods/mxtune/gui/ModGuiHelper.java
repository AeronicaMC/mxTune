package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.gui.widget.IHooverText;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
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

    public static  <T extends Screen, S extends Object>  void drawHooveringHelp(MatrixStack poseStack, T guiScreen, IHooverText widget, double mouseX, double mouseY)
    {

            if (widget.isMouseOverWidget(mouseX, mouseY) && Screen.hasShiftDown())
                guiScreen.renderWrappedToolTip(poseStack, widget.getHooverTexts(), (int) mouseX, (int) mouseY, Minecraft.getInstance().font);
    }

    public static <T extends TextFieldWidget> void clearOnMouseLeftClicked(T textFieldWidget, double mouseX, double mouseY, double mouseButton)
    {
        if (mouseButton == 1 && mouseX >= textFieldWidget.x && mouseX < textFieldWidget.x + textFieldWidget.getWidth()
                && mouseY >= textFieldWidget.y && mouseY < textFieldWidget.y + textFieldWidget.getHeight())
            textFieldWidget.setValue("");
    }
}
