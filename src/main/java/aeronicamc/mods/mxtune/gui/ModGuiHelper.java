package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.gui.widget.IHooverText;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class ModGuiHelper
{
    public static final ITextComponent HELP_HELP01 = new TranslationTextComponent("gui.mxtune.button.help.help01").withStyle(TextFormatting.RESET);
    public static final ITextComponent HELP_HELP02 = new TranslationTextComponent("gui.mxtune.button.help.help02").withStyle(TextFormatting.GREEN);
    public static final ITextComponent HELP_HELP03 = new TranslationTextComponent("gui.mxtune.button.help.help03").withStyle(TextFormatting.GREEN);
    public static final ITextComponent BUTTON_DISABLED = new TranslationTextComponent("gui.mxtune.button.disabled").withStyle(TextFormatting.AQUA);
    public static final ITextComponent BUTTON_ENABLED = new TranslationTextComponent("gui.mxtune.button.enabled").withStyle(TextFormatting.AQUA);

    public static void RenderGuiItemScaled(ItemRenderer itemRenderer, ItemStack pStack, int posX, int posY, int scale, boolean onCenter)
    {
        RenderSystem.pushMatrix();
        int xRel = (onCenter ? posX - 16*scale/2 : posX)/scale;
        int yRel = (onCenter ? posY - 16*scale/2 : posY)/scale;
        RenderSystem.scalef(scale, scale, 1F);
        itemRenderer.renderAndDecorateItem(pStack, xRel, yRel);
        RenderSystem.popMatrix();
    }

    public static  <T extends Screen>  void drawHooveringHelp(MatrixStack poseStack, T guiScreen, List<IGuiEventListener> widgets, double mouseX, double mouseY)
    {
        widgets.stream()
                .filter(widget -> widget instanceof IHooverText && ((IHooverText) widget).isMouseOverWidget(mouseX, mouseY)
                        && (Screen.hasShiftDown() || ((IHooverText) widget).isHooverTextOverride()))
                .forEach(widget -> guiScreen.renderWrappedToolTip(poseStack,
                        ((IHooverText) widget).getHooverTexts(),
                        (int) mouseX, (int) mouseY, Minecraft.getInstance().font));
    }

    public static <T extends TextFieldWidget> void clearOnMouseLeftClicked(T textFieldWidget, double mouseX, double mouseY, double mouseButton)
    {
        if (mouseButton == 1 && mouseX >= textFieldWidget.x && mouseX < textFieldWidget.x + textFieldWidget.getWidth()
                && mouseY >= textFieldWidget.y && mouseY < textFieldWidget.y + textFieldWidget.getHeight())
            textFieldWidget.setValue("");
    }
}
