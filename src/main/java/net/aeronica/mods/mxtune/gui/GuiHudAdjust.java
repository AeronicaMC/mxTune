/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.HudOptionsMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class GuiHudAdjust extends GuiScreen
{

    static final String TITLE = I18n.format("mxtune.gui.hudAdjust.title");
    static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    
    GuiButtonExt buttonCancel;
    GuiButtonExt buttonDone;
    GuiSliderMX sliderHudSize;

    EntityPlayer playerIn;
    boolean midiUnavailable;
    int initialHudPos;
    private int lastHudPos = 0;
    private int mouseX;
    private int mouseY;
    
    public GuiHudAdjust()
    {
        mc = Minecraft.getMinecraft();
        this.playerIn = mc.player;
        midiUnavailable = MIDISystemUtil.midiUnavailable();
    }
    
    @Override
    public void initGui()
    {
        float initialHudSize = (MusicOptionsUtil.getSizeHud(playerIn)*100) < 50F ? 50F : MusicOptionsUtil.getSizeHud(playerIn)*100;
        this.buttonList.clear();
        lastHudPos = initialHudPos = MusicOptionsUtil.getPositionHUD(playerIn);
        MusicOptionsUtil.setAdjustPositionHud(initialHudPos);
        
        int y = height/2;
        int buttonWidth = 100;
        int x = (width / 2) - (buttonWidth/2);
        sliderHudSize = new GuiSliderMX(2, x, y, buttonWidth, 20,  I18n.format("mxtune.gui.hudAdjust.size"), initialHudSize, 50F, 100F, 1F);
        y+=20;
        buttonDone =   new GuiButtonExt(0, x, y, buttonWidth, 20, I18n.format("gui.done"));
        y+=20;
        buttonCancel = new GuiButtonExt(1, x, y, buttonWidth, 20, I18n.format("gui.cancel"));
        
        this.buttonList.add(buttonCancel);
        this.buttonList.add(buttonDone);
        this.buttonList.add(sliderHudSize);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        /** if button is disabled ignore click */
        if (button.enabled)
        {
            switch(button.id)
            {
            case 0: /* done   */
                PacketDispatcher.sendToServer(new HudOptionsMessage(lastHudPos, MusicOptionsUtil.isHudDisabled(playerIn), sliderHudSize.getValue()/100));
                MusicOptionsUtil.setAdjustPositionHud(lastHudPos);
                MusicOptionsUtil.setHudOptions(playerIn, MusicOptionsUtil.isHudDisabled(playerIn), lastHudPos, sliderHudSize.getValue()/100);
                mc.displayGuiScreen(new GuiMusicOptions(playerIn));  
                break;
            case 1: /* cancel */
                MusicOptionsUtil.setAdjustPositionHud(initialHudPos);
                mc.displayGuiScreen(new GuiMusicOptions(playerIn));
                break;
            default:
            }
        }
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        /** capture the ESC key so we close cleanly */
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            this.actionPerformed((GuiButton) buttonList.get(buttonCancel.id));
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen()
    {
        MusicOptionsUtil.setAdjustSizeHud(sliderHudSize.getValue()/100);
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
        /** draw "TITLE" at the top/right column middle */
        int posX = (this.width - getFontRenderer().getStringWidth(localTITLE)) / 2 ;
        int posY = 10;
        getFontRenderer().drawStringWithShadow(localTITLE, posX, posY, 0xD3D3D3);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHudPositions()
    {
        int height = this.height-GuiJamOverlay.HOTBAR_CLEARANCE;
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
                    ((i == mouseOverHudPos(hd, i)) ? color.getRGB() : darkColor.getRGB()) + (128 << 24));
        }
    }

    private int mouseOverHudPos(HudData hd, int pos)
    {
        int height = this.height-GuiJamOverlay.HOTBAR_CLEARANCE;
        if (
                ((hd.isDisplayLeft() ? hd.getPosX() : hd.getPosX() - (width/5)) < mouseX) &&
                ((hd.isDisplayLeft() ? hd.getPosX() + (width/5) : (width)) > mouseX) &&
                ((hd.isDisplayTop()  ? hd.getPosY() : hd.getPosY() - (height/4)) < mouseY) &&
                ((hd.isDisplayTop()  ? hd.getPosY() + (height/4) : hd.getPosY())) > mouseY)
        {
            return lastHudPos = pos;
        }
        return -1;
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
        MusicOptionsUtil.setAdjustPositionHud(lastHudPos);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {return false;}
    
    public Minecraft getMinecraftInstance() {return mc;}

    public FontRenderer getFontRenderer() {return mc.fontRenderer;}

}
