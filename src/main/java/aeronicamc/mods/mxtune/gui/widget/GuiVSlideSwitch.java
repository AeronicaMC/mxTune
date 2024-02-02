package aeronicamc.mods.mxtune.gui.widget;

import aeronicamc.mods.mxtune.gui.MusicBlockScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

public class GuiVSlideSwitch extends MXButton
{
    private boolean onOff;

    public GuiVSlideSwitch(IPressable pOnPress)
    {
        super(0, 0, 20, 20, GuiRedstoneButton.EMPTY, pOnPress);
    }

    public boolean getOnOff() {
        return this.onOff;
    }

    public void setOnOff(boolean onOff) {
        this.onOff = onOff;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        if (this.visible)
        {
            Minecraft mc = Minecraft.getInstance();
            mc.getTextureManager().bind(MusicBlockScreen.GUI);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Icon guiVSlideSwitch$icon = getIcon();
            this.blit(pMatrixStack, this.x, this.y, guiVSlideSwitch$icon.getX(), guiVSlideSwitch$icon.getY(), this.width, this.height);
            mc.font.draw(pMatrixStack, getMessage(), this.getRight() + 5F, this.y + (height - mc.font.lineHeight)/2F, -1);
        }
    }

    private Icon getIcon() {
        Icon guiVSlideSwitch$icon;
        if (!this.active)
        {
            guiVSlideSwitch$icon = this.onOff ? Icon.ON_DISABLED : Icon.OFF_DISABLED;
        }
        else if (this.isHovered())
        {
            guiVSlideSwitch$icon = this.onOff ? Icon.ON_HOVER : Icon.OFF_HOVER;
        }
        else
        {
            guiVSlideSwitch$icon = this.onOff ? Icon.ON : Icon.OFF;
        }
        return guiVSlideSwitch$icon;
    }

    enum Icon {
        ON(216, 60),
        ON_HOVER(216, 80),
        ON_DISABLED(216, 100),
        OFF(236, 60),
        OFF_HOVER(236, 80),
        OFF_DISABLED(236, 100);

        private final int x;
        private final int y;

        Icon(int pX, int pY) {
            this.x = pX;
            this.y = pY;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }
    }
}
