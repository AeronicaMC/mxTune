package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.gui.widget.GuiHelpButton;
import aeronicamc.mods.mxtune.gui.widget.IHooverText;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.list.FileDataList;
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

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ModGuiHelper
{
    public static final ITextComponent HELP_HELP01 = new TranslationTextComponent("gui.mxtune.button.help.help01").withStyle(TextFormatting.RESET);
    public static final ITextComponent HELP_HELP02 = new TranslationTextComponent("gui.mxtune.button.help.help02").withStyle(TextFormatting.GREEN);
    public static final ITextComponent HELP_HELP03 = new TranslationTextComponent("gui.mxtune.button.help.help03").withStyle(TextFormatting.GREEN);
    public static final ITextComponent BUTTON_DISABLED = new TranslationTextComponent("gui.mxtune.button.disabled").withStyle(TextFormatting.AQUA);
    public static final ITextComponent BUTTON_ENABLED = new TranslationTextComponent("gui.mxtune.button.enabled").withStyle(TextFormatting.AQUA);
    public static final ITextComponent BUTTON_OVERLAY_HELP01 = new TranslationTextComponent("gui.mxtune.button.overlay_button.help01").withStyle(TextFormatting.RESET);
    public static final ITextComponent BUTTON_OVERLAY_HELP02 = new TranslationTextComponent("gui.mxtune.button.overlay_button.help02").withStyle(TextFormatting.YELLOW);

    private ModGuiHelper() { /* NOOP */ }

    @SuppressWarnings("deprecation")
    public static void renderGuiItemScaled(ItemRenderer itemRenderer, ItemStack pStack, int posX, int posY, int scale, boolean onCenter)
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
                        && (Screen.hasShiftDown() || ((IHooverText) widget).isHooverTextOverride()) ||
                        (widget instanceof GuiHelpButton && ((IHooverText) widget).isMouseOverWidget(mouseX, mouseY)))
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

    public enum SortType implements Comparator<FileDataList.Entry>
    {
        NORMAL { @Override protected int compare(String name1, String name2){ return 0; }},
        A_TO_Z { @Override protected int compare(String name1, String name2){ return name1.compareTo(name2); }},
        Z_TO_A { @Override protected int compare(String name1, String name2){ return name2.compareTo(name1); }};

        public MXButton button;
        protected abstract int compare(String name1, String name2);
        @Override
        public int compare(FileDataList.Entry o1, FileDataList.Entry o2)
        {
            String name1 = o1.getFileData().getName(true).toLowerCase(Locale.ROOT);
            String name2 = o2.getFileData().getName(true).toLowerCase(Locale.ROOT);
            return compare(name1, name2);
        }

        public ITextComponent getButtonText() {
            return new TranslationTextComponent("gui.mxtune.button_order." + net.minecraftforge.fml.loading.StringUtils.toLowerCase(name()));
        }
    }
}
