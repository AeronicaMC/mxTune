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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.caches.MXTuneFile;
import net.aeronica.mods.mxtune.caches.MXTuneFileHelper;
import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.gui.util.GuiScrollingMultiListOf;
import net.aeronica.mods.mxtune.managers.records.Area;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.GetAreasMessage;
import net.aeronica.mods.mxtune.network.bidirectional.SetServerDataMessage;
import net.aeronica.mods.mxtune.network.bidirectional.SetServerSerializedDataMessage;
import net.aeronica.mods.mxtune.util.CallBack;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.MXTuneRuntimeException;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.aeronica.mods.mxtune.network.bidirectional.SetServerDataMessage.SetType;

public class GuiTest extends GuiScreen implements CallBack
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

    // Area
    private GuiTextField areaName;
    private String cachedAreaName = "";

    // Status
    private GuiTextField status;

    // Misc
    private GuiLabel titleLabel;
    private boolean cacheKeyRepeatState;
    private boolean isStateCached;
    private GuiButton buttonToServer;

    // Mapping
    private BiMap<Path, SongProxy> pathSongProxyBiMap = HashBiMap.create();
    private BiMap<SongProxy, Path> songProxyPathBiMap;

    public GuiTest()
    {
        cacheKeyRepeatState = Keyboard.areRepeatEventsEnabled();
        Keyboard.enableRepeatEvents(false);
        initFileList();
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
                Path entry = get(slotIdx);
                if (entry != null)
                {
                    String name = entry.getFileName().toString().replaceAll("\\.[mM][xX][tT]$", "");
                    String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                    int color = selectedRowIndexes.contains(slotIdx) ? 0xFFFF00 : 0xADD8E6;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                } else
                {
                    String name = "---NULL---";
                    String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                    int color = 0xFF0000;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                }
            }
        };

        guiAreaList = new GuiScrollingListOf<Area>(this, entryAreaHeight, guiAreaListWidth, areaListHeight, listTop, areaBottom, width - guiAreaListWidth - padding)
        {
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

            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                updateStatus();
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex) { updateStatus(); }
        };

        guiDay = new GuiScrollingMultiListOf<SongProxy>(this, singleLineHeight, guiAreaListWidth, areaListHeight ,dayTop, dayBottom, width - guiAreaListWidth - padding)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                // get the filename and remove the '.mxt' extension
                SongProxy entry = get(slotIdx);
                if (entry != null)
                {
                    String name = entry.getTitle();
                    String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                    int color = selectedRowIndexes.contains(slotIdx) ? 0xFFFF00 : 0xADD8E6;
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

        guiNight = new GuiScrollingMultiListOf<SongProxy>(this, singleLineHeight, guiAreaListWidth, areaListHeight, nightTop, nightBottom, width - guiAreaListWidth - padding)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                SongProxy entry = get(slotIdx);
                if (entry != null)
                {
                    String name = entry.getTitle();
                    String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                    int color = selectedRowIndexes.contains(slotIdx) ? 0xFFFF00 : 0xADD8E6;
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

        status = new GuiTextField(0, fontRenderer, left, statusTop, width - padding * 2, singleLineHeight + 2);
        status.setMaxStringLength(256);

        int buttonTop = height - 25;
        int xImport = (this.width /2) - 75 * 2;
        int xPlay = xImport + 75;
        int xSaveDone = xPlay + 75;

        GuiButton buttonImport = new GuiButton(0, xImport, buttonTop, 75, 20, I18n.format("mxtune.gui.button.importMML"));
        GuiButton buttonDone = new GuiButton(1, xSaveDone, buttonTop, 75, 20, I18n.format("gui.done"));

        int selectButtonWidth = guiFileListWidth - 10;
        int selectButtonLeft = guiFileList.getRight() + 8;

        areaName = new GuiTextField(1, fontRenderer, selectButtonLeft, listTop, selectButtonWidth, singleLineHeight + 2);
        buttonToServer = new GuiButton(6, selectButtonLeft, areaName.y + areaName.height + padding, selectButtonWidth, 20, "Send to Server");

        GuiButton test = new GuiButton(7, selectButtonLeft, buttonToServer.y + buttonToServer.height + padding, selectButtonWidth, 20, "Test");

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
        buttonList.add(test);

        initAreas();
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

        buttonToServer.enabled = !(areaName.getText().trim().equals("") || (guiDay.isEmpty() && guiNight.isEmpty()));
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
                for (SongProxy songProxy : guiDay)
                    if (songProxy != null)
                        ModLogger.debug("Day Song   guid: %s, Title: %s", songProxy.getGUID().toString(), songProxy.getTitle());
                for (SongProxy songProxy : guiNight)
                    if (songProxy != null)
                        ModLogger.debug("Night Song guid: %s, Title: %s", songProxy.getGUID().toString(), songProxy.getTitle());
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
            case 7:
                test();
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

    // This is experimental and inefficient
    private void initFileList()
    {
        new Thread(
                () ->
                {
                    pathSongProxyBiMap.clear();
                    Path pathDir = FileHelper.getDirectory(FileHelper.CLIENT_LIB_FOLDER, Side.CLIENT);
                    PathMatcher filter = FileHelper.getMxtMatcher(pathDir);
                    try (Stream<Path> paths = Files.list(pathDir))
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
                        SongProxy songProxy = pathToSongProxy(file);
                        if (songProxy != null)
                            pathSongProxyBiMap.forcePut(file, songProxy);
                        else
                            throw new MXTuneRuntimeException("soundProxy Unexpected NULL in initFileList");
                    }
                    cachedFileList = files;
                    guiFileList.clear();
                    guiFileList.addAll(files);
                    songProxyPathBiMap = pathSongProxyBiMap.inverse();
                }).start();
    }

    @Nullable
    private SongProxy pathToSongProxy(Path path)
    {
        MXTuneFile mxTuneFile = MXTuneFileHelper.getMXTuneFile(path);
        if (mxTuneFile != null)
            return MXTuneFileHelper.getSongProxy(mxTuneFile);
        else
            ModLogger.warn("mxt file is missing or corrupt");

        return null;
    }

    private Song pathToSong(Path path)
    {
        MXTuneFile mxTuneFile = MXTuneFileHelper.getMXTuneFile(path);
        if (mxTuneFile != null)
            return MXTuneFileHelper.getSong(mxTuneFile);
        else
            ModLogger.warn("mxt file is missing or corrupt");

        // The EMPTY SongProxy
        return new Song();
    }

    // So crude: add unique songs only
    private List<SongProxy> pathsToSongProxies(List<Path> paths, List<SongProxy> current)
    {
        List<SongProxy> songList = new ArrayList<>(current);
        for (Path path : paths)
        {
            SongProxy songProxyPath = pathSongProxyBiMap.get(path);
            if (!songList.contains(songProxyPath))
                songList.add(songProxyPath);
        }
        current.clear();
        return songList;
    }

    private void shipIt()
    {
        // TODO: Send Area and Song data to the server
        // existing area check
        // push <-> status

        areaName.setText(areaName.getText().trim());
        Area area = new Area(areaName.getText(), guiDay.getList(), guiNight.getList());
        NBTTagCompound areaCompound = new NBTTagCompound();
        area.writeToNBT(areaCompound);
        PacketDispatcher.sendToServer(new SetServerDataMessage(area.getGUID(), SetType.AREA, areaCompound));

        for (SongProxy songProxy : guiDay.getList())
        {
            if (songProxy != null)
            {
                Song song = pathToSong(songProxyPathBiMap.get(songProxy));
                NBTTagCompound songCompound = new NBTTagCompound();
                song.writeToNBT(songCompound);
                PacketDispatcher.sendToServer(new SetServerDataMessage(songProxy.getGUID(), SetType.MUSIC, songCompound));
            }
        }
        for (SongProxy songProxy : guiNight.getList())
        {
            if (songProxy != null)
            {
                Song song = pathToSong(songProxyPathBiMap.get(songProxy));
                NBTTagCompound songCompound = new NBTTagCompound();
                song.writeToNBT(songCompound);
                PacketDispatcher.sendToServer(new SetServerDataMessage(songProxy.getGUID(), SetType.MUSIC, songCompound));
            }
        }

        // done
        initAreas();
        updateState();
    }


    private void initAreas()
    {
        PacketDispatcher.sendToServer(new GetAreasMessage(CallBackManager.register(this)));
    }

    @Override
    public void onFailure(@Nonnull ITextComponent textComponent)
    {
        status.setText(textComponent.getFormattedText());
        ModLogger.warn("InitAreas onFailure: %s", textComponent.getFormattedText());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onResponse(@Nullable Object payload)
    {
        guiAreaList.clear();
        if (payload != null)
            guiAreaList.addAll((List<Area>) payload);
        updateState();
    }

    private void test()
    {
        areaName.setText(areaName.getText().trim());
        Area area = new Area(areaName.getText(), guiDay.getList(), guiNight.getList());
        NBTTagCompound areaCompound = new NBTTagCompound();
        area.writeToNBT(areaCompound);
        ModLogger.debug("...............................................................");
        ModLogger.debug("AREA TEST %s, Day Count: %d", area.getName(), area.getPlayListDay().size());
        PacketDispatcher.sendToServer(new SetServerSerializedDataMessage(area.getGUID(), SetServerSerializedDataMessage.SetType.AREA, area));

        for (SongProxy songProxy : guiDay.getList())
        {
            if (songProxy != null)
            {
                Song song = pathToSong(songProxyPathBiMap.get(songProxy));
                NBTTagCompound songCompound = new NBTTagCompound();
                song.writeToNBT(songCompound);
                ModLogger.debug("-------------------------------------------------------------------");
                ModLogger.debug("SONG TEST %s, MML Length: %d", song.getTitle(), song.getMml().length());
                PacketDispatcher.sendToServer(new SetServerSerializedDataMessage(songProxy.getGUID(), SetServerSerializedDataMessage.SetType.MUSIC, song));
            }
        }
    }
}