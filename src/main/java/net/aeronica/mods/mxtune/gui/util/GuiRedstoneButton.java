/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.gui.util;

import net.aeronica.mods.mxtune.gui.GuiBandAmp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GuiRedstoneButton extends GuiButtonMX
{
    private boolean signalEnabled;
    private ArrowFaces direction;

    public GuiRedstoneButton(int buttonId, int x, int y, ArrowFaces direction)
    {
        super(buttonId, x, y, 20, 20, "");
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
    public enum Icon
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