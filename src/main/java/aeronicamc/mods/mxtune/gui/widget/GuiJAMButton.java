package aeronicamc.mods.mxtune.gui.widget;

import aeronicamc.mods.mxtune.gui.MusicBlockScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class GuiJAMButton extends MXButton
{
    static final ITextComponent EMPTY = new StringTextComponent("");
    private boolean jamEnabled;

    public GuiJAMButton(IPressable pOnPress)
    {
        super(0, 0, 20, 20, EMPTY, pOnPress);
    }

    public boolean isJamEnabled()
    {
        return this.jamEnabled;
    }

    public void setJamEnabled(boolean jamEnabled)
    {
        this.jamEnabled = jamEnabled;
    }

    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        if (this.visible)
        {
            Minecraft.getInstance().getTextureManager().bind(MusicBlockScreen.GUI);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiJAMButton.Icon guiRedstoneButtonIcon;
            if (this.jamEnabled)
            {
                if (!this.active)
                {
                    guiRedstoneButtonIcon = GuiJAMButton.Icon.JAM_ENABLED_DISABLED;
                }
                else if (isHovered)
                {
                    guiRedstoneButtonIcon = GuiJAMButton.Icon.JAM_ENABLED_HOVER;
                }
                else
                {
                    guiRedstoneButtonIcon = GuiJAMButton.Icon.JAM_ENABLED;
                }
            }
            else if (!this.active)
            {
                guiRedstoneButtonIcon = GuiJAMButton.Icon.JAM_DISABLED_DISABLED;
            }
            else if (isHovered)
            {
                guiRedstoneButtonIcon = GuiJAMButton.Icon.JAM_DISABLED_HOVER;
            }
            else
            {
                guiRedstoneButtonIcon = GuiJAMButton.Icon.JAM_DISABLED_UNLOCKED;
            }

            this.blit(pMatrixStack, this.x, this.y, guiRedstoneButtonIcon.getX(), guiRedstoneButtonIcon.getY(), this.width, this.height);
        }
    }

    public enum Icon
    {
        JAM_ENABLED(216, 120),
        JAM_ENABLED_HOVER(216, 140),
        JAM_ENABLED_DISABLED(216, 160),
        JAM_DISABLED_UNLOCKED(236, 120),
        JAM_DISABLED_HOVER(236, 140),
        JAM_DISABLED_DISABLED(236, 160);

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
