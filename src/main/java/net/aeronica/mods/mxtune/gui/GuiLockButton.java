package net.aeronica.mods.mxtune.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GuiLockButton extends GuiButtonHooverText
{
    private boolean locked;

    public GuiLockButton(int buttonId, int x, int y)
    {
        super(buttonId, x, y, 20, 20, "");
    }

    public boolean isLocked()
    {
        return this.locked;
    }

    public void setLocked(boolean lockedIn)
    {
        this.locked = lockedIn;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            mc.getTextureManager().bindTexture(GuiButton.BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            GuiLockButton.Icon guiLockButton$icon;

            if (this.locked)
            {
                if (!this.enabled)
                {
                    guiLockButton$icon = GuiLockButton.Icon.LOCKED_DISABLED;
                }
                else if (flag)
                {
                    guiLockButton$icon = GuiLockButton.Icon.LOCKED_HOVER;
                }
                else
                {
                    guiLockButton$icon = GuiLockButton.Icon.LOCKED;
                }
            }
            else if (!this.enabled)
            {
                guiLockButton$icon = GuiLockButton.Icon.UNLOCKED_DISABLED;
            }
            else if (flag)
            {
                guiLockButton$icon = GuiLockButton.Icon.UNLOCKED_HOVER;
            }
            else
            {
                guiLockButton$icon = GuiLockButton.Icon.UNLOCKED;
            }

            this.drawTexturedModalRect(this.x, this.y, guiLockButton$icon.getX(), guiLockButton$icon.getY(), this.width, this.height);
        }
    }

    @SideOnly(Side.CLIENT)
    enum Icon
    {
        LOCKED(0, 146),
        LOCKED_HOVER(0, 166),
        LOCKED_DISABLED(0, 186),
        UNLOCKED(20, 146),
        UNLOCKED_HOVER(20, 166),
        UNLOCKED_DISABLED(20, 186);

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

