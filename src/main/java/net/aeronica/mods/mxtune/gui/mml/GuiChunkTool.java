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
import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.managers.ClientFileManager;
import net.aeronica.mods.mxtune.managers.records.PlayList;
import net.aeronica.mods.mxtune.managers.records.RecordType;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.GetBaseDataListsMessage;
import net.aeronica.mods.mxtune.network.server.PlayerSelectedPlayListMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.Notify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static net.aeronica.mods.mxtune.gui.mml.SortHelper.PLAYLIST_ORDERING;

public class GuiChunkTool extends GuiScreen implements Notify
{
    private static final ResourceLocation guiTexture = new ResourceLocation(Reference.MOD_ID, "textures/gui/manage_group.png");
    private int xSize = 239;
    private int ySize = 164;
    private int guiLeft;
    private int guiTop;
    private boolean isStateCached;

    private GuiScrollingListOf<PlayList> guiPlayLists;

    private GuiButton buttonMark;

    private EntityPlayer player;

    public GuiChunkTool()
    {
        mc = Minecraft.getMinecraft();
        this.player = mc.player;
        this.fontRenderer = mc.fontRenderer;

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
                    String trimmedName = fontRenderer.trimStringToWidth(playlistName, listWidth - 10);
                    int color = isSelected(slotIdx) ? 0xFFFF00 : 0xAADDEE;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                } else
                {
                    String name = "---GUID Conflict---";
                    String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                    int color = 0xFF0000;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
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
        //int entryHeight, int width, int height, int top, int bottom, int left)
        guiPlayLists.setLayout(fontRenderer.FONT_HEIGHT + 2, 90, 141,guiTop + 12, guiTop + 12 + 141, guiLeft + 12);

        buttonList.clear();

        /* create button for leave and disable it initially */
        int posX = guiLeft + 169;
        int posY = guiTop + 112;
        buttonMark = new GuiButton(0, posX, posY, 60, 20, "Leave");

        posX = guiLeft + 169;
        posY = guiTop + 132;
        GuiButton btnCancel = new GuiButton(2, posX, posY, 60, 20, "Cancel");
        
        buttonList.add(buttonMark);
        buttonList.add(btnCancel);
        initPlayLists();
        reloadState();
    }

    private void reloadState()
    {
        if (!isStateCached) return;

    }

    private void updateState()
    {
        updateSelected();
        isStateCached = true;
    }

    private void updateSelected()
    {
        PlayList selectedPlaylistToApply = ClientFileManager.getPlayList(MusicOptionsUtil.getSelectedPlayListGuid(mc.player));
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
        drawDefaultBackground();
        drawGuiBackground();

        /* draw "TITLE" at the top right */
        String title = I18n.format("mxtune.gui.GuiChunkTool.title");
        int posX = guiLeft + xSize - this.fontRenderer.getStringWidth(title) - 12;
        int posY = guiTop + 12;
        this.fontRenderer.getStringWidth(title);
        this.fontRenderer.drawString(title, posX, posY, 0x000000);

        guiPlayLists.drawScreen(mouseX, mouseY, partialTicks);

        /* draw the things in the controlList (buttons) */
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        // if button is disabled ignore click
        if (!guibutton.enabled) { return; }

        switch (guibutton.id)
        {
        case 0:
            /* Create Group */
            break;

        case 1:
            /* Leave Group */
            break;

        case 2:
            /* Cancel remove the GUI */
        default:
        }
        mc.displayGuiScreen(null);
        mc.setIngameFocus();
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        guiPlayLists.handleMouseInput(mouseX, mouseY);
        super.handleMouseInput();
    }

    /* Gets the image for the background and renders it in the middle of the screen. */
    private void drawGuiBackground()
    {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.renderEngine.bindTexture(guiTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    private void sendRequest(int operation, Integer memberId)
    {
        // NOP
    }

    private void initPlayLists()
    {
        PacketDispatcher.sendToServer(new GetBaseDataListsMessage(CallBackManager.register(ClientFileManager.INSTANCE, this), RecordType.PLAY_LIST));
    }

    // receive notification of received responses to init playlist and song proxy lists
    @Override
    public void onNotify(RecordType recordType)
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
