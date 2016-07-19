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

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiMusicOptions extends GuiScreen
{
    public static final int GUI_ID = 8;
    private Minecraft mc;
    private String TITLE;

    public GuiMusicOptions() {}
    
    @Override
    public void updateScreen()
    {
        // TODO Auto-generated method stub
        super.updateScreen();
    }

    @Override
    public void initGui()
    {
        TITLE = I18n.format("mxtune.gui.GuiMusicOptions.title", new Object[0]);
        this.mc = Minecraft.getMinecraft();
        
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();

        /** draw "TITLE" at the top/right column middle */
        int posX = (this.width - getFontRenderer().getStringWidth(TITLE)) / 2 ;
        int posY = 10;
        getFontRenderer().drawStringWithShadow(TITLE, posX, posY, 0xD3D3D3);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    /* (non-Javadoc)
     * @see net.minecraft.client.gui.GuiScreen#actionPerformed(net.minecraft.client.gui.GuiButton)
     */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        // TODO Auto-generated method stub
        super.actionPerformed(button);
    }
    /* (non-Javadoc)
     * @see net.minecraft.client.gui.GuiScreen#keyTyped(char, int)
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        // TODO Auto-generated method stub
        super.keyTyped(typedChar, keyCode);
    }
    /* (non-Javadoc)
     * @see net.minecraft.client.gui.GuiScreen#handleMouseInput()
     */
    @Override
    public void handleMouseInput() throws IOException
    {
        // TODO Auto-generated method stub
        super.handleMouseInput();
    }
    /* (non-Javadoc)
     * @see net.minecraft.client.gui.GuiScreen#mouseClicked(int, int, int)
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        // TODO Auto-generated method stub
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public Minecraft getMinecraftInstance() {return mc;}

    public FontRenderer getFontRenderer() {return mc.fontRendererObj;}

}
