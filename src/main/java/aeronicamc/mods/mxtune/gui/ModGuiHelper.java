package aeronicamc.mods.mxtune.gui;

import com.mojang.blaze3d.systems.RenderSystem;
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

//    public static  <T extends Screen, S extends Object>  void drawHooveringHelp(T guiScreen, List<S> hooverTexts, int guiLeft, int guiTop, double mouseX, double mouseY)
//    {
//        for(Object text : hooverTexts)
//            if (text instanceof IHooverText && ((IHooverText) text).isMouseOverWidget(guiLeft, guiTop, mouseX, mouseY) && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)))
//                guiScreen.isMouseOverWidget(((IHooverText) text).getHooverTexts(), mouseX, mouseY);
//    }

    public static <T extends TextFieldWidget> void clearOnMouseLeftClicked(T textFieldWidget, double mouseX, double mouseY, double mouseButton)
    {
        if (mouseButton == 1 && mouseX >= textFieldWidget.x && mouseX < textFieldWidget.x + textFieldWidget.getWidth()
                && mouseY >= textFieldWidget.y && mouseY < textFieldWidget.y + textFieldWidget.getHeight())
        {
            textFieldWidget.setValue("");
        }
    }
}
