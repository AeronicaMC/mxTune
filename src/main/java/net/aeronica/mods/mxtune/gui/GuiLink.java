/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeHooks;

public class GuiLink extends GuiButton
{

    protected final AlignText alignText;

    public enum AlignText
    {
        Left, Center, Right;
    }

    public GuiLink(int buttonId, int x, int y, String buttonText, AlignText alignText)
    {
        super(buttonId, x, y, 200, 20, buttonText);
        this.alignText = alignText;
    }
    
    public GuiLink(int buttonId, int x, int y, String buttonText)
    {
        super(buttonId, x, y, 200, 20, buttonText);
        this.alignText = AlignText.Left;
    } 
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRenderer;
            ITextComponent formattedLink = ForgeHooks.newChatWithLinks(this.displayString, false);
            int stringWidth = fontrenderer.getStringWidth(formattedLink.getFormattedText());
            if (this.alignText.equals(AlignText.Left))
                this.drawString(fontrenderer, formattedLink.getFormattedText(), this.x, this.y, 0xFF0000);
            else if (this.alignText.equals(AlignText.Center))
                this.drawString(fontrenderer, formattedLink.getFormattedText(), this.x - stringWidth + stringWidth / 2, this.y, 0xFF0000);
            else if (this.alignText.equals(AlignText.Right))
                this.drawString(fontrenderer, formattedLink.getFormattedText(), this.x - stringWidth, this.y, 0xFF0000);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        boolean result = false;
        FontRenderer fontrenderer = mc.fontRenderer;
        ITextComponent formattedLink = ForgeHooks.newChatWithLinks(this.displayString, false);
        int stringWidth = fontrenderer.getStringWidth(formattedLink.getFormattedText());
        int stringHeight = fontrenderer.FONT_HEIGHT;
        if (this.alignText.equals(AlignText.Left))
            result =  this.enabled && this.visible && mouseX >= this.x && mouseX < this.x + stringWidth && mouseY >= this.y && mouseY < this.y + stringHeight;
        else if (this.alignText.equals(AlignText.Center))
            result =  this.enabled && this.visible && mouseX >= this.x - stringWidth / 2 && mouseX < this.x + stringWidth / 2 && mouseY >= this.y && mouseY < this.y + stringHeight;
        else if (this.alignText.equals(AlignText.Right))
            result =  this.enabled && this.visible && mouseX >= this.x - stringWidth && mouseX < this.x && mouseY >= this.y && mouseY < this.y + stringHeight;
            
        return result;
    }
       
    /**
     * Call this method from the parent class that extends GuiScreen e.g. "this.handleComponentClick(mmlLink.getLinkComponent());"
     * @return ITextComponent for the URL string
     */
    public ITextComponent getLinkComponent()
    {
        return ForgeHooks.newChatWithLinks(this.displayString, false);
    }
    
}
