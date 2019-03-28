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

import net.aeronica.mods.mxtune.gui.util.GuiScrollingMultiListOf;
import net.aeronica.mods.mxtune.managers.records.Area;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class GuiTest extends GuiScreen
{
    private static final String TITLE = "Test Gui";
    // Song Multi Selector
    private GuiScrollingMultiListOf<Path> guiFileList;
    private List<Path> songFiles = new ArrayList<>();
    private Set<Integer> cachedSelectedSongs = new HashSet<>();

    // Area Multi Selector
    private GuiScrollingMultiListOf<Area> areaGuiList;
    private List<Area> cachedAreaGuiList = new ArrayList<>();
    private Set<Integer> cachedSelectedIndexes = new HashSet<>();

    // Song data
    private Map<UUID, SongProxy> songMap = new HashMap<>();

    // Status
    private GuiTextField status;

    // Misc
    private GuiLabel titleLabel;
    private boolean cacheKeyRepeatState;
    private boolean isStateCached;

    public GuiTest()
    {
        cacheKeyRepeatState = Keyboard.areRepeatEventsEnabled();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(cacheKeyRepeatState);
    }

    @Override
    public void initGui()
    {
        int guiAreaListWidth = (width - 15) * 3 / 4;
        int singleLineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int entryAreaHeight = singleLineHeight * 2;
        int titleTop = 5;
        int left = 5;
        int titleWidth = fontRenderer.getStringWidth(TITLE);
        int titleX = (width / 2) - (titleWidth / 2);
        int titleHeight = singleLineHeight + 2;
        int statusheight = singleLineHeight + 2;
        int listTop = titleTop + titleHeight;
        int listBottom = height - statusheight - listTop - titleHeight - 5;
        int areaListHeight = Math.max(listBottom - listTop, entryAreaHeight);
        int statusTop = listBottom + 5;

        titleLabel = new GuiLabel(fontRenderer, 0, titleX, titleTop, titleWidth, singleLineHeight, 0xFFFFFF );
        titleLabel.addLine(TITLE);

        areaGuiList = new GuiScrollingMultiListOf<Area>(this, entryAreaHeight, guiAreaListWidth, areaListHeight, listTop, listBottom, left)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                Area area = get(slotIdx);
                String trimmedName = fontRenderer.trimStringToWidth(area.getName(), listWidth - 10);
                String trimmedUUID = fontRenderer.trimStringToWidth(area.getUUID().toString(), listWidth - 10);
                int color = selectedRowIndexes.contains(slotIdx) ? 0xFFFF00 : 0xAADDEE;
                fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                fontRenderer.drawStringWithShadow(trimmedUUID, (float) left + 3, (float) slotTop + 10, color);
            }

            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                updateStatus();
            }
        };

        status = new GuiTextField(0, fontRenderer, left, statusTop, guiAreaListWidth, singleLineHeight + 2);

        int buttonTop = height - 25;
        int xImport = (this.width /2) - 75 * 2;
        int xPlay = xImport + 75;
        int xSaveDone = xPlay + 75;

        GuiButton buttonImport = new GuiButton(0, xImport, buttonTop, 75, 20, I18n.format("mxtune.gui.button.importMML"));
        GuiButton buttonDone = new GuiButton(1, xSaveDone, buttonTop, 75, 20, I18n.format("gui.done"));

        buttonList.add(buttonImport);
        buttonList.add(buttonDone);

        reloadState();
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        areaGuiList.addAll(cachedAreaGuiList);
        areaGuiList.setSelectedRowIndexes(cachedSelectedIndexes);
        updateStatus();
        areaGuiList.resetScroll();
    }

    private void updateState()
    {
        cachedAreaGuiList.clear();
        cachedAreaGuiList.addAll(areaGuiList.getList());
        cachedSelectedIndexes.clear();
        cachedSelectedIndexes.addAll(areaGuiList.getSelectedRowIndexes());
        updateStatus();
        isStateCached = true;
    }

    private void updateStatus()
    {
        status.setText(String.format("Selected Item Count: %s", areaGuiList.getSelectedRowsCount()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        titleLabel.drawLabel(mc, mouseX, mouseY);
        areaGuiList.drawScreen(mouseX, mouseY, partialTicks);
        status.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (!button.enabled) return;
        switch (button.id)
        {
            case 0:
                areaGuiList.clear();
                initAreas();
                break;
            case 1:
                areaGuiList.getSelectedRows().forEach(area -> ModLogger.debug("%s, %s", area.getName(), area.getUUID().toString()));
                mc.displayGuiScreen(null);
                break;
            default:
        }
        updateState();
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onResize(@Nonnull Minecraft mcIn, int w, int h)
    {
        updateState();
        super.onResize(mcIn, w, h);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        updateState();
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        areaGuiList.handleMouseInput(mouseX, mouseY);
        super.handleMouseInput();
    }

    private void initAreas()
    {
        for (int i = 0; i < 50; i++)
        {
            Area area = new Area(String.format("TEST: %02d", i));
            areaGuiList.add(area);
        }
    }
}