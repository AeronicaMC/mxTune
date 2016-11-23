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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
    public final int WIDGET_WIDTH = 112;
    public final int WIDGET_HEIGHT = 80;
    
    public ResourceLocation getTexture() {return textureStatusWidgets;}
        
    public void draw(EntityPlayer player, int posX, int posY)
    {
        int[][] notePosMembers = { {50,4},{50,20},{50,36},{50,52}, {68,12},{68,28},{68,44}, {68,60} };

        int left = posX;
        int top = posY;
        int right = posX + WIDGET_WIDTH;
        int bottom = posY + WIDGET_HEIGHT;
        /* staff */
        drawTexturedModalRect(left, top, 0, 0, WIDGET_WIDTH, WIDGET_HEIGHT);
        /* alto clef */
//        drawTexturedModalRect(left+4, top, 0, 48, 32, 32);

        Integer groupID;
        Integer memberID;
        
        /* whole notes/rests for groups */
        if (GROUPS.getClientGroups() != null || GROUPS.getClientMembers() != null)
        {
            groupID = GROUPS.getMembersGroupID(player.getEntityId());
            int i = 0;
            /** Only draw if player is a member of a group */
            if (groupID != null)
            {
                Set<Integer> set = GROUPS.getClientMembers().keySet();
                for (Iterator<Integer> im = set.iterator(); im.hasNext();)
                {
                    memberID = im.next();
                    if (i<notePosMembers.length)
                    {
                        int x = left + notePosMembers[i][0];
                        int y = top + notePosMembers[i][1]; 
                        drawTexturedModalRect(x, y, 0, 104, 24, 16);
                    }
                }
            }
            else
            {
                /* draw whole note/rest for the solo player */
                int x = left + notePosMembers[0][0];
                int y = top + notePosMembers[0][1]; 
                drawTexturedModalRect(x, y, 0, 104, 24, 16);
            }
        }

    }

    /**
     * Copied from the vanilla Gui class, but removed the GlStateManager blend enable/disable method calls.
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param color
     */
    public static void drawRect(int left, int top, int right, int bottom, int color)
    {
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos((double)left, (double)bottom, 0.0D).endVertex();
        vertexbuffer.pos((double)right, (double)bottom, 0.0D).endVertex();
        vertexbuffer.pos((double)right, (double)top, 0.0D).endVertex();
        vertexbuffer.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }
    
}
