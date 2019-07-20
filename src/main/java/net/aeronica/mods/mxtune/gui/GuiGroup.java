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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.managers.GroupHelper;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.ManageGroupMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GuiGroup extends Screen
{
    private static final ResourceLocation guiTexture = new ResourceLocation(Reference.MOD_ID, "textures/gui/manage_group.png");
    private int xSize = 239;
    private int ySize = 164;
    private int guiLeft;
    private int guiTop;

    private Button btnCreate;
    private Button btnLeave;
    private List<MemberButtons> memberButtons;

    private PlayerEntity player;

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(false);

        this.player = mc.player;

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        buttonList.clear();

        /* create button for group creation and disable it initially */
        int posX = guiLeft + 169;
        int posY = guiTop + 92;
        btnCreate = new Button(0, posX, posY, 60, 20, "Create");

        /* create button for leave and disable it initially */
        posX = guiLeft + 169;
        posY = guiTop + 112;
        btnLeave = new Button(1, posX, posY, 60, 20, "Leave");

        posX = guiLeft + 169;
        posY = guiTop + 132;
        Button btnCancel = new Button(2, posX, posY, 60, 20, "Cancel");

        /* create member buttons for delete and promote */
        initMembersButtons();
        
        buttonList.add(btnCreate);
        buttonList.add(btnLeave);
        buttonList.add(btnCancel);
    }

    @Override
    public boolean doesGuiPauseGame() {return false;}

    @Override
    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        drawGuiBackground();

        /* draw "TITLE" at the top right */
        String title = I18n.format("mxtune.gui.GuiGroup.title");
        int posX = guiLeft + xSize - this.fontRenderer.getStringWidth(title) - 12;
        int posY = guiTop + 12;
        this.fontRenderer.getStringWidth(title);
        this.fontRenderer.drawString(title, posX, posY, 0x000000);

        drawGroupMembers();
        /* Create and Leave buttons should always reflect group membership */
        btnLeave.enabled = GroupHelper.getMembersGroupID(player.getEntityId()) != null;
        btnCreate.enabled = !btnLeave.enabled;

        /* draw the things in the controlList (buttons) */
        super.drawScreen(i, j, f);
    }

    private void initMembersButtons()
    {
        int posX = guiLeft + 104;
        int posY = guiTop + 12 + 10;
        int did = 10;
        int pid = 100;

        memberButtons = new ArrayList<>();

        for (int i = 0; i < GroupHelper.MAX_MEMBERS; i++)
        {
            memberButtons.add(i, memberButtons(did, pid, posX, posY));
            posY += 10;
            did++;
            pid++;
        }
    }

    private void clearMembersButtons()
    {
        for (int i = 0; i < GroupHelper.MAX_MEMBERS; i++)
        {
            memberButtons.get(i).memberName = "";
            memberButtons.get(i).buttonRemove.enabled = false;
            memberButtons.get(i).buttonRemove.visible = false;
            memberButtons.get(i).buttonPromote.enabled = false;
            memberButtons.get(i).buttonPromote.visible = false;
        }
    }

    private Integer getMemberByButtonID(int buttonID)
    {
        MemberButtons mb;
        Integer memberId = -1;
        for (int i = 0; i < GroupHelper.MAX_MEMBERS; i++)
        {
            mb = memberButtons.get(i);
            if (mb.buttonRemove.id == buttonID || mb.buttonPromote.id == buttonID)
            {
                memberId = mb.memberId;
            }
        }
        return memberId;
    }

    private void drawGroupMembers()
    {
        int posX = guiLeft + 12;
        int posY = guiTop + 12;
        Integer groupID;

        clearMembersButtons();
        if (GroupHelper.getClientGroups() != null || GroupHelper.getClientMembers() != null)
        {
            groupID = GroupHelper.getMembersGroupID(player.getEntityId());
            Integer leaderID = GroupHelper.getLeaderOfGroup(groupID);
            if (groupID != null && leaderID != null)
            {
                // Always put the leader at the TOP of the list
                drawLeader(leaderID, posX, posY);
                posY += 10;
                // Display the remaining members taking care to not print the leader a 2nd time
                drawMembers(groupID, leaderID, posX, posY);
            }
        }
    }

    private void drawLeader(Integer leaderID, int posX, int posY)
    {
        String leaderName = Objects.requireNonNull(this.mc.world.getEntityByID(leaderID)).getName();
        this.fontRenderer.drawStringWithShadow(TextFormatting.YELLOW + leaderName, posX, posY, 16777215);
    }

    private void drawMembers(Integer groupID, Integer leaderID, int posX, int posYIn)
    {
        int i = 0;
        int posY = posYIn;
        Set<Integer> members = GroupHelper.getClientMembers().keySet();
        for (Integer memberId : members)
        {
            if (groupID.equals(GroupHelper.getMembersGroupID(memberId)) && !memberId.equals(leaderID))
            {
                String memberName = Objects.requireNonNull(this.mc.world.getEntityByID(memberId)).getName();
                this.fontRenderer.drawStringWithShadow(memberName, posX, posY, 16777215);
                memberButtons.get(i).memberName = memberName;
                memberButtons.get(i).memberId = memberId;
                /* Only Leaders get to remove and promote other members! */
                if (player.getEntityId() == (leaderID))
                {
                    memberButtons.get(i).buttonRemove.enabled = true;
                    memberButtons.get(i).buttonRemove.visible = true;
                    memberButtons.get(i).buttonPromote.enabled = true;
                    memberButtons.get(i).buttonPromote.visible = true;
                }
                posY += 10;
                i++;
            }
        }
    }

    @Override
    protected void actionPerformed(Button guibutton)
    {
        // if button is disabled ignore click
        if (!guibutton.enabled) { return; }

        // 10-99 delete; 100+ promote
        if (guibutton.id >= 10 && guibutton.id < 100)
        {
            sendRequest(GroupHelper.MEMBER_REMOVE, getMemberByButtonID(guibutton.id));
            ModLogger.debug("+++ Gui Remove Member: " + GroupHelper.MEMBER_REMOVE);
            return;
        }
        if (guibutton.id >= 100)
        {
            sendRequest(GroupHelper.MEMBER_PROMOTE, getMemberByButtonID(guibutton.id));
            ModLogger.debug("+++ Gui Promote Member: " + GroupHelper.MEMBER_PROMOTE);
            return;
        }

        switch (guibutton.id)
        {
        case 0:
            /* Create Group */
            sendRequest(GroupHelper.GROUP_ADD, player.getEntityId());
            ModLogger.debug("+++ Gui Create Group: " + GroupHelper.GROUP_ADD);
            break;

        case 1:
            /* Leave Group */
            sendRequest(GroupHelper.MEMBER_REMOVE, player.getEntityId());
            ModLogger.debug("+++ Gui Leave Group: " + GroupHelper.MEMBER_REMOVE);
            break;

        case 2:
            /* Cancel remove the GUI */
        default:
        }
        mc.displayGuiScreen(null);
        mc.setIngameFocus();
    }

    /* Gets the image for the background and renders it in the middle of the screen. */
    private void drawGuiBackground()
    {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.renderEngine.bindTexture(guiTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    private void sendRequest(int operation, Integer memberId)
    {
        PacketDispatcher.sendToServer(new ManageGroupMessage(operation, null, memberId));
    }

    protected class MemberButtons
    {
        Button buttonRemove;
        Button buttonPromote;
        String memberName;
        Integer memberId;
    }

    private MemberButtons memberButtons(int deleteId, int promoteId, int xPosition, int yPosition)
    {
        MemberButtons buttons = new MemberButtons();
        buttons.buttonRemove = new Button(deleteId, xPosition, yPosition, 10, 10, "D");
        buttons.buttonRemove.visible = false;
        buttons.buttonRemove.enabled = false;
        buttons.buttonPromote = new Button(promoteId, xPosition + 10, yPosition, 10, 10, "L");
        buttons.buttonPromote.visible = false;
        buttons.buttonPromote.enabled = false;
        buttonList.add(buttons.buttonRemove);
        buttonList.add(buttons.buttonPromote);
        return buttons;
    }
}
