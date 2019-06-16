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

import net.aeronica.mods.mxtune.gui.hud.HudData;
import net.aeronica.mods.mxtune.gui.hud.HudDataFactory;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.items.ItemChunkTool;
import net.aeronica.mods.mxtune.items.ItemStaffOfMusic;
import net.aeronica.mods.mxtune.managers.ClientFileManager;
import net.aeronica.mods.mxtune.managers.ClientPlayManager;
import net.aeronica.mods.mxtune.managers.records.PlayList;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.world.caps.chunk.ModChunkPlaylistHelper;
import net.aeronica.mods.mxtune.world.caps.world.ModWorldPlaylistHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.DebugRendererChunkBorder;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static net.aeronica.mods.mxtune.gui.hud.GuiJamOverlay.HOT_BAR_CLEARANCE;

public class GuiStaffOverlay extends Gui
{
    private Minecraft mc;
    private FontRenderer fontRenderer;
    private DebugRenderer.IDebugRenderer chunkBorder;
    private boolean holdingTriggerItem;
    private boolean holdingStaffOfMusic;
    private boolean holdingChunkTool;


    private static class GuiStaffOverlayHolder { private static final GuiStaffOverlay INSTANCE = new GuiStaffOverlay(); }
    public static GuiStaffOverlay getInstance() { return GuiStaffOverlayHolder.INSTANCE; }

    private GuiStaffOverlay()
    {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = this.mc.fontRenderer;
        this.chunkBorder = new DebugRendererChunkBorder(mc);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onEvent(RenderGameOverlayEvent.Post event)
    {
        if (mc.gameSettings.showDebugInfo) return;
        RenderGameOverlayEvent.ElementType elementType = event.getType();

        if (event.isCancelable() || (elementType != RenderGameOverlayEvent.ElementType.EXPERIENCE && elementType != RenderGameOverlayEvent.ElementType.TEXT))
            return;

        EntityPlayerSP playerSP = this.mc.player;
        Item heldItem = playerSP.getHeldItemMainhand().getItem();
        int width = event.getResolution().getScaledWidth();
        int height = event.getResolution().getScaledHeight() - HOT_BAR_CLEARANCE;
        HudData hudData = HudDataFactory.calcHudPositions(0, width, height);

        holdingStaffOfMusic = heldItem instanceof ItemStaffOfMusic;
        holdingChunkTool = heldItem instanceof ItemChunkTool;
        holdingTriggerItem =  holdingStaffOfMusic || holdingChunkTool;

        if (holdingStaffOfMusic)
            renderHud(hudData, width, elementType, this::drawPlayListInfo, 6);
        if (holdingChunkTool)
            renderHud(hudData, width, elementType, this::drawChunkToolInfo, 6);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEvent(RenderWorldLastEvent event)
    {
        if (holdingTriggerItem && !mc.gameSettings.showDebugInfo)
            chunkBorder.render(event.getPartialTicks(), 0);
    }

    public void setTexture(ResourceLocation texture) { this.mc.renderEngine.bindTexture(texture);}

    private void renderHud(HudData hd, int width, RenderGameOverlayEvent.ElementType elementType, HudInfo hudInfo, int numLines)
    {
        int maxWidth = width - 4;
        int maxHeight = (fontRenderer.FONT_HEIGHT + 2) * numLines;

        GL11.glPushMatrix();
        GL11.glTranslatef(hd.getPosX(), hd.getPosY(), 0F);
        if (elementType == RenderGameOverlayEvent.ElementType.TEXT)
            hudInfo.drawHud(hd, maxWidth, maxHeight);

        int iconX = hd.quadX(maxWidth, 0, 2, maxWidth);
        int iconY = hd.quadY(maxHeight, 0, 2, maxHeight);

        if (elementType == RenderGameOverlayEvent.ElementType.EXPERIENCE)
            drawBox(iconX, iconY, maxWidth, maxHeight);

        GL11.glPopMatrix();
    }

    private void drawPlayListInfo(HudData hd, int maxWidth, int maxHeight)
    {
        int fontHeight = fontRenderer.FONT_HEIGHT + 2;
        int y = 0;
        boolean isCtrlDown = MusicOptionsUtil.isCtrlKeyDown(mc.player);
        String normalBoldUnderline = isCtrlDown ? TextFormatting.BOLD + TextFormatting.UNDERLINE.toString() : TextFormatting.RESET.toString();

        // Chunk Playlist
        BlockPos pos = mc.player.getPosition();
        Chunk chunk = mc.world.getChunk(pos);
        GUID chunkPlayListGuid = ModChunkPlaylistHelper.getPlaylistGuid(chunk);
        PlayList chunkPlaylists = ClientFileManager.getPlayList(chunkPlayListGuid);
        String chunkPlaylistName = ModGuiUtils.getPlaylistName(chunkPlaylists);

        // World Playlist
        GUID worldPlayListGuid = ModWorldPlaylistHelper.getPlaylistGuid(mc.world);
        PlayList worldPlaylists = ClientFileManager.getPlayList(worldPlayListGuid);
        String worldPlaylistName = ModGuiUtils.getPlaylistName(worldPlaylists);

        // Selected Playlist
        PlayList selectedPlaylistToApply = ClientFileManager.getPlayList(MusicOptionsUtil.getSelectedPlayListGuid(mc.player));
        String selectedPlaylistName = ModGuiUtils.getPlaylistName(selectedPlaylistToApply);

        // Draw texts

        String delayTimer = ClientPlayManager.getDelayTimerDisplay();
        int delayWidth = fontRenderer.getStringWidth(delayTimer);
        int spaceWidth = fontRenderer.getStringWidth(" ");
        int widthMinusDelayTimerText = (maxWidth - delayWidth - 2) / spaceWidth;
        StringBuilder paddedText = new StringBuilder();
        for (int i = 0; i < widthMinusDelayTimerText; i++)
            paddedText.append(" ");

        paddedText.append(ClientPlayManager.getDelayTimerDisplay());

        renderLine(paddedText.toString(), y, hd, maxWidth, maxHeight, fontHeight);

        String formattedText = I18n.format("mxtune.gui.guiStaffOverlay.play_list_name_world_chunk", worldPlaylistName,
                                           String.format("%+d", chunk.x), String.format("%+d", chunk.z),
                                           chunkPlaylistName);
        y += fontHeight;
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight);

        y += fontHeight;
        formattedText = I18n.format("mxtune.gui.guiStaffOverlay.help");
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0xAAAAAA);

        y += fontHeight;
        formattedText = ClientPlayManager.getLastSongLine01();
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0xFFCC00);

        y += fontHeight;
        formattedText = ClientPlayManager.getLastSongLine02();
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0xFFFF00);

        y += fontHeight;
        formattedText = normalBoldUnderline + I18n.format("mxtune.gui.guiStaffOverlay.selected_play_list_to_apply", selectedPlaylistName);
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, isCtrlDown ? 0x00FF00 : 0x7FFFFF);
    }

    private void drawChunkToolInfo(HudData hd, int maxWidth, int maxHeight)
    {
        int fontHeight = fontRenderer.FONT_HEIGHT + 2;
        int y = fontHeight;

        // Selected Playlist
        PlayList selectedPlaylistToApply = ClientFileManager.getPlayList(MusicOptionsUtil.getSelectedPlayListGuid(mc.player));
        String selectedPlaylistName = ModGuiUtils.getPlaylistName(selectedPlaylistToApply);

        // This Chunk Playlist
        BlockPos pos = mc.player.getPosition();
        Chunk chunk = mc.world.getChunk(pos);
        GUID chunkPlayListGuid = ModChunkPlaylistHelper.getPlaylistGuid(chunk);
        PlayList chunkPlaylists = ClientFileManager.getPlayList(chunkPlayListGuid);
        String chunkPlaylistName = ModGuiUtils.getPlaylistName(chunkPlaylists);

        // Chunk Start, End, Total
        Chunk chunkStart = new Chunk(null, 5, 1);
        Chunk chunkEnd = new Chunk(null, 6, 2);
        int totalChunks = (Math.abs(chunkStart.x - chunkEnd.x) + 1) * (Math.abs(chunkStart.z - chunkEnd.z) + 1);

        String formattedText = I18n.format("mxtune.gui.guiStaffOverlay.selected_play_list_to_apply", selectedPlaylistName);
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0x7FFFFF);

        formattedText = I18n.format("mxtune.gui.guiStaffOverlay.play_list_name_this_chunk",
                                           String.format("%+d", chunk.x), String.format("%+d", chunk.z),
                                           chunkPlaylistName);
        y += fontHeight;
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight);

        formattedText = I18n.format("mxtune.gui.guiStaffOverlay.play_list_name_start_chunk",
                                    String.format("%+d", chunkStart.x), String.format("%+d", chunkStart.z));
        y += fontHeight;
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0x00FF21);

        formattedText = I18n.format("mxtune.gui.guiStaffOverlay.play_list_name_end_chunk",
                                    String.format("%+d", chunkEnd.x), String.format("%+d", chunkEnd.z));
        y += fontHeight;
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0x0094FF);

        formattedText = I18n.format("mxtune.gui.guiStaffOverlay.play_list_name_total_chunks",
                                    String.format("%d", totalChunks));
        y += fontHeight;
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0xFF954F);
    }

    private void renderLine(String formattedText, int y, HudData hd, int maxWidth, int maxHeight, int fontHeight)
    {
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0xFFFFFF);
    }
    private void renderLine(String formattedText, int y, HudData hd, int maxWidth, int maxHeight, int fontHeight, int fontColor)
    {
        String trimmedText = fontRenderer.trimStringToWidth(formattedText, maxWidth);
        int qX = hd.quadX(maxWidth, 0, 4, maxWidth - 4);
        int qY = hd.quadY(maxHeight, y, 4, fontHeight);
        fontRenderer.drawStringWithShadow(trimmedText, qX, qY, fontColor);
    }

    private void drawBox(int x, int y, int width, int height) {
        drawRect(x - 2, y - 2, x + width + 2, y + height + 2, 0x88222222);
        drawRect(x, y, x + width, y + height,0xCC444444);
    }

    private interface HudInfo
    {
        void drawHud(HudData hd, int maxWidth, int maxHeight);
    }
}
