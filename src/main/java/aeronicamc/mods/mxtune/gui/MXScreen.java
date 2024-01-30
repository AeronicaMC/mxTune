package aeronicamc.mods.mxtune.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

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
        ModGuiHelper.drawHooveringHelp(pMatrixStack, this, children, pMouseX, pMouseY);
    }
}

