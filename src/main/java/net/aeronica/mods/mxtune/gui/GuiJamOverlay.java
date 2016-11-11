/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
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
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiJamOverlay extends Gui
{
    private Minecraft mc = null;
    private FontRenderer fontRenderer = null;
    private static final ResourceLocation textureLocation = new ResourceLocation(MXTuneMain.prependModID("textures/gui/manage_group.png"));

    public GuiJamOverlay(Minecraft mc)
    {
        super();
        this.mc = mc;
        this.fontRenderer = this.mc.fontRendererObj;
    }

    private static final int STAT_ICON_SIZE = 18;

    private static final int STAT_ICON_BASE_U_OFFSET = 0;
    private static final int STAT_ICON_BASE_V_OFFSET = 165;
    private static final int STAT_ICONS_PER_ROW = 8;

    /**
     * This event is called by GuiIngameForge during each frame by
     * GuiIngameForge.pre() and GuiIngameForce.post().
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderExperienceBar(RenderGameOverlayEvent event)
    {

        if (mc.gameSettings.showDebugInfo) return;
        //
        // We draw after the ExperienceBar has drawn. The event raised by
        // GuiIngameForge.pre()
        // will return true from isCancelable. If you call
        // event.setCanceled(true) in
        // that case, the portion of rendering which this event represents will
        // be canceled.
        // We want to draw *after* the experience bar is drawn, so we make sure
        // isCancelable() returns
        // false and that the eventType represents the ExperienceBar event.
        if (event.isCancelable() || event.getType() != ElementType.EXPERIENCE) { return; }

        EntityPlayerSP player = this.mc.thePlayer;
        // Starting position for the status bar - 2 pixels from the top left
        // corner.
        int xPos = 2;
        int yPos = 2;
        // String s1 = "";
        // String s2 = "";
        // if (GROUPS.clientGroups != null || GROUPS.clientMembers != null) {
        // s1 = GROUPS.clientGroups.toString();
        // s2 = GROUPS.clientMembers.toString();
        // }
        // GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // GL11.glDisable(GL11.GL_LIGHTING);
        // GL11.glPushMatrix();
        // fontRenderer.drawStringWithShadow(s1, 2, 22, 16777215);
        // fontRenderer.drawStringWithShadow(s2, 2, 32, 16777215);
        // GL11.glPopMatrix();
        this.mc.renderEngine.bindTexture(textureLocation);

        if ((player.getHeldItemMainhand() != null) && (player.getHeldItemMainhand().getItem() instanceof IInstrument))
        {
            int iconIndex = GROUPS.getIndex(player.getEntityId());
            this.drawTexturedModalRect(xPos, yPos, STAT_ICON_BASE_U_OFFSET + iconIndex % STAT_ICONS_PER_ROW * STAT_ICON_SIZE, STAT_ICON_BASE_V_OFFSET + iconIndex / STAT_ICONS_PER_ROW * STAT_ICON_SIZE,
                    STAT_ICON_SIZE, STAT_ICON_SIZE);

        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderText(RenderGameOverlayEvent event)
    {
        if (mc.gameSettings.showDebugInfo) return;

        if (event.isCancelable() || event.getType() != ElementType.TEXT) { return; }

        EntityPlayerSP player = this.mc.thePlayer;

        drawMusicTitle(player);
        drawGroup(player);
        drawDebug();
    }

    private void drawMusicTitle(EntityPlayer playerIn)
    {
        /** draw the music title if any */
        if ((playerIn.getHeldItemMainhand() != null) && (playerIn.getHeldItemMainhand().getItem() instanceof IInstrument))
        {
            ItemStack is = playerIn.getHeldItemMainhand();
            fontRenderer.drawStringWithShadow(getMusicTitle(is), 22, 7, 16777215);
        }
    }

    private String getMusicTitle(ItemStack stackIn)
    {
        String result = TextFormatting.YELLOW + "(empty)";
        if (stackIn == null) return result;
        String sheetMusicTitle = this.getMusicTitleRaw(stackIn);
        /** Display the title of the contained music book. */
        if (!sheetMusicTitle.isEmpty())
        {
            result = TextFormatting.GREEN + sheetMusicTitle;
        }
        return result;
    }

    private void drawGroup(EntityPlayer player)
    {
        int posX = 2;
        int posY = 22;
        Integer groupID;
        Integer memberID;
        String leaderName;
        String memberName;

        if (GROUPS.getClientGroups() != null || GROUPS.getClientMembers() != null)
        {
            groupID = GROUPS.getMembersGroupID(player.getEntityId());
            
            /** Only draw if player is a member of a group */
            if (groupID != null)
            {
                /** Always put the leader at the TOP of the list */
                leaderName = player.worldObj.getEntityByID(GROUPS.getLeaderOfGroup(groupID)).getDisplayName().getUnformattedText();
                fontRenderer.drawStringWithShadow(TextFormatting.YELLOW + leaderName, posX, posY, 16777215);
                posY += 10;
                /** display the remaining members taking care to not print the leader a 2nd time. */
                Set<Integer> set = GROUPS.getClientMembers().keySet();
                for (Iterator<Integer> im = set.iterator(); im.hasNext();)
                {
                    memberID = im.next();
                    if (groupID.equals(GROUPS.getMembersGroupID(memberID)) && !memberID.equals(GROUPS.getLeaderOfGroup(groupID)))
                    {
                        memberName = player.worldObj.getEntityByID(memberID).getDisplayName().getUnformattedText();
                        fontRenderer.drawStringWithShadow(memberName, posX, posY, 16777215);
                        posY += 10;
                    }
                }
            }
        }
    }
 
    private void drawDebug()
    {
        if (GROUPS.getClientPlayStatuses() != null && !GROUPS.getClientPlayStatuses().isEmpty())
        {
            String status = new String("Play Status: " + GROUPS.getClientPlayStatuses().toString());
            fontRenderer.drawStringWithShadow(status, 2, 60, 16777215);
        }
        if (GROUPS.getPlayIDMembers() != null && !GROUPS.getPlayIDMembers().isEmpty())
        {
            String status = new String("PlayID Members: " + GROUPS.getPlayIDMembers().toString());
            fontRenderer.drawStringWithShadow(status, 2, 70, 16777215);
        }
        if (GROUPS.getActivePlayIDs() != null && !GROUPS.getActivePlayIDs().isEmpty())
        {
            String status = new String("ActivePlayIDs: " + GROUPS.getActivePlayIDs().toString());
            fontRenderer.drawStringWithShadow(status, 2, 80, 16777215);
        }
    }
    
    private String getMusicTitleRaw(ItemStack stackIn)
    {
        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(stackIn);
        if (sheetMusic != null)
        {
            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("MusicBook");
            if (contents != null)
            {
                return sheetMusic.getDisplayName();
            }
        }
        return new String();
    }    
}
