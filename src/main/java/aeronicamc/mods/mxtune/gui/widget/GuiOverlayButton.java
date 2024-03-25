package aeronicamc.mods.mxtune.gui.widget;

import aeronicamc.mods.mxtune.gui.MultiInstScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class GuiOverlayButton extends MXButton
{
    static final ITextComponent EMPTY = StringTextComponent.EMPTY;
    private boolean buttonEnabled;

    public GuiOverlayButton(IPressable pOnPress)
    {
        super(0, 0, 20, 20, EMPTY, pOnPress);
    }

    public boolean isButtonEnabled()
    {
        return this.buttonEnabled;
    }

    public void setButtonEnabled(boolean buttonEnabled)
    {
        this.buttonEnabled = buttonEnabled;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        if (this.visible)
        {
            Minecraft.getInstance().getTextureManager().bind(MultiInstScreen.GUI);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiOverlayButton.Icon guiOverlayButtonIcon;
            if (this.buttonEnabled)
            {
                if (!this.active)
                {
                    guiOverlayButtonIcon = GuiOverlayButton.Icon.BUTTON_ENABLED_DISABLED;
                }
                else if (isHovered)
                {
                    guiOverlayButtonIcon = GuiOverlayButton.Icon.BUTTON_ENABLED_HOVER;
                }
                else
                {
                    guiOverlayButtonIcon = GuiOverlayButton.Icon.BUTTON_ENABLED;
                }
            }
            else if (!this.active)
            {
                guiOverlayButtonIcon = GuiOverlayButton.Icon.BUTTON_DISABLED_DISABLED;
            }
            else if (isHovered)
            {
                guiOverlayButtonIcon = GuiOverlayButton.Icon.BUTTON_DISABLED_HOVER;
            }
            else
            {
                guiOverlayButtonIcon = GuiOverlayButton.Icon.BUTTON_DISABLED_UNLOCKED;
            }

            this.blit(pMatrixStack, this.x, this.y, guiOverlayButtonIcon.getX(), guiOverlayButtonIcon.getY(), this.width, this.height);
        }
    }

    public enum Icon
    {
        BUTTON_ENABLED(216, 0),
        BUTTON_ENABLED_HOVER(216, 20),
        BUTTON_ENABLED_DISABLED(216, 40),
        BUTTON_DISABLED_UNLOCKED(236, 0),
        BUTTON_DISABLED_HOVER(236, 20),
        BUTTON_DISABLED_DISABLED(236, 40);

        private final int x;
        private final int y;

        Icon(int xIn, int yIn)
        {
            this.x = xIn;
            this.y = yIn;
        }

        public int getX()
        {
            return this.x;
        }

        public int getY()
        {
            return this.y;
        }
    }
}
