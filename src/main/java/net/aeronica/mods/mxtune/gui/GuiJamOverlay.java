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

import org.lwjgl.opengl.GL11;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
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
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class GuiJamOverlay extends Gui
{
    private Minecraft mc = null;
    private FontRenderer fontRenderer = null;
    private static final ResourceLocation textureLocation = new ResourceLocation(MXTuneMain.prependModID("textures/gui/manage_group.png"));
    
    private static class GuiJamOverlayHolder {private static final GuiJamOverlay INSTANCE = new GuiJamOverlay();}
    public static GuiJamOverlay getInstance() {return GuiJamOverlayHolder.INSTANCE;}
    
    public GuiJamOverlay()
    {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = this.mc.fontRendererObj;
    }

    private static ItemStack lastItemStack = null;
    private static int hudTimer = 0;
    private static int count = 0;
    
    @SubscribeEvent
    public void onEvent(ClientTickEvent event)
    {
        if (event.side == Side.CLIENT && event.phase == TickEvent.Phase.END)
        {
            /* 4 ticks per second */
            if ((count++ % 5 == 0) && (hudTimer > 0)) hudTimer--;
        }
    }
    
    private boolean canRenderHud(EntityPlayer playerIn)
    {        
        if (inGuiHudAdjust())
            return true;
        else return !MusicOptionsUtil.isHudDisabled(playerIn) && hudTimer > 0;
    }
    
    private boolean inGuiHudAdjust() {return (mc.currentScreen != null && mc.currentScreen instanceof GuiHudAdjust);}
    
    private static HudData hudData = null;
    private static int lastWidth = 0;
    private static int lastHeight = 0;
    
    @SuppressWarnings("static-access")
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderExperienceBar(RenderGameOverlayEvent.Post event)
    {

        if (mc.gameSettings.showDebugInfo) return;

       if (event.isCancelable() || event.getType() != ElementType.EXPERIENCE) { return; }
        EntityPlayerSP player = this.mc.thePlayer;
 
        int width = event.getResolution().getScaledWidth();
        int height = event.getResolution().getScaledHeight();
//        if (hudData == null || lastWidth != width || lastHeight != height || !hudData.isEqual(calcHudPositions(MusicOptionsUtil.getPositionHUD(player), width, height)))
//        {
//            hudData = calcHudPositions(MusicOptionsUtil.getPositionHUD(player), width, height);
//            lastWidth = width; lastHeight = height;
//        }
        hudData = calcHudPositions((inGuiHudAdjust() ? MusicOptionsUtil.getAdjustPositionHud() : MusicOptionsUtil.getPositionHUD(player)), width, height);
        
        this.mc.renderEngine.bindTexture(textureLocation);
        ItemStack currentItemStack = player.getHeldItemMainhand();
        
        if ((player.getHeldItemMainhand() != null) && (player.getHeldItemMainhand().getItem() instanceof IInstrument))
        {            
            if (lastItemStack==null || !currentItemStack.equals(this.lastItemStack)) {hudTimer = 10; lastItemStack = currentItemStack;}
            if (canRenderHud(player)) this.renderTest(hudData, player);
        } else lastItemStack = currentItemStack;
    }

    private void drawGroup(EntityPlayer player)
    {
        int posX = 6;
        int posY = 24;
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
 
    /*
     * 0 | 1
     * 2 | 3
     * 4 | 5
     * 6 | 7
     */
    public static HudData calcHudPositions(int positionHud, int width, int height)
    {
        int posX = 0;
        int posY= 0;
        boolean displayLeft = true;
        boolean displayTop = true;

        switch(positionHud)
        {
        case 0:
            break;
        case 1:
            displayLeft = false;
            posX = width;
            break;
        case 2:
            posY = height / 4;
            break;
        case 3:
            displayLeft = false;
            posX = width;
            posY = height / 4;
            break;
        case 4:
            displayTop = false;
            posY = (height / 2) + (height / 4);
            break;
        case 5:
            displayLeft = false;
            displayTop = false;
            posX = width;
            posY = (height / 2) + (height / 4);
            break;
        case 6:
            displayTop = false;
            posY = height;
            break;
        case 7:
            displayLeft = false;
            displayTop = false;
            posX = width;
            posY = height;
            break;
        default:
        }
        return new HudData(posX, posY, displayLeft, displayTop);
    }
        
    private void drawDebug()
    {
        if (GROUPS.getClientPlayStatuses() != null && !GROUPS.getClientPlayStatuses().isEmpty())
        {
            String status = new String("Play Status: " + GROUPS.getClientPlayStatuses().toString());
            fontRenderer.drawStringWithShadow(status, 6, 80, 16777215);
        }
        if (GROUPS.getPlayIDMembers() != null && !GROUPS.getPlayIDMembers().isEmpty())
        {
            String status = new String("PlayID Members: " + GROUPS.getPlayIDMembers().toString());
            fontRenderer.drawStringWithShadow(status, 6, 90, 16777215);
        }
        if (GROUPS.getActivePlayIDs() != null && !GROUPS.getActivePlayIDs().isEmpty())
        {
            String status = new String("ActivePlayIDs: " + GROUPS.getActivePlayIDs().toString());
            fontRenderer.drawStringWithShadow(status, 6, 100, 16777215);
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

    
    
    private static final int STAT_ICON_SIZE = 18;
    private static final int STAT_ICON_BASE_U_OFFSET = 0;
    private static final int STAT_ICON_BASE_V_OFFSET = 165;
    private static final int STAT_ICONS_PER_ROW = 8;

    private void renderTest(HudData hd, EntityPlayer playerIn)
    {
        String fiftyTwo = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";
        int width52 = fontRenderer.getStringWidth(fiftyTwo);
        int alphaBack = 128;
        int alphaFore = 192;
        int maxWidth = 255;
        int maxHeight = 127;
        int top = hd.isDisplayTop() ? 0 : -maxHeight;
        int left = hd.isDisplayLeft() ? 0 : -maxWidth;
        int bottom = hd.isDisplayTop() ? maxHeight : 0;
        int right = hd.isDisplayLeft() ? maxWidth : 0;

        
        GL11.glPushMatrix();
        GL11.glTranslatef(hd.getPosX(), hd.getPosY(), 0F);
        GL11.glScalef(.5F, .5F, .5F);
        drawRect(left+4, top+4, right, bottom, 0x00000000 + (alphaBack << 24));
        drawRect(left, top, right-4, bottom-4, 0xA0A0A0 + (alphaBack << 24));
        
        ItemStack is = playerIn.getHeldItemMainhand();
        int iconIndex = GROUPS.getIndex(playerIn.getEntityId());
        drawTexturedModalRect(left+4,     top+4, STAT_ICON_BASE_U_OFFSET + iconIndex % STAT_ICONS_PER_ROW * STAT_ICON_SIZE, STAT_ICON_BASE_V_OFFSET + iconIndex / STAT_ICONS_PER_ROW * STAT_ICON_SIZE,
                STAT_ICON_SIZE, STAT_ICON_SIZE);
        drawTexturedModalRect(right-4-18, top+4, STAT_ICON_BASE_U_OFFSET + iconIndex % STAT_ICONS_PER_ROW * STAT_ICON_SIZE, STAT_ICON_BASE_V_OFFSET + iconIndex / STAT_ICONS_PER_ROW * STAT_ICON_SIZE,
                STAT_ICON_SIZE, STAT_ICON_SIZE);
        drawTexturedModalRect(right-4-18, bottom-4-18, STAT_ICON_BASE_U_OFFSET + iconIndex % STAT_ICONS_PER_ROW * STAT_ICON_SIZE, STAT_ICON_BASE_V_OFFSET + iconIndex / STAT_ICONS_PER_ROW * STAT_ICON_SIZE,
                STAT_ICON_SIZE, STAT_ICON_SIZE);
        drawTexturedModalRect(left+4,     bottom-4-18, STAT_ICON_BASE_U_OFFSET + iconIndex % STAT_ICONS_PER_ROW * STAT_ICON_SIZE, STAT_ICON_BASE_V_OFFSET + iconIndex / STAT_ICONS_PER_ROW * STAT_ICON_SIZE,
                STAT_ICON_SIZE, STAT_ICON_SIZE);
        
        fontRenderer.drawStringWithShadow(getMusicTitle(is), left+22, top+8, 0x00FF00);
        int center52 = maxWidth/2 - width52/2;
        fontRenderer.drawStringWithShadow(fiftyTwo, left+center52, top+18, 0x00FFFF);
        fontRenderer.drawString(fiftyTwo, left+22, top+28, 0xFFFF00);
        drawGroup(playerIn);
        drawDebug();
        GL11.glPopMatrix();
    }
    
}
