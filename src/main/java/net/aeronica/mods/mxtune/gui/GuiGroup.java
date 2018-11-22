/*
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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.ManageGroupMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GuiGroup extends GuiScreen
{
    public static final int GUI_ID = 2;
    private static final ResourceLocation guiTexture = new ResourceLocation(Reference.MOD_ID, "textures/gui/manage_group.png");
    private int xSize = 239;
    private int ySize = 164;
    private int guiLeft;
    private int guiTop;

    private GuiButton btnCreate;
    private GuiButton btnLeave;
    private List<MemberButtons> memberButtons;

    private EntityPlayer player;

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
        btnCreate = new GuiButton(0, posX, posY, 60, 20, "Create");

        /* create button for leave and disable it initially */
        posX = guiLeft + 169;
        posY = guiTop + 112;
        btnLeave = new GuiButton(1, posX, posY, 60, 20, "Leave");

        posX = guiLeft + 169;
        posY = guiTop + 132;
        GuiButton btnCancel = new GuiButton(2, posX, posY, 60, 20, "Cancel");

        /* create member buttons for delete and promote */
        initMembersButtons();
        
        buttonList.add(btnCreate);
        buttonList.add(btnLeave);
        buttonList.add(btnCancel);
    }

    @Override
    public void onGuiClosed() {}

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

        drawMembers();
        /* Create and Leave buttons should always reflect group membership */
        btnLeave.enabled = GROUPS.getMembersGroupID(player.getEntityId()) != null;
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

        for (int i = 0; i < GROUPS.MAX_MEMBERS; i++)
        {
            memberButtons.add(i, memberButtons(did, pid, posX, posY));
            posY += 10;
            did++;
            pid++;
        }
    }

    private void clearMembersButtons()
    {
        for (int i = 0; i < GROUPS.MAX_MEMBERS; i++)
        {
            memberButtons.get(i).memberName = "";
            memberButtons.get(i).btn_delete.enabled = false;
            memberButtons.get(i).btn_delete.visible = false;
            memberButtons.get(i).btn_promote.enabled = false;
            memberButtons.get(i).btn_promote.visible = false;
        }
    }

    private Integer getMemberButton(int buttonID)
    {
        MemberButtons mb;
        Integer memberId = -1;
        for (int i = 0; i < GROUPS.MAX_MEMBERS; i++)
        {
            mb = memberButtons.get(i);
            if (mb.btn_delete.id == buttonID || mb.btn_promote.id == buttonID)
            {
                memberId = mb.memberId;
            }
        }
        return memberId;
    }

    private void drawMembers()
    {
        int posX = guiLeft + 12;
        int posY = guiTop + 12;
        Integer groupID;
        String leaderName;
        String memberName;
        int i = 0;

        clearMembersButtons();

        try
        {
            if (GROUPS.getClientGroups() != null || GROUPS.getClientMembers() != null)
            {
                groupID = GROUPS.getMembersGroupID(player.getEntityId());
                Integer leaderID = GROUPS.getLeaderOfGroup(groupID);
                if (groupID != null && leaderID !=null)
                {
                    /* Always put the leader at the TOP of the list */
                    leaderName = this.mc.world.getEntityByID(leaderID).getDisplayName().getUnformattedText();
                    this.fontRenderer.drawStringWithShadow(TextFormatting.YELLOW + leaderName, posX, posY, 16777215);
                    posY += 10;
                    /* Display the remaining members taking care to not print the leader a 2nd time. */
                    Set<Integer> members = GROUPS.getClientMembers().keySet();
                    for (Integer memberId : members)
                    {
                        if (groupID.equals(GROUPS.getMembersGroupID(memberId)) && !memberId.equals(leaderID))
                        {
                            memberName = this.mc.world.getEntityByID(memberId).getDisplayName().getUnformattedText();
                            this.fontRenderer.drawStringWithShadow(memberName, posX, posY, 16777215);
                            memberButtons.get(i).memberName = memberName;
                            memberButtons.get(i).memberId = memberId;
                            /* Only Leaders get to remove and promote other members! */
                            if (player.getEntityId() == (leaderID))
                            {
                                memberButtons.get(i).btn_delete.enabled = true;
                                memberButtons.get(i).btn_delete.visible = true;
                                memberButtons.get(i).btn_promote.enabled = true;
                                memberButtons.get(i).btn_promote.visible = true;
                            }
                            posY += 10;
                            i++;
                        }
                    }
                }
            }
        } catch(NullPointerException e)
        {
            ModLogger.error("GuiGroup#drawMembers: Oops %s", e);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        /* if button is disabled ignore click */
        if (!guibutton.enabled) { return; }

        if (guibutton.id >= 10 && guibutton.id < 100)
        {
            sendRequest(GROUPS.MEMBER_REMOVE, getMemberButton(guibutton.id));
            ModLogger.debug("+++ Gui Remove Member: " + GROUPS.MEMBER_REMOVE);
            return;
        }
        if (guibutton.id >= 100)
        {
            sendRequest(GROUPS.MEMBER_PROMOTE, getMemberButton(guibutton.id));
            ModLogger.debug("+++ Gui Promote Member: " + GROUPS.MEMBER_PROMOTE);
            return;
        }

        /* id 0 = create; 1 = leave; id 2 = cancel; 10-99 delete; 100+ promote; */
        switch (guibutton.id)
        {
        case 0:
            /* Create Group */
            sendRequest(GROUPS.GROUP_ADD, player.getEntityId());
            ModLogger.debug("+++ Gui Create Group: " + GROUPS.GROUP_ADD);
            break;

        case 1:
            /* Leave Group */
            sendRequest(GROUPS.MEMBER_REMOVE, player.getEntityId());
            ModLogger.debug("+++ Gui Leave Group: " + GROUPS.MEMBER_REMOVE);
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
        GuiButton btn_delete;
        GuiButton btn_promote;
        String memberName;
        Integer memberId;
    }

    private MemberButtons memberButtons(int did, int pid, int xpos, int ypos)
    {
        MemberButtons buttons = new MemberButtons();
        buttons.btn_delete = new GuiButton(did, xpos, ypos, 10, 10, "D");
        buttons.btn_delete.visible = false;
        buttons.btn_delete.enabled = false;
        buttons.btn_promote = new GuiButton(pid, xpos + 10, ypos, 10, 10, "L");
        buttons.btn_promote.visible = false;
        buttons.btn_promote.enabled = false;
        buttonList.add(buttons.btn_delete);
        buttonList.add(buttons.btn_promote);
        return buttons;
    }
}
