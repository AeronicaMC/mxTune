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

import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.items.ItemStaffOfMusic;
import net.aeronica.mods.mxtune.managers.records.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.DebugRendererChunkBorder;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.aeronica.mods.mxtune.gui.hud.GuiJamOverlay.HOT_BAR_CLEARANCE;

public class GuiStaffOverlay extends GuiScreen
{
   // private Minecraft mc;
    //private FontRenderer fontRenderer;
    private float partialTicks;
    //private int width;
    //private int height;
    private DebugRenderer.IDebugRenderer chunkBorder;
    private boolean showChunkBorders;

    private GuiScrollingListOf<Area> guiAreaList;

    private static class GuiStaffOverlayHolder { private static final GuiStaffOverlay INSTANCE = new GuiStaffOverlay(); }
    public static GuiStaffOverlay getInstance() { return GuiStaffOverlayHolder.INSTANCE; }

    private GuiStaffOverlay()
    {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = this.mc.fontRenderer;
        this.chunkBorder = new DebugRendererChunkBorder(mc);

        guiAreaList = new GuiScrollingListOf<Area>(this, (mc.fontRenderer.FONT_HEIGHT + 2) * 2, 200, (mc.displayHeight - HOT_BAR_CLEARANCE) / 2, 5,(mc.displayHeight - HOT_BAR_CLEARANCE) / 2, 5) {
            @Override
            protected void selectedClickedCallback(int selectedIndex) { /* NOP */ }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex) { /* NOP */ }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                Area area = get(slotIdx);
                if (area != null)
                {
                    String trimmedName = fontRenderer.trimStringToWidth(area.getName(), listWidth - 10);
                    String trimmedUUID = fontRenderer.trimStringToWidth(area.getGUID().toString(), listWidth - 10);
                    int color = isSelected(slotIdx) ? 0xFFFF00 : 0xAADDEE;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                    fontRenderer.drawStringWithShadow(trimmedUUID, (float) left + 3, (float) slotTop + 10, color);
                } else
                {
                    String name = "---GUID Conflict---";
                    String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                    int color = 0xFF0000;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                }
            }
        };
        guiAreaList.add(new Area("Shrine Path"));
        guiAreaList.add(new Area("Village Well"));
        guiAreaList.add(new Area("Great Hall"));
        guiAreaList.add(new Area("McJunk's Saloon"));
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onEvent(RenderGameOverlayEvent.Post event)
    {
        if (mc.gameSettings.showDebugInfo) return;
        RenderGameOverlayEvent.ElementType elementType = event.getType();

        if (event.isCancelable() || (elementType != RenderGameOverlayEvent.ElementType.EXPERIENCE && elementType != RenderGameOverlayEvent.ElementType.TEXT))
            return;

        EntityPlayerSP playerSP = this.mc.player;
        width = event.getResolution().getScaledWidth();
        height = event.getResolution().getScaledHeight() - HOT_BAR_CLEARANCE;
        partialTicks = event.getPartialTicks();

        showChunkBorders = playerSP.getHeldItemMainhand().getItem() instanceof ItemStaffOfMusic;

        if (showChunkBorders)
        {
            guiAreaList.drawScreen(0,0, partialTicks);
        }
    }

    @SubscribeEvent
    public void onEvent(RenderWorldLastEvent event)
    {
        if (showChunkBorders && !mc.gameSettings.showDebugInfo)
            chunkBorder.render(event.getPartialTicks(), 0);
    }

}
