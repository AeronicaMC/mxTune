/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.gui;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.HudOptionsMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;

public class GuiHudAdjust extends GuiScreen
{

    private static final String TITLE = I18n.format("mxtune.gui.hudAdjust.title");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private GuiScreen guiScreenOld;

    private GuiButtonExt buttonCancel;
    private GuiSliderMX sliderHudSize;

    private EntityPlayer playerIn;
    private boolean midiUnavailable;
    private int chosenHudPos;
    private int mouseX;
    private int mouseY;
    private HudMouse hudMouse = new HudMouse();

    /* Cached State for when the GUI is resized */
    private boolean isStateCached = false;
    private int prevHudPos;
    private float prevHudSize;

    GuiHudAdjust(@Nullable GuiScreen guiScreenIn)
    {
        this.guiScreenOld = guiScreenIn;
        this.mc = MXTune.proxy.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
    }

    @Override
    public void initGui()
    {
        this.playerIn = mc.player;
        midiUnavailable = MIDISystemUtil.midiUnavailable();
        float hudSize = (MusicOptionsUtil.getSizeHud(playerIn) * 100) < 50F ? 50F : MusicOptionsUtil.getSizeHud(playerIn) * 100;
        this.buttonList.clear();
        chosenHudPos = MusicOptionsUtil.getPositionHUD(playerIn);
        
        int y = height/2;
        int buttonWidth = 100;
        int x = (width / 2) - (buttonWidth/2);
        sliderHudSize = new GuiSliderMX(2, x, y, buttonWidth, 20, I18n.format("mxtune.gui.hudAdjust.size"), hudSize, 50F, 100F, 5F);
        y+=20;
        GuiButtonExt buttonDone = new GuiButtonExt(0, x, y, buttonWidth, 20, I18n.format("gui.done"));
        y+=20;
        buttonCancel = new GuiButtonExt(1, x, y, buttonWidth, 20, I18n.format("gui.cancel"));
        
        this.buttonList.add(buttonCancel);
        this.buttonList.add(buttonDone);
        this.buttonList.add(sliderHudSize);
        reloadState();
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        sliderHudSize.setValue(prevHudSize);
        chosenHudPos = prevHudPos;
    }

    private void updateState()
    {
        prevHudPos = chosenHudPos;
        this.isStateCached = true;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            switch (button.id)
            {
                case 0: /* done   */
                    PacketDispatcher.sendToServer(new HudOptionsMessage(chosenHudPos, MusicOptionsUtil.isHudDisabled(playerIn), sliderHudSize.getValue() / 100));
                    MusicOptionsUtil.setAdjustPositionHud(chosenHudPos);
                    MusicOptionsUtil.setAdjustSizeHud(sliderHudSize.getValue() / 100);
                    MusicOptionsUtil.setHudOptions(playerIn, MusicOptionsUtil.isHudDisabled(playerIn), chosenHudPos, sliderHudSize.getValue() / 100);
                    mc.displayGuiScreen(guiScreenOld);
                    break;
                case 1: /* cancel */
                    onGuiClosed();
                    mc.displayGuiScreen(guiScreenOld);
                    break;
                default:
            }
            updateState();
        }
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        // capture the ESC key so we close cleanly
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            this.actionPerformed(buttonList.get(buttonCancel.id));
            return;
        }
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen()
    {
        prevHudSize = sliderHudSize.getValue();
        MusicOptionsUtil.setAdjustSizeHud(prevHudSize / 100);
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawHudPositions();
        String localTITLE;
        if (midiUnavailable)
            localTITLE = TITLE + " - " + TextFormatting.RED + MIDI_NOT_AVAILABLE;
        else
            localTITLE = TITLE;
        // draw "TITLE" at the top/right column middle
        int posX = (this.width - fontRenderer.getStringWidth(localTITLE)) / 2 ;
        int posY = 10;
        fontRenderer.drawStringWithShadow(localTITLE, posX, posY, 0xD3D3D3);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onResize(@Nonnull Minecraft mcIn, int w, int h)
    {
        reloadState();
        super.onResize(mcIn, w, h);
    }

    @SuppressWarnings("NumericOverflow")
    private void drawHudPositions()
    {
        int height = this.height-GuiJamOverlay.HOT_BAR_CLEARANCE;
        for (int i = 0; i < 8; i++)
        {
            HudData hd = HudDataFactory.calcHudPositions(i, width, height);
            Color color = new Color(0,255,255);
            Color darkColor = color.darker();
            drawRect(
                    (hd.isDisplayLeft() ? hd.getPosX() + 2 : hd.getPosX() -2),
                    (hd.isDisplayTop()  ? hd.getPosY() + 2 : hd.getPosY() -2),
                    (hd.isDisplayLeft() ? hd.getPosX() + (width/5) -1 : hd.getPosX() - (width/5) +1),
                    (hd.isDisplayTop()  ? hd.getPosY() + (height/4)-1 : hd.getPosY() - (height/4)+1),                                     
                    ((i == mouseOverHudPos(hd, i).getHudPos()) ? color.getRGB() : darkColor.getRGB()) + (128 << 24));
        }
    }

    private HudMouse mouseOverHudPos(HudData hd, int pos)
    {
        int height = this.height - GuiJamOverlay.HOT_BAR_CLEARANCE;
        if (
                ((hd.isDisplayLeft() ? hd.getPosX() : hd.getPosX() - (width/5)) < mouseX) &&
                ((hd.isDisplayLeft() ? hd.getPosX() + (width/5) : (width)) > mouseX) &&
                ((hd.isDisplayTop()  ? hd.getPosY() : hd.getPosY() - (height/4)) < mouseY) &&
                (hd.isDisplayTop()  ? hd.getPosY() + (height/4) : hd.getPosY()) > mouseY)
        {
            hudMouse.setHudPos(pos);
            hudMouse.setMouseX(mouseX);
            hudMouse.setMouseY(mouseY);
            return hudMouse;
        }
        return hudMouse;
    }
    
    @Override
    public void handleMouseInput() throws IOException
    {
        mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;    
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if ((mouseButton == 0) && hudMouse.getHudPos() != -1 && (hudMouse.getMouseX() == mouseX) && (hudMouse.getMouseY() == mouseY))
        {
            chosenHudPos = hudMouse.getHudPos();
            MusicOptionsUtil.setAdjustPositionHud(chosenHudPos);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {return false;}

    @Override
    public void onGuiClosed()
    {
        MusicOptionsUtil.setAdjustPositionHud(MusicOptionsUtil.getPositionHUD(playerIn));
        MusicOptionsUtil.setAdjustSizeHud(MusicOptionsUtil.getSizeHud(playerIn));
    }
}
