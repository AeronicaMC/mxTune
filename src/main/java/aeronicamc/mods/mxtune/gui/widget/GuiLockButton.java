package aeronicamc.mods.mxtune.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;

public class GuiLockButton extends MXButton
{
    private boolean locked;

    public GuiLockButton(IPressable pOnPress)
    {
        super(0, 0, 20, 20, GuiRedstoneButton.EMPTY, pOnPress);
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean pLocked) {
        this.locked = pLocked;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        if (this.visible)
        {
            Minecraft.getInstance().getTextureManager().bind(Widget.WIDGETS_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Icon icon = getIcon();
            this.blit(pMatrixStack, this.x, this.y, icon.getX(), icon.getY(), this.width, this.height);
        }
    }

    private Icon getIcon() {
        Icon icon;
        if (!this.active)
            icon = this.locked ? Icon.LOCKED_DISABLED : Icon.UNLOCKED_DISABLED;
        else if (this.isHovered())
            icon = this.locked ? Icon.LOCKED_HOVER : Icon.UNLOCKED_HOVER;
        else
            icon = this.locked ? Icon.LOCKED : Icon.UNLOCKED;
        return icon;
    }

    enum Icon {
        LOCKED(0, 146),
        LOCKED_HOVER(0, 166),
        LOCKED_DISABLED(0, 186),
        UNLOCKED(20, 146),
        UNLOCKED_HOVER(20, 166),
        UNLOCKED_DISABLED(20, 186);

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
