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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.ManageGroupMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GuiGroup extends GuiScreen
{
    public static final int GUI_ID = 2;

    private Minecraft mc;
    private FontRenderer fontRenderer = null;

    private static final ResourceLocation score_entryTexture = new ResourceLocation(MXTuneMain.prependModID("textures/gui/manage_group.png"));

    /** The X size of the group window in pixels. */
    protected int xSize = 239;

    /** The Y size of the group window in pixels. */
    protected int ySize = 164;

    /**
     * Starting X position for the Gui. Inconsistent use for Gui backgrounds.
     */
    protected int guiLeft;

    /**
     * Starting Y position for the Gui. Inconsistent use for Gui backgrounds.
     */
    protected int guiTop;

    private String TITLE = "Jam Session";

    private GuiButton btn_create, btn_leave, btn_cancel;
    private List<MemberButtons> list_btn_members;

    private EntityPlayer player;

    /** Initializes the GUI elements. */
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

        /** create button for group creation and disable it initially */
        int posX = guiLeft + 169;
        int posY = guiTop + 92;
        btn_create = new GuiButton(0, posX, posY, 60, 20, "Create");

        /** create button for leave and disable it initially */
        posX = guiLeft + 169;
        posY = guiTop + 112;
        btn_leave = new GuiButton(1, posX, posY, 60, 20, "Leave");

        posX = guiLeft + 169;
        posY = guiTop + 132;
        btn_cancel = new GuiButton(2, posX, posY, 60, 20, "Cancel");

        /** create member buttons for delete and promote */
        initMembersButtons();
        
        buttonList.add(btn_create);
        buttonList.add(btn_leave);
        buttonList.add(btn_cancel);
    }

    /** Called when the screen is unloaded. Used to disable keyboard repeat events, etc */
    @Override
    public void onGuiClosed() {}

    @Override
    public boolean doesGuiPauseGame() {return false;}

    /** Draws the screen and all the components in it. */
    @Override
    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        drawGuiBackground();

        /** draw "TITLE" at the top right */
        int posX = guiLeft + xSize - fontRenderer.getStringWidth(TITLE) - 12;
        int posY = guiTop + 12;
        fontRenderer.getStringWidth(TITLE);
        fontRenderer.drawString(TITLE, posX, posY, 0x000000);

        drawMembers();
        /** Create and Leave buttons should always reflect group membership */
        btn_create.enabled = !(btn_leave.enabled = GROUPS.getMembersGroupID(player.getEntityId()) != null);

        /** draw the things in the controlList (buttons) */
        super.drawScreen(i, j, f);
    }

    private void initMembersButtons()
    {
        int posX = guiLeft + 104;
        int posY = guiTop + 12 + 10;
        int did = 10;
        int pid = 100;

        list_btn_members = new ArrayList<MemberButtons>();

        for (int i = 0; i < GROUPS.MAX_MEMBERS; i++)
        {
            list_btn_members.add(i, memberButtons(did, pid, posX, posY));
            posY += 10;
            did++;
            pid++;

        }
    }

    private void clearMembersButtons()
    {
        for (int i = 0; i < GROUPS.MAX_MEMBERS; i++)
        {
            list_btn_members.get(i).memberName = "";
            list_btn_members.get(i).btn_delete.enabled = false;
            list_btn_members.get(i).btn_delete.visible = false;
            list_btn_members.get(i).btn_promote.enabled = false;
            list_btn_members.get(i).btn_promote.visible = false;
        }
    }

    private String getMemberButton(int buttonID)
    {
        MemberButtons mb;
        String memberName = "UnFound_UnFound_Unfound";
        for (int i = 0; i < GROUPS.MAX_MEMBERS; i++)
        {
            mb = list_btn_members.get(i);
            if (mb.btn_delete.id == buttonID || mb.btn_promote.id == buttonID)
            {
                memberName = mb.memberName;
            }
        }
        return memberName;
    }

    private void drawMembers()
    {
        int posX = guiLeft + 12;
        int posY = guiTop + 12;
        Integer groupID;
        Integer memberID;
        String leaderName;
        String memberName;
        int i = 0;

        clearMembersButtons();

        /** TODO: use/add sensible methods to make readable - partially done... */
        if (GROUPS.getClientGroups() != null || GROUPS.getClientMembers() != null)
        {
            groupID = GROUPS.getMembersGroupID(player.getEntityId());
            if (groupID != null)
            {
                /** Always put the leader at the TOP of the list */
                leaderName = player.worldObj.getEntityByID(GROUPS.getLeaderOfGroup(groupID)).getDisplayName().getUnformattedText();
                fontRenderer.drawStringWithShadow(TextFormatting.YELLOW + leaderName, posX, posY, 16777215);
                posY += 10;
                /** Display the remaining members taking care to not print the leader a 2nd time. */
                Set<Integer> set = GROUPS.getClientMembers().keySet();
                for (Iterator<Integer> im = set.iterator(); im.hasNext();)
                {
                    memberID = im.next();
                    if (groupID.equals(GROUPS.getMembersGroupID(memberID)) && !memberID.equals(GROUPS.getLeaderOfGroup(groupID)))
                    {
                        memberName = player.worldObj.getEntityByID(memberID).getDisplayName().getUnformattedText();
                        fontRenderer.drawStringWithShadow(memberName, posX, posY, 16777215);
                        list_btn_members.get(i).memberName = memberName;
                        /** Only Leaders get to remove and promote other members! */
                        if (player.getEntityId() == (GROUPS.getLeaderOfGroup(groupID)))
                        {
                            list_btn_members.get(i).btn_delete.enabled = true;
                            list_btn_members.get(i).btn_delete.visible = true;
                            list_btn_members.get(i).btn_promote.enabled = true;
                            list_btn_members.get(i).btn_promote.visible = true;
                        }
                        posY += 10;
                        i++;
                    }
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        /** if button is disabled ignore click */
        if (!guibutton.enabled) { return; }

        if (guibutton.id >= 10 && guibutton.id < 100)
        {
            sendRequest(GROUPS.MEMBER_REMOVE, "", getMemberButton(guibutton.id));
            ModLogger.debug("+++ Gui Remove Member: " + GROUPS.MEMBER_REMOVE);
            return;
        }
        if (guibutton.id >= 100)
        {
            sendRequest(GROUPS.MEMBER_PROMOTE, "", getMemberButton(guibutton.id));
            ModLogger.debug("+++ Gui Promote Member: " + GROUPS.MEMBER_PROMOTE);
            return;
        }

        /** id 0 = create; 1 = leave; id 2 = cancel; 10-99 delete; 100+ promote; */
        switch (guibutton.id)
        {
        case 0:
            /** Create Group */
            sendRequest(GROUPS.GROUP_ADD, "", player.getDisplayName().getUnformattedText());
            ModLogger.debug("+++ Gui Create Group: " + GROUPS.GROUP_ADD);
            break;

        case 1:
            /** Leave Group */
            sendRequest(GROUPS.MEMBER_REMOVE, "", player.getDisplayName().getUnformattedText());
            ModLogger.debug("+++ Gui Leave Group: " + GROUPS.MEMBER_REMOVE);
            break;

        case 2:
            /** Cancel remove the GUI */
        default:
        }
        mc.displayGuiScreen(null);
        mc.setIngameFocus();
    }

    /** Gets the image for the background and renders it in the middle of the screen. */
    protected void drawGuiBackground()
    {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.renderEngine.bindTexture(score_entryTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    protected void sendRequest(GROUPS operation, String leaderName, String memberName)
    {
        Integer groupID = null;
        Integer memberID = player.worldObj.getPlayerEntityByName(memberName).getEntityId();
        PacketDispatcher.sendToServer(new ManageGroupMessage(operation.toString(), groupID, memberID));
    }

    protected class MemberButtons
    {
        GuiButton btn_delete;
        GuiButton btn_promote;
        String memberName;
    }

    /** Yes I know hard coding those button sizes is uncool - internationally recognized icons instead ?*/
    protected MemberButtons memberButtons(int did, int pid, int xpos, int ypos)
    {
        MemberButtons btns_member = new MemberButtons();
        btns_member.btn_delete = new GuiButton(did, xpos, ypos, 10, 10, "D");
        btns_member.btn_delete.visible = false;
        btns_member.btn_delete.enabled = false;
        btns_member.btn_promote = new GuiButton(pid, xpos + 10, ypos, 10, 10, "L");
        btns_member.btn_promote.visible = false;
        btns_member.btn_promote.enabled = false;
        buttonList.add(btns_member.btn_delete);
        buttonList.add(btns_member.btn_promote);
        return btns_member;
    }
}
