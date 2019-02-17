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

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public class GuiButtonHooverText extends GuiButton
{
    private List<String> hooverTexts = new ArrayList<>();
    private List<String> hooverTextsCopy = new ArrayList<>();

    private String statusText = "";

    public GuiButtonHooverText(int buttonId, int x, int y, String buttonText)
    {
        super(buttonId, x, y, buttonText);
        if(!buttonText.equals("")) hooverTexts.add(buttonText);
    }

    public GuiButtonHooverText(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        if(!buttonText.equals("")) hooverTexts.add(buttonText);
    }

    public List<String> getHooverTexts()
    {
        hooverTextsCopy.clear();
        hooverTextsCopy.addAll(hooverTexts);
        if (!statusText.equals(""))hooverTextsCopy.add(statusText);
        return hooverTextsCopy;
    }

    public void addHooverText(String hooverText)
    {
        hooverTexts.add(hooverText);
    }

    public void setStatusText(String statusText)
    {
        this.statusText = statusText;
    }

    public String getStatusText()
    {
        return statusText;
    }
}
