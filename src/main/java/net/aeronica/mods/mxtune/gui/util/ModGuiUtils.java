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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.managers.records.Area;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import java.util.List;

public class ModGuiUtils
{
    public static final ModGuiUtils INSTANCE = new ModGuiUtils(){};

    public <T extends GuiScreen> void drawHooveringButtonHelp(T guiScreen, List<GuiButton> guiButtonList, int guiLeft, int guiTop, int mouseX, int mouseY)
    {
        for(GuiButton b : guiButtonList)
            if (this.isMouseOverButton(b, guiLeft, guiTop, mouseX, mouseY))
                guiScreen.drawHoveringText(((GuiButtonHooverText) b).getHooverTexts(), mouseX, mouseY);
    }

    private <T extends GuiButton> boolean isMouseOverButton(T button, int guiLeft, int guiTop, int mouseX, int mouseY)
    {
        return (button instanceof GuiButtonHooverText) && this.isPointInRegion(button, guiLeft, guiTop, mouseX, mouseY);
    }

    private <T extends GuiButton> boolean isPointInRegion(T button, int guiLeft,  int guiTop, int pointX, int pointY)
    {
        pointX = pointX - guiLeft;
        pointY = pointY - guiTop;
        int rectX = button.x - guiLeft;
        int rectY = button.y - guiTop;
        int rectWidth = button.width;
        int rectHeight = button.height;
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }

    public static <T extends GuiTextField> void clearOnMouseLeftClicked(T guiTextField, int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 1 && mouseX >= guiTextField.x && mouseX < guiTextField.x + guiTextField.width
                && mouseY >= guiTextField.y && mouseY < guiTextField.y + guiTextField.height)
        {
            guiTextField.setText("");
        }
    }

    public static String getPlaylistName(@Nullable Area playlist)
    {
        if (playlist != null)
        {
            String temp = playlist.getName().trim();
            if (Reference.EMPTY_GUID.equals(playlist.getGUID()))
                return I18n.format("mxtune.info.playlist.null_playlist");
            else if (Reference.NO_MUSIC_GUID.equals(playlist.getGUID()))
                return I18n.format("mxtune.info.playlist.empty_playlist");
            else
                return temp;
        }
        else
            return I18n.format("mxtune.info.playlist.null_playlist");
    }
}
