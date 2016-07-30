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

import java.util.Iterator;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.ManageGroupMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiGroupJoin extends GuiScreen
{
    public static final int GUI_ID = 5;

    private Minecraft mc;
    private FontRenderer fontRenderer = null;

    private static final ResourceLocation score_entryTexture = new ResourceLocation(MXTuneMain.prependModID("textures/gui/manage_group.png"));

    /** The X size of the group window in pixels. */
    protected int xSize = 239;

    /** The Y size of the group window in pixels. */
    protected int ySize = 164;

    /** Starting X position for the Gui. Inconsistent use for Gui backgrounds. */
    protected int guiLeft;

    /** Starting Y position for the Gui. Inconsistent use for Gui backgrounds. */
    protected int guiTop;

    private GuiButton btn_yes, btn_no;
//    private GuiSlider gTest;

    private EntityPlayer player;

    private String groupID;

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

        int posX = guiLeft + 169;
        int posY = guiTop + 92;
        btn_yes = new GuiButton(0, posX, posY, 60, 20, I18n.format("gui.yes"));

        posX = guiLeft + 169;
        posY = guiTop + 112;
        btn_no = new GuiButton(1, posX, posY, 60, 20, I18n.format("gui.no"));

//        gTest = new GuiSlider(2, posX, posY - 50, 60, 20, "Test", 10f, 1f, 20f, 1f);
        
        buttonList.add(btn_yes);
        buttonList.add(btn_no);
//        buttonList.add(gTest);

        /** This is a back door way to pass parameters to the GUI. */
        this.groupID = MusicOptionsUtil.getSParam1(player);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        drawGuiBackground();

        /** draw "TITLE" at the top right */
        String title = I18n.format("mxtune.gui.GuiGroupJoin.title", new Object[0]);
        int posX = guiLeft + xSize - fontRenderer.getStringWidth(title) - 12;
        int posY = guiTop + 12;
        fontRenderer.getStringWidth(title);
        fontRenderer.drawString(title, posX, posY, 0x000000);

        drawMembers();
        super.drawScreen(i, j, f);
    }

    private void drawMembers()
    {
        int posX = guiLeft + 12;
        int posY = guiTop + 12;
        String member;

        if (GROUPS.getClientGroups() != null || GROUPS.getClientMembers() != null)
        {
            if (groupID != null)
            {
                /** Always put the leader at the TOP of the list */
                fontRenderer.drawStringWithShadow(GROUPS.getLeaderOfGroup(groupID), posX, posY, 16777215);
                posY += 10;
                /** display the remaining members taking care to not print the leader a 2nd time. */
                Set<String> set = GROUPS.getClientMembers().keySet();
                for (Iterator<String> im = set.iterator(); im.hasNext();)
                {
                    member = im.next();
                    if (groupID.equalsIgnoreCase(GROUPS.getMembersGroupID(member)) && !member.equalsIgnoreCase(GROUPS.getLeaderOfGroup(groupID)))
                    {
                        fontRenderer.drawStringWithShadow(member, posX, posY, 16777215);

                        /** Only Leaders get to remove and promote other members! */
                        if (player.getDisplayName().getUnformattedText().equalsIgnoreCase(GROUPS.getLeaderOfGroup(groupID)))
                        {
                            /** um, I forget why this is here */
                        }
                        posY += 10;
                    }
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {

        /** id 0 = yes; 1 = no; */
        switch (guibutton.id)
        {
        case 2:
            return;
        case 0:
            /** Yes */
            sendRequest(GROUPS.MEMBER_ADD, groupID, player.getDisplayName().getUnformattedText());

        default:
        }
        mc.displayGuiScreen(null);
        mc.setIngameFocus();
    }

    protected void drawGuiBackground()
    {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.renderEngine.bindTexture(score_entryTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    protected void sendRequest(GROUPS operation, String groupID, String memberName)
    {
        PacketDispatcher.sendToServer(new ManageGroupMessage(operation.toString(), groupID, memberName));
    }
}
