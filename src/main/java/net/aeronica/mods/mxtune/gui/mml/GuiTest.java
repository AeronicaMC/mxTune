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

import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.caches.MXTuneFile;
import net.aeronica.mods.mxtune.caches.MXTuneFileHelper;
import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.gui.util.GuiScrollingMultiListOf;
import net.aeronica.mods.mxtune.managers.records.Area;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiTest extends GuiScreen
{
    private static final String TITLE = "Test Gui";
    // Song Multi Selector
    private GuiScrollingMultiListOf<Path> guiFileList;
    private List<Path> cachedFileList = new ArrayList<>();
    private Set<Integer> cachedSelectedSongs = new HashSet<>();
    private int cachedSelectedFileDummy = -1;

    // Area Multi Selector
    private GuiScrollingListOf<Area> guiAreaList;
    private List<Area> cachedAreaGuiList = new ArrayList<>();
    private int cachedSelectedAreaIndex = -1;

    // PlayList Day
    private GuiScrollingMultiListOf<SongProxy> guiDay;
    private List<SongProxy> cachedDayList = new ArrayList<>();
    private Set<Integer> cachedSelectedDaySongs = new HashSet<>();
    private int cachedSelectedDaySongDummy = -1;

    // PlayList Night
    private GuiScrollingMultiListOf<SongProxy> guiNight;
    private List<SongProxy> cachedNightList = new ArrayList<>();
    private Set<Integer> cachedSelectedNightSongs = new HashSet<>();
    private int cachedSelectedNightSongDummy = -1;

    // Song data
    private Map<UUID, SongProxy> songProxyMap = new HashMap<>();
    private Map<UUID, Song> songMap = new HashMap<>();

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
        int guiAreaListWidth = (width - 15) / 3;
        int guiFileListWidth = (width - 15) / 3;
        int singleLineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int entryAreaHeight = singleLineHeight * 2;
        int border = 5;
        int titleTop = border;
        int left = border;
        int titleWidth = fontRenderer.getStringWidth(TITLE);
        int titleX = (width / 2) - (titleWidth / 2);

        int titleHeight = singleLineHeight + 2;
        int statusHeight = singleLineHeight + 2;
        int entryFileHeight = singleLineHeight;

        int listTop = titleTop + titleHeight;
        int fileListBottom = height - statusHeight - listTop - titleHeight - border;

        int fileListHeight = Math.max(fileListBottom - listTop, singleLineHeight);
        int statusTop = fileListBottom + border;

        int thirdsHeight = (fileListBottom - listTop) / 3;
        int areaListHeight = Math.max(thirdsHeight, entryAreaHeight);
        int areaBottom = (height - areaListHeight * 3);

        int nightTop = fileListBottom - areaListHeight;
        int nightBottom = fileListBottom;

        int dayTop = nightTop - areaListHeight;
        int dayBottom = nightTop;

        titleLabel = new GuiLabel(fontRenderer, 0, titleX, titleTop, titleWidth, singleLineHeight, 0xFFFFFF );
        titleLabel.addLine(TITLE);

        guiFileList = new GuiScrollingMultiListOf<Path>(this, entryFileHeight, guiFileListWidth, fileListHeight,listTop, fileListBottom, left)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                // get the filename and remove the '.mxt' extension
                String name = (get(slotIdx).getFileName().toString()).replaceAll("\\.[mM][xX][tT]$", "");
                String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                int color = selectedRowIndexes.contains(slotIdx) ? 0xFFFF00 : 0xADD8E6;
                fontRenderer.drawStringWithShadow(trimmedName, (float)left + 3, slotTop, color);
            }
        };

        guiAreaList = new GuiScrollingListOf<Area>(this, entryAreaHeight, guiAreaListWidth, areaListHeight, listTop, areaBottom, width - guiAreaListWidth - border)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                Area area = get(slotIdx);
                String trimmedName = fontRenderer.trimStringToWidth(area.getName(), listWidth - 10);
                String trimmedUUID = fontRenderer.trimStringToWidth(area.getUUID().toString(), listWidth - 10);
                int color = isSelected(slotIdx) ? 0xFFFF00 : 0xAADDEE;
                fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                fontRenderer.drawStringWithShadow(trimmedUUID, (float) left + 3, (float) slotTop + 10, color);
            }

            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                updateStatus();
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex) { updateStatus(); }
        };

        guiDay = new GuiScrollingMultiListOf<SongProxy>(this, singleLineHeight, guiAreaListWidth, areaListHeight ,dayTop, dayBottom, width - guiAreaListWidth - border)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                // get the filename and remove the '.mxt' extension
                String name = (get(slotIdx).getTitle());
                String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                int color = selectedRowIndexes.contains(slotIdx) ? 0xFFFF00 : 0xADD8E6;
                fontRenderer.drawStringWithShadow(trimmedName, (float)left + 3, slotTop, color);
            }
        };

        guiNight = new GuiScrollingMultiListOf<SongProxy>(this, singleLineHeight, guiAreaListWidth, areaListHeight, nightTop, nightBottom, width - guiAreaListWidth - border)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                // get the filename and remove the '.mxt' extension
                String name = (get(slotIdx).getTitle());
                String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                int color = selectedRowIndexes.contains(slotIdx) ? 0xFFFF00 : 0xADD8E6;
                fontRenderer.drawStringWithShadow(trimmedName, (float)left + 3, slotTop, color);
            }
        };

        status = new GuiTextField(0, fontRenderer, left, statusTop, width - border * 2, singleLineHeight + 2);

        int buttonTop = height - 25;
        int xImport = (this.width /2) - 75 * 2;
        int xPlay = xImport + 75;
        int xSaveDone = xPlay + 75;

        GuiButton buttonImport = new GuiButton(0, xImport, buttonTop, 75, 20, I18n.format("mxtune.gui.button.importMML"));
        GuiButton buttonDone = new GuiButton(1, xSaveDone, buttonTop, 75, 20, I18n.format("gui.done"));

        int selectButtonWidth = ((width - 15) / 3) - 10;
        int selectButtonLeft = guiFileList.getRight() + 8;
        int buttonYOffset = (areaListHeight / 2) - 12;
        GuiButton buttonToDay = new GuiButton(2, selectButtonLeft, dayTop + buttonYOffset, selectButtonWidth, 20, "To Day List ->");
        GuiButton buttonToNight = new GuiButton(3, selectButtonLeft, nightTop + buttonYOffset, selectButtonWidth, 20, "To Night List ->");

        buttonList.add(buttonImport);
        buttonList.add(buttonDone);
        buttonList.add(buttonToDay);
        buttonList.add(buttonToNight);

        initFileList();
        reloadState();
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        guiAreaList.addAll(cachedAreaGuiList);
        guiAreaList.setSelectedIndex(cachedSelectedAreaIndex);

        guiFileList.addAll(cachedFileList);
        guiFileList.setSelectedRowIndexes(cachedSelectedSongs);
        guiFileList.setSelectedIndex(cachedSelectedFileDummy);

        guiDay.addAll(cachedDayList);
        guiDay.setSelectedRowIndexes(cachedSelectedDaySongs);
        guiDay.setSelectedIndex(cachedSelectedDaySongDummy);

        guiNight.addAll(cachedNightList);
        guiNight.setSelectedRowIndexes(cachedSelectedNightSongs);
        guiNight.setSelectedIndex(cachedSelectedNightSongDummy);

        updateStatus();
        guiAreaList.resetScroll();
        guiFileList.resetScroll();
    }

    private void updateState()
    {
        cachedAreaGuiList.clear();
        cachedAreaGuiList.addAll(guiAreaList.getList());
        cachedSelectedAreaIndex = guiAreaList.getSelectedIndex();

        cachedSelectedSongs.clear();
        cachedSelectedSongs.addAll(guiFileList.getSelectedRowIndexes());
        cachedSelectedFileDummy = guiFileList.getSelectedIndex();

        cachedSelectedDaySongs.clear();
        cachedSelectedDaySongs.addAll(guiDay.getSelectedRowIndexes());
        cachedDayList.clear();
        cachedDayList.addAll(guiDay.getList());
        cachedSelectedDaySongDummy = guiDay.getSelectedIndex();

        cachedSelectedNightSongs.clear();
        cachedSelectedNightSongs.addAll(guiNight.getSelectedRowIndexes());
        cachedNightList.clear();
        cachedNightList.addAll(guiNight.getList());
        cachedSelectedNightSongDummy = guiNight.getSelectedIndex();

        updateStatus();
        isStateCached = true;
    }

    private void updateStatus()
    {
        status.setText(String.format("Selected Song Count: %s", guiFileList.getSelectedRowsCount()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        titleLabel.drawLabel(mc, mouseX, mouseY);
        guiFileList.drawScreen(mouseX, mouseY, partialTicks);
        guiAreaList.drawScreen(mouseX, mouseY, partialTicks);
        guiDay.drawScreen(mouseX, mouseY, partialTicks);
        guiNight.drawScreen(mouseX, mouseY, partialTicks);
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
                break;
            case 1:
                //guiAreaList.getSelectedRows().forEach(area -> ModLogger.debug("%s, %s", area.getName(), area.getUUID().toString()));
                mc.displayGuiScreen(null);
                break;
            case 2:
                // to Day
                guiDay.addAll(pathsToSongProxies(guiFileList.getSelectedRows(), guiDay.getList()));
                break;
            case 3:
                // to Night
                guiNight.addAll(pathsToSongProxies(guiFileList.getSelectedRows(), guiNight.getList()));
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
        guiAreaList.handleMouseInput(mouseX, mouseY);
        guiFileList.handleMouseInput(mouseX, mouseY);
        guiDay.handleMouseInput(mouseX, mouseY);
        guiNight.handleMouseInput(mouseX, mouseY);
        super.handleMouseInput();
    }

    private void initAreas()
    {
        for (int i = 0; i < 50; i++)
        {
            Area area = new Area(String.format("TEST: %02d", i));
            guiAreaList.add(area);
        }
    }

    // This is experimental and inefficient
    private void initFileList()
    {
        final Runnable runnable = () ->
        {
            Path path = FileHelper.getDirectory(FileHelper.CLIENT_LIB_FOLDER, Side.CLIENT);
            PathMatcher filter = FileHelper.getMxtMatcher(path);
            try (Stream<Path> paths = Files.list(path))
            {
                cachedFileList = paths
                        .filter(filter::matches)
                        .collect(Collectors.toList());
            } catch (NullPointerException | IOException e)
            {
                ModLogger.error(e);
            }

            List<Path> files = new ArrayList<>();
            for (Path file : cachedFileList)
            {
//            if (file.getFileName().toString().toLowerCase(Locale.ROOT).contains(search.getText().toLowerCase(Locale.ROOT)))
//            {
                Song song;

                files.add(file);
                MXTuneFile mxTuneFile = MXTuneFileHelper.getMXTuneFile(file);
                if (mxTuneFile != null)
                {
                    song = MXTuneFileHelper.getSong(mxTuneFile);
                    if (!songMap.containsKey(song.getUUID()))
                        songMap.put(song.getUUID(), song);
                    SongProxy songProxy = MXTuneFileHelper.getSongProxy(song);
                    if (!songProxyMap.containsKey(songProxy.getUUID()))
                        songProxyMap.put(songProxy.getUUID(), songProxy);
                }
                else
                    ModLogger.warn("mxt file is missing or corrupt");
//            }
            }
            cachedFileList = files;
            guiFileList.clear();
            guiFileList.addAll(files);
            //lastSearch = search.getText();
        };
        new Thread(runnable).start();
    }

    // So crude: add unique songs only
    private List<SongProxy> pathsToSongProxies(List<Path> paths, List<SongProxy> current)
    {
        Song song;
        List<SongProxy> songProxies = new ArrayList<>();
        List<UUID> uuid = new ArrayList<>();
        for (SongProxy s : current)
        {
            uuid.add(s.getUUID());
        }
        for (Path path : paths)
        {
            MXTuneFile mxTuneFile = MXTuneFileHelper.getMXTuneFile(path);
            if (mxTuneFile != null)
            {
                song = MXTuneFileHelper.getSong(mxTuneFile);
                SongProxy songProxy = MXTuneFileHelper.getSongProxy(song);
                if (!uuid.contains(songProxy.getUUID()))
                    songProxies.add(songProxy);
            }
            else
                ModLogger.warn("mxt file is missing or corrupt");
        }
        return songProxies;
    }
}