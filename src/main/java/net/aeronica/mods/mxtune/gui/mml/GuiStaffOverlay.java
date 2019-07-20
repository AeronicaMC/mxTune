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
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.debug.ChunkBorderDebugRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
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

import javax.annotation.Nullable;

import static net.aeronica.mods.mxtune.gui.hud.GuiJamOverlay.HOT_BAR_CLEARANCE;
import static net.aeronica.mods.mxtune.network.server.ChunkToolMessage.Operation;

public class GuiStaffOverlay extends AbstractGui
{
    private Minecraft mc;
    private FontRenderer fontRenderer;
    private DebugRenderer.IDebugRenderer chunkBorder;
    private boolean holdingTriggerItem;
    private boolean holdingStaffOfMusic;
    private boolean holdingChunkTool;

    private static final String DASHES = "--";


    private static class GuiStaffOverlayHolder { private static final GuiStaffOverlay INSTANCE = new GuiStaffOverlay(); }
    public static GuiStaffOverlay getInstance() { return GuiStaffOverlayHolder.INSTANCE; }

    private GuiStaffOverlay()
    {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = this.mc.fontRenderer;
        this.chunkBorder = new ChunkBorderDebugRenderer(mc);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onEvent(RenderGameOverlayEvent.Post event)
    {
        if (mc.gameSettings.showDebugInfo) return;
        RenderGameOverlayEvent.ElementType elementType = event.getType();

        if (event.isCancelable() || (elementType != RenderGameOverlayEvent.ElementType.EXPERIENCE && elementType != RenderGameOverlayEvent.ElementType.TEXT))
            return;

        ClientPlayerEntity playerSP = this.mc.player;
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
        {
            //chunkBorder.render(event.getPartialTicks(), 0);
            render(event.getPartialTicks(), 0);
        }
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
        Chunk chunk = getThisChunk();
        GUID chunkPlayListGuid = ModChunkPlaylistHelper.getPlaylistGuid(chunk);
        PlayList chunkPlaylists = ClientFileManager.getPlayList(chunkPlayListGuid);
        String chunkPlaylistName = ModGuiUtils.getPlaylistName(chunkPlaylists);

        // Chunk Start, End, Total
        Operation op = MusicOptionsUtil.getChunkToolOperation(mc.player);
        Chunk chunkStart = MusicOptionsUtil.getChunkStart(mc.player);
        Chunk chunkEnd = MusicOptionsUtil.getChunkEnd(mc.player);

        int totalChunks = chunkStart != null && chunkEnd != null ? (Math.abs(chunkStart.x - chunkEnd.x) + 1) * (Math.abs(chunkStart.z - chunkEnd.z) + 1) : 0;

        String formattedText = I18n.format("mxtune.gui.guiStaffOverlay.selected_play_list_to_apply", selectedPlaylistName);
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0x7FFFFF);

        formattedText = I18n.format("mxtune.gui.guiStaffOverlay.play_list_name_this_chunk",
                                    chunk != null ? String.format("%+d", chunk.x) : DASHES,
                                    chunk != null ? String.format("%+d", chunk.z) : DASHES,
                                           chunkPlaylistName);
        y += fontHeight;
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight);

        formattedText = embolden(op, Operation.START, I18n.format("mxtune.gui.guiStaffOverlay.play_list_name_start_chunk",
                                    chunkStart != null ? String.format("%+d", chunkStart.x) : DASHES,
                                    chunkStart != null ? String.format("%+d", chunkStart.z) : DASHES));
        y += fontHeight;
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0x00FF21);

        formattedText = embolden(op, Operation.END, I18n.format("mxtune.gui.guiStaffOverlay.play_list_name_end_chunk",
                                    chunkEnd != null ? String.format("%+d", chunkEnd.x) : DASHES,
                                    chunkEnd != null ? String.format("%+d", chunkEnd.z) : DASHES));
        y += fontHeight;
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0x0094FF);

        formattedText = I18n.format("mxtune.gui.guiStaffOverlay.play_list_name_total_chunks",
                                    String.format("%d", totalChunks));
        y += fontHeight;
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0xFF954F);
    }

    // A bit of silliness(or not) to shut-up some sonar-cloud null-implied analytics
    @Nullable
    private Chunk getThisChunk()
    {
        Chunk chunk;
        if (mc != null && mc.world != null)
        {
            BlockPos pos = mc.player.getPosition();
            chunk = mc.world.getChunk(pos);
            return chunk;
        } else
            return null;
    }

    private String embolden(Operation op ,Operation opTest, String string)
    {
        return opTest == op ? TextFormatting.BOLD + string : string;
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

    private void render(float partialTicks, long finishTimeNano)
    {
        PlayerEntity entityplayer = this.mc.player;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        double d0 = entityplayer.lastTickPosX + (entityplayer.posX - entityplayer.lastTickPosX) * (double)partialTicks;
        double d1 = entityplayer.lastTickPosY + (entityplayer.posY - entityplayer.lastTickPosY) * (double)partialTicks;
        double d2 = entityplayer.lastTickPosZ + (entityplayer.posZ - entityplayer.lastTickPosZ) * (double)partialTicks;
        double d3 = 0.0D - d1;
        double d4 = 256.0D - d1;
        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        double d5 = (double)(entityplayer.chunkCoordX << 4) - d0;
        double d6 = (double)(entityplayer.chunkCoordZ << 4) - d2;
        GlStateManager.glLineWidth(1.0F);
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

        // Red Chunk verticals next chunk over
        for (int i = -16; i <= 32; i += 16)
        {
            for (int j = -16; j <= 32; j += 16)
            {
                bufferbuilder.pos(d5 + (double)i, d3, d6 + (double)j).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos(d5 + (double)i, d3, d6 + (double)j).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                bufferbuilder.pos(d5 + (double)i, d4, d6 + (double)j).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                bufferbuilder.pos(d5 + (double)i, d4, d6 + (double)j).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
            }
        }

        // North-South Yellow Verticals
        for (int k = 2; k < 16; k += 2)
        {
            bufferbuilder.pos(d5 + (double)k, d3, d6).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.pos(d5 + (double)k, d3, d6).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5 + (double)k, d4, d6).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5 + (double)k, d4, d6).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.pos(d5 + (double)k, d3, d6 + 16.0D).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.pos(d5 + (double)k, d3, d6 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5 + (double)k, d4, d6 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5 + (double)k, d4, d6 + 16.0D).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        // East-West Yellow Verticals
        for (int l = 2; l < 16; l += 2)
        {
            bufferbuilder.pos(d5, d3, d6 + (double)l).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.pos(d5, d3, d6 + (double)l).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5, d4, d6 + (double)l).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5, d4, d6 + (double)l).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.pos(d5 + 16.0D, d3, d6 + (double)l).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.pos(d5 + 16.0D, d3, d6 + (double)l).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5 + 16.0D, d4, d6 + (double)l).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5 + 16.0D, d4, d6 + (double)l).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        // Yellow Horizontals
        for (int i1 = 0; i1 <= 256; i1 += 2)
        {
            double d7 = (double)i1 - d1;
            bufferbuilder.pos(d5, d7, d6).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.pos(d5, d7, d6).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5, d7, d6 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5 + 16.0D, d7, d6 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5 + 16.0D, d7, d6).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5, d7, d6).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5, d7, d6).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        tessellator.draw();
        GlStateManager.glLineWidth(2.0F);
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

        for (int j1 = 0; j1 <= 16; j1 += 16)
        {
            for (int l1 = 0; l1 <= 16; l1 += 16)
            {
                bufferbuilder.pos(d5 + (double)j1, d3, d6 + (double)l1).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
                bufferbuilder.pos(d5 + (double)j1, d3, d6 + (double)l1).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                bufferbuilder.pos(d5 + (double)j1, d4, d6 + (double)l1).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                bufferbuilder.pos(d5 + (double)j1, d4, d6 + (double)l1).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            }
        }

        for (int k1 = 0; k1 <= 256; k1 += 16)
        {
            double d8 = (double)k1 - d1;
            bufferbuilder.pos(d5, d8, d6).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            bufferbuilder.pos(d5, d8, d6).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5, d8, d6 + 16.0D).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5 + 16.0D, d8, d6 + 16.0D).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5 + 16.0D, d8, d6).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5, d8, d6).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferbuilder.pos(d5, d8, d6).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
        }

        tessellator.draw();
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
    }

}
