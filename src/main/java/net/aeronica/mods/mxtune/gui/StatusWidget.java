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

import java.util.Iterator;
import java.util.Set;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * Using a 128x128 texture. All positions and sizes need to be multiplied by 2.
 * @author Paul Boese aka Aeronica
 *
 */
public class StatusWidget extends Gui
{

    public StatusWidget() {}
    
    private static final ResourceLocation textureStatusWidgets = new ResourceLocation(MXTuneMain.prependModID("textures/gui/status_widgets.png"));
    public final int WIDGET_WIDTH = 48;
    public final int WIDGET_HEIGHT = 33;
    private final int[][] notePosMembers = { {25,0},{25,8},{25,16},{25,24}, {33,4},{33,12},{33,20} };
    
    public ResourceLocation getTexture() {return textureStatusWidgets;}
        
    public void draw(EntityPlayer player, int posX, int posY)
    {
        int left = posX;
        int top = posY;
        int right = posX + WIDGET_WIDTH;
        int bottom = posY + WIDGET_HEIGHT;
        /* translucent background */
        drawRect(left, top, right, bottom, 0xA0A0A0 + (128 << 24));
        /* staff */
        drawTexturedModalRect(left, top, 0, 0, WIDGET_WIDTH, WIDGET_HEIGHT);
        /* alto clef */
        drawTexturedModalRect(left+4, top, 0, 48, 32, 32);

        Integer groupID;
        Integer memberID;
        
        /* whole notes/rests for groups */
        if (GROUPS.getClientGroups() != null || GROUPS.getClientMembers() != null)
        {
            groupID = GROUPS.getMembersGroupID(player.getEntityId());
            
            /** Only draw if player is a member of a group */
            if (groupID != null)
            {
                // int i=0;
                Set<Integer> set = GROUPS.getClientMembers().keySet();
                //for (Iterator<Integer> im = set.iterator(); im.hasNext();)
                for (int i=0; i<7; i++)
                {
                    //memberID = im.next();
                    int x = left + notePosMembers[i][0];
                    int y = top + notePosMembers[i][1]; 
                    drawTexturedModalRect(x, y, 0, 40, 8, 8);
                }
            }
            else
            {
                /* draw quarter note/rest for the solo player */
            }
        }

    }
    
       
}
