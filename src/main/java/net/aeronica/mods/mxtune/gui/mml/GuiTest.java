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
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.SetServerDataMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
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

import static net.aeronica.mods.mxtune.network.bidirectional.SetServerDataMessage.SetType;

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
    private GuiScrollingMultiListOf<Song> guiDay;
    private List<Song> cachedDayList = new ArrayList<>();
    private Set<Integer> cachedSelectedDaySongs = new HashSet<>();
    private int cachedSelectedDaySongDummy = -1;

    // PlayList Night
    private GuiScrollingMultiListOf<Song> guiNight;
    private List<Song> cachedNightList = new ArrayList<>();
    private Set<Integer> cachedSelectedNightSongs = new HashSet<>();
    private int cachedSelectedNightSongDummy = -1;

    // Area
    private GuiTextField areaName;
    private String cachedAreaName = "";

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
        int guiAreaListWidth = Math.min(Math.max((width - 15) / 3, 100), 400);
        int guiFileListWidth = guiAreaListWidth;
        int singleLineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int entryAreaHeight = singleLineHeight * 2;
        int padding = 5;
        int titleTop = padding;
        int left = padding;
        int titleWidth = fontRenderer.getStringWidth(TITLE);
        int titleX = (width / 2) - (titleWidth / 2);

        int titleHeight = singleLineHeight + 2;
        int statusHeight = singleLineHeight + 2;
        int entryFileHeight = singleLineHeight;

        int listTop = titleTop + titleHeight;
        int fileListBottom = Math.max(height - statusHeight - listTop - titleHeight - padding, entryAreaHeight * 9);

        int fileListHeight = Math.max(fileListBottom - listTop, singleLineHeight);
        int statusTop = fileListBottom + padding;

        int thirdsHeight = ((statusTop - listTop) / 3) - padding;
        int areaListHeight = Math.max(thirdsHeight, entryAreaHeight);
        int areaBottom = listTop + areaListHeight;

        int dayTop = areaBottom + padding;
        int dayBottom = dayTop + areaListHeight;

        int nightTop = dayBottom + padding;
        int nightBottom = nightTop + areaListHeight;

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

        guiAreaList = new GuiScrollingListOf<Area>(this, entryAreaHeight, guiAreaListWidth, areaListHeight, listTop, areaBottom, width - guiAreaListWidth - padding)
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

        guiDay = new GuiScrollingMultiListOf<Song>(this, singleLineHeight, guiAreaListWidth, areaListHeight ,dayTop, dayBottom, width - guiAreaListWidth - padding)
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

        guiNight = new GuiScrollingMultiListOf<Song>(this, singleLineHeight, guiAreaListWidth, areaListHeight, nightTop, nightBottom, width - guiAreaListWidth - padding)
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

        status = new GuiTextField(0, fontRenderer, left, statusTop, width - padding * 2, singleLineHeight + 2);

        int buttonTop = height - 25;
        int xImport = (this.width /2) - 75 * 2;
        int xPlay = xImport + 75;
        int xSaveDone = xPlay + 75;

        GuiButton buttonImport = new GuiButton(0, xImport, buttonTop, 75, 20, I18n.format("mxtune.gui.button.importMML"));
        GuiButton buttonDone = new GuiButton(1, xSaveDone, buttonTop, 75, 20, I18n.format("gui.done"));

        int selectButtonWidth = guiFileListWidth - 10;
        int selectButtonLeft = guiFileList.getRight() + 8;

        areaName = new GuiTextField(1, fontRenderer, selectButtonLeft, listTop, selectButtonWidth, singleLineHeight + 2);
        GuiButton buttonToServer = new GuiButton(6, selectButtonLeft, areaName.y + areaName.height + padding, selectButtonWidth, 20, "Send to Server");

        GuiButton buttonToDay = new GuiButton(2, selectButtonLeft, dayTop + padding, selectButtonWidth, 20, "To Day List ->");
        GuiButton buttonToNight = new GuiButton(3, selectButtonLeft, nightTop + padding, selectButtonWidth, 20, "To Night List ->");
        GuiButton buttonDDeleteDay = new GuiButton(4, selectButtonLeft, buttonToDay.y + buttonToDay.height + padding, selectButtonWidth, 20, "Delete");
        GuiButton buttonDDeleteNight = new GuiButton(5, selectButtonLeft, buttonToNight.y + buttonToNight.height + padding, selectButtonWidth, 20, "Delete");

        buttonList.add(buttonImport);
        buttonList.add(buttonDone);
        buttonList.add(buttonToDay);
        buttonList.add(buttonToNight);
        buttonList.add(buttonDDeleteDay);
        buttonList.add(buttonDDeleteNight);
        buttonList.add(buttonToServer);

        initAreas();
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

        areaName.setText(cachedAreaName);

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

        cachedAreaName = areaName.getText();

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
        areaName.drawTextBox();
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
                guiDay.forEach(song -> ModLogger.debug("Day Song   uuid: %s, Title: %s", song.getUUID().toString(), song.getTitle()));
                guiNight.forEach(song -> ModLogger.debug("Night Song uuid: %s, Title: %s", song.getUUID().toString(), song.getTitle()));
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
            case 4:
                // delete Day
                guiDay.deleteSelectedRows();
                break;
            case 5:
                // delete Night
                guiNight.deleteSelectedRows();
                break;
            case 6:
                // send to Server
                shipIt();
                break;
            default:
        }
        updateState();
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        areaName.textboxKeyTyped(typedChar, keyCode);
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
        areaName.mouseClicked(mouseX, mouseY, mouseButton);
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
        // TODO: Get existing Areas from the server
        for (int i = 0; i < 2; i++)
        {
            Area area = new Area(String.format("TEST: %02d", i));
            guiAreaList.add(area);
        }
    }

    // This is experimental and inefficient
    private void initFileList()
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
            files.add(file);
        }
        cachedFileList = files;
        guiFileList.clear();
        guiFileList.addAll(files);
    }

    // So crude: add unique songs only
    private List<Song> pathsToSongProxies(List<Path> paths, List<Song> current)
    {
        List<Song> songList = new ArrayList<>();
        List<UUID> uuid = new ArrayList<>();
        for (Song s : current)
        {
            uuid.add(s.getUUID());
        }
        for (Path path : paths)
        {
            MXTuneFile mxTuneFile = MXTuneFileHelper.getMXTuneFile(path);
            if (mxTuneFile != null)
            {
                Song song = MXTuneFileHelper.getSong(mxTuneFile);
                if (!uuid.contains(song.getUUID()))
                    songList.add(song);
            }
            else
                ModLogger.warn("mxt file is missing or corrupt");
        }
        return songList;
    }

    private void shipIt()
    {
        // TODO: Send Area and Song data to the server
        // shipTo button disable
        // checks empty areaName, empty toDay, empty toNight
        // existing area check
        // push <-> status
        List<UUID> dayUUIDs = new ArrayList<>();
        List<UUID> nightUUIDs = new ArrayList<>();
        guiDay.forEach(song->dayUUIDs.add(song.getUUID()));
        guiNight.forEach(song->nightUUIDs.add(song.getUUID()));

        Area area = new Area(areaName.getText(), dayUUIDs, nightUUIDs);
        NBTTagCompound areaCompound = new NBTTagCompound();
        area.writeToNBT(areaCompound);
        PacketDispatcher.sendToServer(new SetServerDataMessage(area.getUUID(), SetType.AREA, areaCompound));

        for (Song song : guiDay.getList())
        {
            NBTTagCompound songCompound = new NBTTagCompound();
            song.writeToNBT(songCompound);
            PacketDispatcher.sendToServer(new SetServerDataMessage(song.getUUID(), SetType.MUSIC, songCompound));
        }
        for (Song song : guiNight.getList())
        {
            NBTTagCompound songCompound = new NBTTagCompound();
            song.writeToNBT(songCompound);
            PacketDispatcher.sendToServer(new SetServerDataMessage(song.getUUID(), SetType.MUSIC, songCompound));
        }
        // done
        // shipTo button enable
    }
}