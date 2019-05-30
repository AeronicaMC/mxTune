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

package net.aeronica.mods.mxtune.gui.mml;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StringUtils;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// Adapted from the FML GuiModList class
final class SortFileDataHelper
{
    private SortFileDataHelper() { /* NOP */}

    /**
     * SortType for lists of Path instances
     */
    public enum SortType implements Comparator<FileData>
    {
        NATURAL(24),
        ASCENDING(25){ @Override protected int compare(String name1, String name2){ return name1.compareTo(name2); }},
        DESCENDING(26){ @Override protected int compare(String name1, String name2){ return name2.compareTo(name1); }};

        private static final int NO_SORT = 0;
        private int buttonID;

        SortType(int buttonID)
        {
            this.buttonID = buttonID;
        }

        public int getButtonID() { return buttonID; }

        @Nullable
        public static SortType getSortTypeForButton(GuiButton button)
        {
            for (SortType t : values())
            {
                if (t.buttonID == button.id)
                {
                    return t;
                }
            }
            return null;
        }

        @SuppressWarnings("unused")
        protected int compare(String name1, String name2) { return NO_SORT; }

        @Override
        public int compare(FileData o1, FileData o2)
        {
            String name1 = StringUtils.stripControlCodes(o1.name).toLowerCase(Locale.ROOT);
            String name2 = StringUtils.stripControlCodes(o2.name).toLowerCase(Locale.ROOT);
            return compare(name1, name2);
        }
    }

    static void updateSortButtons(@Nullable SortType sortType, List<GuiButton> buttonList)
    {
        for (GuiButton button : buttonList)
        {
            SortType type = SortType.getSortTypeForButton(button);
            if (type != null && type == sortType)
                button.enabled = false;
            else if (type != null)
                button.enabled = true;
        }
    }
}
