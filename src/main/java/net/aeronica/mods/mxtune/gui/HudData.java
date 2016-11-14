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

public class HudData
{
    
    private final int posX;
    private final int posY;
    private final boolean displayLeft;
    private final boolean displayTop;
    
    public HudData(int posX, int posY, boolean displayLeft, boolean displayTop)
    {
        this.posX = posX; this.posY = posY; this.displayLeft = displayLeft; this.displayTop = displayTop;
    }

    public int getPosX()
    {
        return posX;
    }

    public int getPosY()
    {
        return posY;
    }

    public boolean isDisplayLeft()
    {
        return displayLeft;
    }

    public boolean isDisplayTop()
    {
        return displayTop;
    }
    
    public boolean isEqual(HudData hudData)
    {
        return (posX == hudData.getPosX()) && (posY == hudData.getPosY()) && (displayLeft == hudData.isDisplayLeft()) && (displayTop == hudData.isDisplayTop());
    }

}
