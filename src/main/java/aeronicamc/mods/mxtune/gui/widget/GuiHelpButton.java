package aeronicamc.mods.mxtune.gui.widget;

import aeronicamc.mods.mxtune.gui.MusicBlockScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class GuiHelpButton extends MXButton
{
    static final ITextComponent EMPTY = new StringTextComponent("");
    private boolean helpEnabled;

    public GuiHelpButton(IPressable pOnPress)
    {
        super(0, 0, 20, 20, EMPTY, pOnPress);
    }

    public boolean isHelpEnabled()
    {
        return this.helpEnabled;
    }

    public void setHelpEnabled(boolean helpEnabled)
    {
        this.helpEnabled = helpEnabled;
    }

    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        if (this.visible)
        {
            Minecraft.getInstance().getTextureManager().bind(MusicBlockScreen.GUI);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiHelpButton.Icon guiRedstoneButtonIcon;
            if (this.helpEnabled)
            {
                if (!this.active)
                {
                    guiRedstoneButtonIcon = GuiHelpButton.Icon.HELP_ENABLED_DISABLED;
                }
                else if (isHovered)
                {
                    guiRedstoneButtonIcon = GuiHelpButton.Icon.HELP_ENABLED_HOVER;
                }
                else
                {
                    guiRedstoneButtonIcon = GuiHelpButton.Icon.HELP_ENABLED;
                }
            }
            else if (!this.active)
            {
                guiRedstoneButtonIcon = GuiHelpButton.Icon.HELP_DISABLED_DISABLED;
            }
            else if (isHovered)
            {
                guiRedstoneButtonIcon = GuiHelpButton.Icon.HELP_DISABLED_HOVER;
            }
            else
            {
                guiRedstoneButtonIcon = GuiHelpButton.Icon.HELP_DISABLED_UNLOCKED;
            }

            this.blit(pMatrixStack, this.x, this.y, guiRedstoneButtonIcon.getX(), guiRedstoneButtonIcon.getY(), this.width, this.height);
        }
    }

    public enum Icon
    {
        HELP_ENABLED(216, 0),
        HELP_ENABLED_HOVER(216, 20),
        HELP_ENABLED_DISABLED(216, 40),
        HELP_DISABLED_UNLOCKED(236, 0),
        HELP_DISABLED_HOVER(236, 20),
        HELP_DISABLED_DISABLED(236, 40);

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
