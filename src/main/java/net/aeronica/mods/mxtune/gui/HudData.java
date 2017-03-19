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
    private final Quadrant quadrant;
    
    public HudData(int posX, int posY, boolean displayLeft, boolean displayTop, Quadrant quadrant)
    {
        this.posX = posX; this.posY = posY; this.displayLeft = displayLeft; this.displayTop = displayTop; this.quadrant = quadrant;
    }

    public int getPosX() {return posX;}

    public int getPosY() {return posY;}
    
    public Quadrant getQuadrant() {return quadrant;}

    public boolean isDisplayLeft() {return displayLeft;}

    public boolean isDisplayTop() {return displayTop;}
    
    public boolean isEqual(HudData hudData)
    {
        return (posX == hudData.getPosX()) && (posY == hudData.getPosY()) && (displayLeft == hudData.isDisplayLeft()) && (displayTop == hudData.isDisplayTop());
    }
    
    public int top(int maxHeight)    { return this.isDisplayTop() ? 0 : -maxHeight; }
    public int left(int maxWidth)    { return this.isDisplayLeft() ? 0 : -maxWidth; }
    public int bottom(int maxHeight) { return this.isDisplayTop() ? maxHeight : 0; }
    public int right(int maxWidth)   { return this.isDisplayLeft() ? maxWidth : 0; }
    
    public int quadX(int maxWidth, int xIn, int padding, int xSize)
    {
        if(quadrant == Quadrant.I || quadrant == Quadrant.IV)
            return left(maxWidth) + xIn + padding + left(xSize);
        else 
            return right(maxWidth) - xIn - padding - xSize;
    }

    public int quadY(int maxHeight, int yIn, int padding, int ySize)
    {
        if(quadrant == Quadrant.III || quadrant == Quadrant.IV)
            return top(maxHeight) + yIn + padding + top(ySize);
        else
            return bottom(maxHeight) - yIn - padding - ySize;
    }
    
    /*
     * Plane Geometry
     *  II | I
     * ----+----
     * III | IV
     */
    public enum Quadrant {
        I, II, III, IV
    }
    
}
