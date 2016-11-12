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

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiHudAdjust extends GuiScreen
{

    private GuiScreen callingScreen;
    
    public GuiHudAdjust(GuiScreen callingScreen)
    {
        this.callingScreen = callingScreen;
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        // TODO Stuff w da buttons
        // go back to the calling screen here
        super.actionPerformed(button);
    }

    @Override
    public void initGui()
    {
        // create buttons and initialize elements
    }

    @Override
    public void updateScreen()
    {
        // TODO Do component updates here
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        // TODO titles, labels, enclosed lists . . .
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    @Override
    public void onGuiClosed()
    {
        // TODO Auto-generated method stub
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {return false;}
    
    public Minecraft getMinecraftInstance() {return mc;}

    public FontRenderer getFontRenderer() {return mc.fontRendererObj;}

}
