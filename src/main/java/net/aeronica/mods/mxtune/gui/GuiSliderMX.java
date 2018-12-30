/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class GuiSliderMX extends GuiButton
{
    private float sliderValue;
    private float value;
    private String name;
    private boolean dragging = false;
    private final float valueStep;
    private final float valueMin;
    private final float valueMax;

    GuiSliderMX(int id, int posX, int posY, int width, int height, String name, float value, float valueMin, float valueMax, float valueStep)
    {
        super(id, posX, posY, width, height, "");
        this.width = width;
        this.height = height;
        this.sliderValue = 1.0f;
        this.valueMin = valueMin;
        this.valueMax = valueMax;
        this.valueStep = valueStep;
        this.value = value;
        this.sliderValue = this.normalizeValue(value);
        this.name = name;
        this.displayString = getSliderText();
    }

    public float getValue() {return value;}

    public void setValue(float value)
    {
        this.value = value;
        this.sliderValue = this.normalizeValue(value);
        this.displayString = getSliderText();
    }

    public int getHeight() {return this.height;}

    public int getWidth() {return this.width;}

    private String getSliderText() {return String.format("%s: %06.2f", this.name, this.value);}

    /** Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over this button. */
    @Override
    public int getHoverState(boolean mouseOver) {return 0;}

    /** Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e). */
    @Override
    protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            if (this.dragging)
            {
                this.sliderValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);

                if (this.sliderValue < 0.0F)
                {
                    this.sliderValue = 0.0F;
                }

                if (this.sliderValue > 1.0F)
                {
                    this.sliderValue = 1.0F;
                }

                value = this.denormalizeValue(this.sliderValue);
                this.displayString = getSliderText();

            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            // Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
            this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (float) (this.width - 8)), this.y, 0, 66, 4, this.height);
            this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.y, 196, 66, 4, this.height);
        }
    }

    /** Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent e). */
    @Override
    public boolean mousePressed(Minecraft mcIn, int mouseX, int mouseY)
    {
        if (super.mousePressed(mcIn, mouseX, mouseY))
        {
            this.sliderValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);

            if (this.sliderValue < 0.0F)
            {
                this.sliderValue = 0.0F;
            }

            if (this.sliderValue > 1.0F)
            {
                this.sliderValue = 1.0F;
            }

            value = this.denormalizeValue(this.sliderValue);
            this.displayString = getSliderText();
            this.dragging = true;
            return true;
        } else
        {
            return false;
        }
    }

    // Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
    @Override
    public void mouseReleased(int mouseX, int mouseY) {this.dragging = false;}

    private float normalizeValue(float param)
    {
        return MathHelper.clamp((this.snapToStepClamp(param) - this.valueMin) / (this.valueMax - this.valueMin), 0.0F, 1.0F);
    }

    private float denormalizeValue(float param)
    {
        return this.snapToStepClamp(this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp(param, 0.0F, 1.0F));
    }

    private float snapToStepClamp(float paramIn)
    {
        return MathHelper.clamp(this.snapToStep(paramIn), this.valueMin, this.valueMax);
    }

    private float snapToStep(float paramIn)
    {
        float snapped = paramIn;
        if (this.valueStep > 0.0F)
        {
            snapped = this.valueStep * (float) Math.round(paramIn / this.valueStep);
        }
        return snapped;
    }
}
