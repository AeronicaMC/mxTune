package aeronicamc.mods.mxtune.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

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
            Minecraft.getInstance().getTextureManager().bind(Button.WIDGETS_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiLockButton.Icon guiLockButton$icon;
            if (!this.active)
            {
                guiLockButton$icon = this.locked ? GuiLockButton.Icon.LOCKED_DISABLED : GuiLockButton.Icon.UNLOCKED_DISABLED;
            }
            else if (this.isHovered())
            {
                guiLockButton$icon = this.locked ? GuiLockButton.Icon.LOCKED_HOVER : GuiLockButton.Icon.UNLOCKED_HOVER;
            }
            else
            {
                guiLockButton$icon = this.locked ? GuiLockButton.Icon.LOCKED : GuiLockButton.Icon.UNLOCKED;
            }

            this.blit(pMatrixStack, this.x, this.y, guiLockButton$icon.getX(), guiLockButton$icon.getY(), this.width, this.height);
        }
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
