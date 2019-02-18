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

package net.aeronica.mods.mxtune.gui.hud;

public class HudDataFactory
{

    private HudDataFactory() {}
    
    /* Position / Quadrant
     * 0 IV | 1 III
     * 2 IV | 3 III
     * 4 I  | 5 II
     * 6 I  | 7 II
     */
    public static HudData calcHudPositions(int positionHud, int width, int height)
    {
        int posX = 0;
        int posY= 0;
        boolean displayLeft = true;
        boolean displayTop = true;
        HudData.Quadrant quadrant = HudData.Quadrant.IV;

        switch(positionHud)
        {
        case 0:
            break;
        case 1:
            displayLeft = false;
            posX = width;
            quadrant = HudData.Quadrant.III;
            break;
        case 2:
            posY = height / 4;
            break;
        case 3:
            displayLeft = false;
            posX = width;
            posY = height / 4;
            quadrant = HudData.Quadrant.III;
            break;
        case 4:
            displayTop = false;
            posY = (height / 2) + (height / 4);
            quadrant = HudData.Quadrant.I;
            break;
        case 5:
            displayLeft = false;
            displayTop = false;
            posX = width;
            posY = (height / 2) + (height / 4);
            quadrant = HudData.Quadrant.II;
            break;
        case 6:
            displayTop = false;
            posY = height;
            quadrant = HudData.Quadrant.I;
            break;
        case 7:
            displayLeft = false;
            displayTop = false;
            posX = width;
            posY = height;
            quadrant = HudData.Quadrant.II;
            break;
        default:
        }
        return new HudData(posX, posY, displayLeft, displayTop, quadrant);
    }
    
}
