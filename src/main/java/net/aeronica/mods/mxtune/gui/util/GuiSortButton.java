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

import com.google.common.collect.ComparisonChain;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;

import java.util.Comparator;
import java.util.Locale;

@SuppressWarnings("unused")
public abstract class GuiSortButton<T> extends GuiButtonMX implements Comparator<T>
{
    private static final String ASCENDING = "\u25B2";
    private static final String DESCENDING = "\u25BC";
    private static final String NATURAL = "\u25AC";

    private String displayAscending = "";
    private String displayDescending = "";
    private String displayNatural = "";

    private int sortMode = 0;
    private boolean naturalOrderOption = false;

    public GuiSortButton(int buttonId, int x, int y)
    {
        super(buttonId, x, y, 20, 20, "");
        setButtonText();
    }

    public GuiSortButton(int buttonId, int x, int y, int widthIn, int heightIn)
    {
        super(buttonId, x, y, widthIn, heightIn, "");
        setButtonText();
    }

    @Override
    public int compare(T o1, T o2)
    {
        switch (getSortMode())
        {
            case 0:
                return ComparisonChain.start().compare(stripCase(o1), stripCase(o2)).result();
            case 1:
                return ComparisonChain.start().compare(stripCase(o2), stripCase(o1)).result();
            case 2:
                return 0;
            default:
                return 0;
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (isMouseOver())
        {
            sortMode++;
            setButtonText();
        }
        return super.mousePressed(mc, mouseX, mouseY);
    }

    /**
     * Override and implement this to return a string from the object type.
     *
     * @param o An object of type T
     * @return A string that can used used for ordering by Comparator#Compare
     */
    public abstract String getString(T o);

    private String stripCase(T o)
    {
        return StringUtils.stripControlCodes(getString(o)).toLowerCase(Locale.ROOT);
    }

    public int getSortMode() { return sortMode % (naturalOrderOption ? 3 : 2); }

    public void setSortMode(int sortMode)
    {
        this.sortMode = sortMode;
        setButtonText();
    }

    public void setDisplayText(String displayAscending, String displayDescending, String displayNatural)
    {
        this.displayAscending = displayAscending;
        this.displayDescending = displayDescending;
        this.displayNatural = displayNatural;
        setButtonText();
    }

    public boolean hasNaturalOrderOption() { return naturalOrderOption; }

    public void setNaturalOrderOption(boolean naturalOrderOption) { this.naturalOrderOption = naturalOrderOption; }

    private void setButtonText()
    {
        switch (getSortMode())
        {
            case 0:
                this.displayString = displayAscending.isEmpty() ? ASCENDING : displayAscending;
                break;
            case 1:
                this.displayString = displayDescending.isEmpty() ? DESCENDING : displayDescending;
                break;
            case 2:
                this.displayString = displayNatural.isEmpty() ? NATURAL : displayNatural;
                break;
            default:
        }
    }
}
