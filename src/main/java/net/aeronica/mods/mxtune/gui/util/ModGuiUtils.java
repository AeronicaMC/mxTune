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
import net.aeronica.mods.mxtune.managers.records.PlayList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

public class ModGuiUtils
{
    public static final ModGuiUtils INSTANCE = new ModGuiUtils(){};

    public static boolean isPointInRegion(int x, int y, int height, int width, int guiLeft,  int guiTop, int pointX, int pointY)
    {
        pointX = pointX - guiLeft;
        pointY = pointY - guiTop;
        int rectX = x - guiLeft;
        int rectY = y - guiTop;
        int rectWidth = width;
        int rectHeight = height;
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }

    public <T extends GuiScreen, S extends Object>  void drawHooveringHelp(T guiScreen, List<S> hooverTexts, int guiLeft, int guiTop, int mouseX, int mouseY)
    {
        for(Object text : hooverTexts)
            if (text instanceof IHooverText && ((IHooverText) text).isMouseOverElement(guiLeft, guiTop, mouseX, mouseY) && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)))
                guiScreen.drawHoveringText(((IHooverText) text).getHooverTexts(), mouseX, mouseY);
    }

    public static <T extends GuiTextField> void clearOnMouseLeftClicked(T guiTextField, int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 1 && mouseX >= guiTextField.x && mouseX < guiTextField.x + guiTextField.width
                && mouseY >= guiTextField.y && mouseY < guiTextField.y + guiTextField.height)
        {
            guiTextField.setText("");
        }
    }

    public static String getPlaylistName(@Nullable PlayList playlist)
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

    public static String getLocalizedInstrumentName(String id)
    {
        return I18n.format("item.mxtune:multi_inst." + id + ".name");
    }
}
