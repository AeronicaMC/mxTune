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

package net.aeronica.mods.mxtune.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

class HooverHelper
{
    static final HooverHelper INSTANCE = new HooverHelper(){};

    <T extends GuiScreen> void drawHooveringButtonHelp(T guiScreen, List<GuiButton> guiButtonList, int guiLeft, int guiTop, int mouseX, int mouseY)
    {
        for(GuiButton b : guiButtonList)
            if (this.isMouseOverButton(b, guiLeft, guiTop, mouseX, mouseY))
                guiScreen.drawHoveringText(((GuiButtonHooverText) b).getHooverTexts(), mouseX, mouseY);
    }

    private <T extends GuiButton> boolean isMouseOverButton( T button, int guiLeft, int guiTop, int mouseX, int mouseY)
    {
        return (button instanceof GuiButtonHooverText) && this.isPointInRegion(guiLeft, guiTop, button.x - guiLeft, button.y - guiTop, button.width, button.height, mouseX, mouseY);
    }

    private boolean isPointInRegion(int guiLeft,  int guiTop, int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY)
    {
        pointX = pointX - guiLeft;
        pointY = pointY - guiTop;
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }
}
