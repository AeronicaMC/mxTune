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
import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.managers.GroupHelper;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.ManageGroupMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.Set;

import static net.aeronica.mods.mxtune.managers.GroupHelper.MEMBER_ADD;
import static net.aeronica.mods.mxtune.managers.GroupHelper.getLeaderOfGroup;

public class GuiGroupJoin extends Screen
{
    private static final ResourceLocation guiTexture = new ResourceLocation(Reference.MOD_ID, "textures/gui/manage_group.png");

    /** The X size of the group window in pixels. */
    private int xSize = 239;

    /** The Y size of the group window in pixels. */
    private int ySize = 164;

    /** Starting X position for the Gui. Inconsistent use for Gui backgrounds. */
    private int guiLeft;

    /** Starting Y position for the Gui. Inconsistent use for Gui backgrounds. */
    private int guiTop;

    private PlayerEntity player;
    private Integer groupID;

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(false);

        this.player = mc.player;

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        buttonList.clear();

        int posX = guiLeft + 169;
        int posY = guiTop + 92;
        Button buttonYes = new Button(0, posX, posY, 60, 20, I18n.format("gui.yes"));

        posX = guiLeft + 169;
        posY = guiTop + 112;
        Button buttonNo = new Button(1, posX, posY, 60, 20, I18n.format("gui.no"));
        
        buttonList.add(buttonYes);
        buttonList.add(buttonNo);

        // This is a back door way to pass parameters to the GUI.
        this.groupID = Integer.valueOf(MusicOptionsUtil.getSParam1(player));
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

        /* draw "TITLE" at the top right */
        String title = I18n.format("mxtune.gui.GuiGroupJoin.title");
        int posX = guiLeft + xSize - fontRenderer.getStringWidth(title) - 12;
        int posY = guiTop + 12;
        fontRenderer.getStringWidth(title);
        fontRenderer.drawString(title, posX, posY, 0x000000);

        drawGroupMembers();
        super.drawScreen(i, j, f);
    }

    private void drawGroupMembers()
    {
        int posX = guiLeft + 12;
        int posY = guiTop + 12;

        if ((GroupHelper.getClientGroups() != null) || ((GroupHelper.getClientMembers() != null) && (groupID != null)))
        {
                // Always put the leader at the TOP of the list
                drawLeader(posX, posY);
                posY += 10;
                // Display the remaining members taking care to not print the leader a 2nd time
                drawMembers(posX, posY);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void drawLeader(int posX, int posY)
    {
        String leaderName = MXTune.proxy.getPlayerByEntityID(getLeaderOfGroup(groupID)).getName();
        this.fontRenderer.drawStringWithShadow(TextFormatting.YELLOW + leaderName, posX, posY, 16777215);
    }

    private void drawMembers(int posX, int posYIn)
    {
        int posY = posYIn;
        Set<Integer> members = GroupHelper.getClientMembers().keySet();
        for (Integer memberID : members)
        {
            if (groupID.equals(GroupHelper.getMembersGroupID(memberID)) && !memberID.equals(GroupHelper.getLeaderOfGroup(groupID)))
            {
                String memberName = MXTune.proxy.getPlayerByEntityID(memberID).getName();
                this.fontRenderer.drawStringWithShadow(memberName, posX, posY, 16777215);
                posY += 10;
            }
        }
    }

    @Override
    protected void actionPerformed(Button guibutton)
    {
        switch (guibutton.id)
        {
        case 2:
            /* No */
            cleanup();
            break;
        case 0:
            /* Yes */
            sendRequest(groupID, player.getEntityId());
            cleanup();
            break;
            
        default:
        }
        cleanup();
    }

    protected void cleanup()
    {
        mc.displayGuiScreen(null);
        mc.setIngameFocus();
    }
    
    private void drawGuiBackground()
    {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.renderEngine.bindTexture(guiTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    // TODO: Queue the group information server side, and only send a true/false packet to accept/decline.
    // Store in the MusicOptions capability. The other management commands will need to be rethought and
    // actions based on server side state with perhaps only button indexes sent
    private void sendRequest(Integer groupID, Integer memberID)
    {
        PacketDispatcher.sendToServer(new ManageGroupMessage(MEMBER_ADD, groupID, memberID));
    }
}
