package aeronicamc.mods.mxtune.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

import java.util.Objects;

public class MXScreen extends Screen
{
    protected MXScreen(ITextComponent pTitle)
    {
        super(pTitle);
    }

    @Override
    public <T extends Widget> T addButton(T pButton) {
        return super.addButton(pButton);
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        ModGuiHelper.drawHooveringHelp(pMatrixStack, this, children, pMouseX, pMouseY);
    }

    @Override
    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public ClientPlayerEntity getPlayer()
    {
        return Objects.requireNonNull(getMinecraft().player);
    }
    public FontRenderer getFont()
    {
        return Objects.requireNonNull(getMinecraft()).font;
    }
}

