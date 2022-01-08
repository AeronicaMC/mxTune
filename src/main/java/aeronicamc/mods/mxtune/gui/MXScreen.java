package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.gui.widget.IHooverText;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class MXScreen extends Screen
{
    protected MXScreen(ITextComponent pTitle)
    {
        super(pTitle);
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        this.renderTooltip(pMatrixStack, new StringTextComponent(""), pMouseX, pMouseY);
    }

    @Override
    public void renderTooltip(MatrixStack pMatrixStack, ITextComponent pText, int pMouseX, int pMouseY)
    {
        children.stream().filter(p -> p instanceof IHooverText).forEach(widget -> {
            ModGuiHelper.drawHooveringHelp(pMatrixStack, this, (IHooverText) widget, pMouseX, pMouseY);
        });
    }
}

