package aeronicamc.mods.mxtune.gui.widget;

import aeronicamc.mods.mxtune.gui.MusicBlockScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class GuiRedstoneButton extends MXButton
{
    static final ITextComponent EMPTY = new StringTextComponent("");
    private boolean signalEnabled;
    private final ArrowFaces direction;

    public GuiRedstoneButton(ArrowFaces direction, IPressable pOnPress)
    {
        super(0, 0, 20, 20, EMPTY, pOnPress);
        this.direction = direction;
    }

    public boolean isSignalEnabled()
    {
        return this.signalEnabled;
    }

    public void setSignalEnabled(boolean signalEnabledIn)
    {
        this.signalEnabled = signalEnabledIn;
    }

    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        if (this.visible)
        {
            Minecraft.getInstance().getTextureManager().bind(MusicBlockScreen.GUI);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiRedstoneButton.Icon guiRedstoneButtonIcon;
            if (this.signalEnabled)
            {
                if (!this.active)
                {
                    guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_ENABLED_DISABLED;
                }
                else if (isHovered)
                {
                    guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_ENABLED_HOVER;
                }
                else
                {
                    guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_ENABLED;
                }
            }
            else if (!this.active)
            {
                guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_DISABLED_DISABLED;
            }
            else if (isHovered)
            {
                guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_DISABLED_HOVER;
            }
            else
            {
                guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_DISABLED_UNLOCKED;
            }

            this.blit(pMatrixStack, this.x, this.y, guiRedstoneButtonIcon.getX() + direction.getXOffset(), guiRedstoneButtonIcon.getY(), this.width, this.height);
        }
    }

    public enum Icon
    {
        SIGNAL_ENABLED(0, 184),
        SIGNAL_ENABLED_HOVER(0, 204),
        SIGNAL_ENABLED_DISABLED(0, 224),
        SIGNAL_DISABLED_UNLOCKED(20, 184),
        SIGNAL_DISABLED_HOVER(20, 204),
        SIGNAL_DISABLED_DISABLED(20, 224);

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

    public enum ArrowFaces
    {
        UP(0),
        DOWN(40),
        LEFT(80),
        RIGHT(120);

        private final int direction;

        ArrowFaces(int directionIn)
        {
            this.direction = directionIn;
        }

        public int getXOffset() { return this.direction; }
    }
}
