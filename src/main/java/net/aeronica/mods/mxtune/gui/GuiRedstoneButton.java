package net.aeronica.mods.mxtune.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GuiRedstoneButton extends GuiButtonHooverText
{
    private boolean signalEnabled;
    private ArrowFaces direction;

    GuiRedstoneButton(int buttonId, int x, int y, ArrowFaces direction)
    {
        super(buttonId, x, y, 20, 20, "");
        this.direction = direction;
    }

    boolean isSignalEnabled()
    {
        return this.signalEnabled;
    }

    void setSignalEnabled(boolean signalEnabledIn)
    {
        this.signalEnabled = signalEnabledIn;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            mc.getTextureManager().bindTexture(GuiBandAmp.BG_TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            GuiRedstoneButton.Icon guiRedstoneButtonIcon;

            if (this.signalEnabled)
            {
                if (!this.enabled)
                {
                    guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_ENABLED_DISABLED;
                }
                else if (flag)
                {
                    guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_ENABLED_HOVER;
                }
                else
                {
                    guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_ENABLED;
                }
            }
            else if (!this.enabled)
            {
                guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_DISABLED_DISABLED;
            }
            else if (flag)
            {
                guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_DISABLED_HOVER;
            }
            else
            {
                guiRedstoneButtonIcon = GuiRedstoneButton.Icon.SIGNAL_DISABLED_UNLOCKED;
            }

            this.drawTexturedModalRect(this.x, this.y, guiRedstoneButtonIcon.getX() + direction.getXOffset(), guiRedstoneButtonIcon.getY(), this.width, this.height);
        }
    }

    @SideOnly(Side.CLIENT)
    enum Icon
    {
        SIGNAL_ENABLED(0, 166),
        SIGNAL_ENABLED_HOVER(0, 186),
        SIGNAL_ENABLED_DISABLED(0, 206),
        SIGNAL_DISABLED_UNLOCKED(20, 166),
        SIGNAL_DISABLED_HOVER(20, 186),
        SIGNAL_DISABLED_DISABLED(20, 206);

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

    @SideOnly(Side.CLIENT)
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