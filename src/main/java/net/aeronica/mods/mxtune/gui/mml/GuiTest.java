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
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuiTest extends GuiScreen
{
    private int guiLeft;
    private int guiTop;
    private GuiScrollingMultiListOf<Area> areaGuiList;
    private List<Area> cachedAreaGuiList = new ArrayList<>();
    private boolean isStateCached;
    private Area selectedArea;
    private int cachedSelectedAreaIndex;
    private boolean cacheKeyRepeatState;
    private Set<Integer> cachedSelectedIndexes = new HashSet<>();

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
        this.guiLeft = 0;
        this.guiTop = 0;
        int guiListWidth = (width - 15) * 3 / 4;
        // Area List
        int entryHeight = (mc.fontRenderer.FONT_HEIGHT + 2) * 2;
        int left = 5;
        int titleTop = 20;
        int listTop = titleTop + 25;
        int listHeight = height - titleTop - entryHeight - 2 - 10 - 25 - 25;
        int listBottom = listTop + listHeight;
        int statusTop = listBottom + 4;
        int partListWidth = (width - 15) / 4;

        areaGuiList = new GuiScrollingMultiListOf<Area>(this, entryHeight, guiListWidth, listHeight, listTop, listBottom, left)
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
        };

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
        areaGuiList.setSelectedIndex(cachedSelectedAreaIndex);
        areaGuiList.setSelectedRowIndexes(cachedSelectedIndexes);
        areaGuiList.resetScroll();
    }

    private void updateState()
    {
        cachedAreaGuiList.clear();
        cachedAreaGuiList.addAll(areaGuiList.getList());
        cachedSelectedAreaIndex = areaGuiList.getSelectedIndex();
        cachedSelectedIndexes.clear();
        cachedSelectedIndexes.addAll(areaGuiList.getSelectedRowIndexes());
        isStateCached = true;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        areaGuiList.drawScreen(mouseX, mouseY, partialTicks);
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
                for (Area area : areaGuiList.getSelectedRows())
                {
                    ModLogger.debug("%s, %s", area.getName(), area.getUUID().toString());
                }
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