/*
 * Aeronica's mxTune MOD
 * Copyright {2018} Paul Boese a.k.a. Aeronica
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

import net.minecraft.client.gui.GuiButton;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class GuiButtonHooverText extends GuiButton
{
    private List<String> hooverTexts = new ArrayList<>();

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
        return hooverTexts;
    }

    public void setHooverTexts(List<String> hooverTexts)
    {
        this.hooverTexts = hooverTexts;
    }

    public void addHooverText(String hooverText)
    {
        hooverTexts.add(hooverText);
    }
}
