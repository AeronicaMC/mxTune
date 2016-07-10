/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.gui;

import org.lwjgl.input.Keyboard;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.StopPlayMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

public class GuiPlaying extends GuiScreen
{
    public static final int GUI_ID = 3;
    private static final String TITLE = "Playing";
    private Minecraft mc;
    private FontRenderer fontRenderer = null;
    private EntityPlayerSP player;

    /** The X size of the group window in pixels. */
    private int xSize = 512;

    /** The Y size of the group window in pixels. */
    private int ySize = 256;

    /**
     * Starting X position for the Gui. Inconsistent use for Gui backgrounds.
     */
    private int guiLeft;

    /**
     * Starting Y position for the Gui. Inconsistent use for Gui backgrounds.
     */
    private int guiTop;

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(false);

        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRendererObj;
        this.player = mc.thePlayer;

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        buttonList.clear();
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        drawGuiBackground();

        /** draw "TITLE" */
        int posX = guiLeft + xSize / 2 - fontRenderer.getStringWidth(TITLE) / 2;
        int posY = guiTop + ySize / 4;
        fontRenderer.getStringWidth(TITLE);
        fontRenderer.drawStringWithShadow(TITLE, posX, posY, 16777215);

        super.drawScreen(i, j, f);
    }

    private void drawGuiBackground() {}

    @Override
    public void onGuiClosed()
    {
        sendStop();
        ModLogger.logInfo("GuiPlaying.onGuiClosed");
    }

    @Override
    public boolean doesGuiPauseGame() {return false;}

    private void sendStop()
    {
        PacketDispatcher.sendToServer(new StopPlayMessage(player.getDisplayName().getUnformattedText()));
    }
}
