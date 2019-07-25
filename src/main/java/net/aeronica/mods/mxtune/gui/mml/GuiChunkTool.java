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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.caps.player.MusicOptionsUtil;
import net.aeronica.mods.mxtune.gui.util.GuiButtonMX;
import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.managers.ClientFileManager;
import net.aeronica.mods.mxtune.managers.records.PlayList;
import net.aeronica.mods.mxtune.managers.records.RecordType;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.GetBaseDataListsMessage;
import net.aeronica.mods.mxtune.network.server.ChunkToolMessage;
import net.aeronica.mods.mxtune.network.server.PlayerSelectedPlayListMessage;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.Notify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.io.IOException;

import static net.aeronica.mods.mxtune.gui.mml.SortHelper.PLAYLIST_ORDERING;
import static net.aeronica.mods.mxtune.network.server.ChunkToolMessage.Operation;

public class GuiChunkTool extends Screen implements Notify
{
    private static final ResourceLocation guiTexture = new ResourceLocation(Reference.MOD_ID, "textures/gui/manage_group.png");
    private int xSize = 239;
    private int ySize = 164;
    private int guiLeft;
    private int guiTop;
    private boolean isStateCached;

    private GuiScrollingListOf<PlayList> guiPlayLists;

    private GuiButtonMX buttonStart;
    private GuiButtonMX buttonEnd;
    private GuiButtonMX buttonApply;
    private GuiButtonMX buttonReset;

    private PlayerEntity player;

    public GuiChunkTool()
    {
        minecraft = Minecraft.getInstance();
        this.player = minecraft.player;
        this.font = minecraft.fontRenderer;

        guiPlayLists = new GuiScrollingListOf<PlayList>(this) {
            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                PlayList playList = get();
                if (playList != null)
                {
                    PacketDispatcher.sendToServer(new PlayerSelectedPlayListMessage(playList.getGUID()));
                    initPlayLists();
                }
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex)
            {

            }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                PlayList playList = !isEmpty() && slotIdx < getSize() && slotIdx >= 0 ? get(slotIdx) : null;
                if (playList != null)
                {
                    String playlistName = ModGuiUtils.getPlaylistName(playList);
                    String trimmedName = font.trimStringToWidth(playlistName, listWidth - 10);
                    int color = isSelected(slotIdx) ? 0xFFFF00 : 0xAADDEE;
                    font.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                } else
                {
                    String name = "---GUID Conflict---";
                    String trimmedName = font.trimStringToWidth(name, listWidth - 10);
                    int color = 0xFF0000;
                    font.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                }
            }
        };
    }

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(false);
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        guiPlayLists.setLayout(font.FONT_HEIGHT + 2, 90, 141,guiTop + 12, guiTop + 12 + 141, guiLeft + 12);

        /* create button for leave and disable it initially */
        int widthButtons = 75;
        int posX = guiLeft + 154;
        int posY = guiTop + 32;
        buttonStart = new GuiButtonMX(0, posX, posY, widthButtons, 20, "Start");
        buttonEnd = new GuiButtonMX(1, posX, buttonStart.y + buttonStart.height, widthButtons, 20, "End");
        buttonApply = new GuiButtonMX(2, posX, buttonEnd.y + buttonEnd.height, widthButtons, 20, "Apply");
        buttonReset = new GuiButtonMX(3, posX, buttonApply.y + buttonApply.height, widthButtons, 20, "Reset");

        Button buttonCancel = new Button(4, posX, buttonReset.y + buttonReset.height * 2, widthButtons, 20, I18n.format("gui.done"));
        
        buttonList.add(buttonStart);
        buttonList.add(buttonEnd);
        buttonList.add(buttonApply);
        buttonList.add(buttonReset);
        buttonList.add(buttonCancel);
        initPlayLists();
        reloadState();
    }

    private void reloadState()
    {
        updateButtons();
    }

    private void updateState()
    {
        updateSelected();
        updateButtons();
    }

    private void updateButtons()
    {
        buttonApply.enabled = MusicOptionsUtil.getChunkStart(player) != null &&
                MusicOptionsUtil.getChunkEnd(player) != null &&
                ClientFileManager.getPlayList(MusicOptionsUtil.getSelectedPlayListGuid(minecraft.player)) != null;
    }

    private void updateSelected()
    {
        PlayList selectedPlaylistToApply = ClientFileManager.getPlayList(MusicOptionsUtil.getSelectedPlayListGuid(minecraft.player));
        if (selectedPlaylistToApply != null)
            guiPlayLists.stream().filter(selectedPlaylistToApply::equals).forEach(playList ->
                {
                    guiPlayLists.setSelectedIndex(guiPlayLists.indexOf(playList));
                    guiPlayLists.resetScroll();
                });
    }

    @Override
    public boolean doesGuiPauseGame() {return false;}

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawGuiBackground();

        /* draw "TITLE" at the top right */
        String title = I18n.format("mxtune.gui.GuiChunkTool.title");
        int posX = guiLeft + xSize - this.font.getStringWidth(title) - 12;
        int posY = guiTop + 12;
        this.font.getStringWidth(title);
        this.font.drawString(title, posX, posY, 0x000000);

        guiPlayLists.drawScreen(mouseX, mouseY, partialTicks);

        /* draw the things in the controlList (buttons) */
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(Button guibutton)
    {
        // if button is disabled ignore click
        if (!guibutton.enabled) { return; }

        switch (guibutton.id)
        {
            case 0:
                // Starting Chunk
                setOperation(Operation.START);
                MusicOptionsUtil.setChunkStart(player, player.world.getChunk(player.getPosition()));
                break;

            case 1:
                // Ending Chunk
                setOperation(Operation.END);
                MusicOptionsUtil.setChunkEnd(player, player.world.getChunk(player.getPosition()));
                break;

            case 2:
                // Apply Playlist
                setOperation(Operation.APPLY);
                break;

            case 3:
                // Reset Chunks
                resetOperations();
                break;

            case 4:
                // Close remove the GUI

                break;
            default:
        }
        minecraft.displayGuiScreen(null);
        minecraft.setIngameFocus();
        updateState();
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / minecraft.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / minecraft.displayHeight - 1;
        guiPlayLists.handleMouseInput(mouseX, mouseY);
        super.handleMouseInput();
    }

    private void drawGuiBackground()
    {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        minecraft.renderEngine.bindTexture(guiTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    private void initPlayLists()
    {
        PacketDispatcher.sendToServer(new GetBaseDataListsMessage(CallBackManager.register(ClientFileManager.INSTANCE, this), RecordType.PLAY_LIST));
    }

    private void setOperation(Operation op)
    {
        PacketDispatcher.sendToServer(new ChunkToolMessage(op));
    }

    private void resetOperations()
    {
        PacketDispatcher.sendToServer(new ChunkToolMessage(Operation.RESET));
        MusicOptionsUtil.setChunkStart(player, null);
        MusicOptionsUtil.setChunkEnd(player, null);
    }

    // receive notification of received responses to init playlist and song proxy lists
    @Override
    public void onNotify(@Nonnull RecordType recordType)
    {
        switch (recordType)
        {
            case PLAY_LIST:
                guiPlayLists.clear();
                guiPlayLists.addAll(PLAYLIST_ORDERING.sortedCopy(ClientFileManager.getPlayLists()));
                break;
            default:
        }
        updateState();
    }
}
