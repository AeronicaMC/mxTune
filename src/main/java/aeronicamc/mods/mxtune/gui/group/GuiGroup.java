package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.gui.MXScreen;
import aeronicamc.mods.mxtune.gui.TextColorFg;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import aeronicamc.mods.mxtune.managers.Group;
import aeronicamc.mods.mxtune.managers.GroupClient;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.GetGroupPinMessage;
import aeronicamc.mods.mxtune.util.IGroupClientChangedCallback;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuiGroup extends MXScreen implements IGroupClientChangedCallback
{
    private static final ITextComponent DISBAND = new TranslationTextComponent("gui.mxtune.button.disband");
    private final MXButton buttonDone = new MXButton(p -> done());
    private final MXButton buttonDisband = new MXButton(p -> disband());
    private final MXTextFieldWidget groupDisplay = new MXTextFieldWidget(1024);
    private final MXTextFieldWidget pinDisplay = new MXTextFieldWidget(4);
    private final MemberDisplay memberDisplay;
    private int counter;
    private final int groupId;
    private int lastHash;
    private final int lineHeight;
    private final int nameWidth;
    public GuiGroup()
    {
        super(StringTextComponent.EMPTY);
        groupId = GroupClient.getGroup(player().getId()).getGroupId();
        lineHeight = mc().font.lineHeight + 6;
        nameWidth = mc().font.width("MMMMMMMMMMMM") + 4;
        memberDisplay = new MemberDisplay(this);
        GroupClient.setCallback(this);
    }

    private int getGroupId()
    {
        return this.groupId;
    }

    private int getLineHeight()
    {
        return this.lineHeight;
    }

    private int getNameWidth()
    {
        return this.nameWidth;
    }

    @Override
    protected void init()
    {
        super.init();

        PacketDispatcher.sendToServer(new GetGroupPinMessage());
        memberDisplay.initMemberDisplay(10, 10);

        int groupDisplayWidth = mc().font.width(GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId));
        int pinDisplayWidth = mc().font.width("0000");
        groupDisplay.setLayout(width/2 - groupDisplayWidth /2, 10, groupDisplayWidth + 8, 20);
        groupDisplay.setBordered(false);
        ITextComponent msg = GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId);
        lastHash = msg.hashCode();
        groupDisplay.setValue(msg.getString());

        pinDisplay.setLayout(width / 2 - pinDisplayWidth /2, groupDisplay.getBottom() + 4, pinDisplayWidth + 8, 20);
        pinDisplay.setValue("----");

        buttonDone.setLayout(width - 100 - 10,10 ,100,20);
        buttonDone.setMessage(DialogTexts.GUI_DONE);
        buttonDisband.setLayout(buttonDone.x, buttonDone.y + 22, 100, 20);
        buttonDisband.setMessage(DISBAND);
        addButton(buttonDone);
        addButton(buttonDisband);
    }

    @Override
    public void onGroupClientChanged(Type type)
    {
        switch (type)
        {
            case Group:
                this.reInit();
                break;
            case Member:
                // this.updateStatus()
                break;
            case Pin:
                pinDisplay.setValue(GroupClient.getPrivatePin());
                break;
        }
        System.out.printf("callback: %s\n", type);
    }

    private void reInit()
    {
        this.buttons.clear();
        this.children.clear();
        this.setFocused(null);
        this.init();
    }

    private void promote(Button button)
    {
        MXButton mxButton = (MXButton) button;
        System.out.printf("promote memberId: %d\n", mxButton.getIndex());
    }

    private void remove(Button button)
    {
        MXButton mxButton = (MXButton) button;
        System.out.printf("remove memberId: %d\n", mxButton.getIndex());
    }

    private void disband()
    {
        this.onClose();
    }

    private void done()
    {
        this.onClose();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.fillGradient(pMatrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        groupDisplay.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        pinDisplay.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        memberDisplay.renderMemberDisplay(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    @Override
    public void tick()
    {
        if (counter++ % 20 == 0)
        {
            ITextComponent msg = GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId);
            groupDisplay.setValue(msg.getString());
            if (lastHash != msg.hashCode())
            {
                lastHash = msg.hashCode();
                this.init();
            }
        }
        super.tick();
    }

    @Override
    public void onClose()
    {
        GroupClient.removeCallback();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return true;
    }

    private static ClientPlayerEntity player()
    {
        return Objects.requireNonNull(mc().player);
    }

    private static Minecraft mc()
    {
        return Minecraft.getInstance();
    }

    private static class MemberDisplay
    {
        private final GuiGroup parent;
        private final List<MemberInfo> memberButtons = new ArrayList<>();
        int xPos;
        int yPos;
        final int maxWidth;
        final int maxHeight;

        MemberDisplay(GuiGroup parent)
        {
            this.parent = parent;
            maxHeight = (this.parent.getLineHeight() * 17) + 4;
            maxWidth = this.parent.getNameWidth() + 40;
        }

        private void initMemberDisplay(int posX, int posY)
        {
            this.xPos = posX;
            this.yPos = posY + 2;
            int y = this.yPos;
            memberButtons.clear();
            Group group = GroupClient.getGroupById(parent.getGroupId());
            System.out.printf("Group Id %d, Leader Id: %d\n", parent.getGroupId(), group.getLeader());

            y = leaderFirst(posX, y, group.getLeader());
            for (Integer memberId : GroupClient.getGroupById(parent.getGroupId()).getMembers())
            {
                if (!GroupClient.isLeader(memberId))
                {
                    MemberInfo memberInfo = new MemberInfo(posX, y, parent, memberId, parent::promote, parent::remove);
                    parent.addButton(memberInfo.promote);
                    parent.addButton(memberInfo.remove);
                    memberButtons.add(memberInfo);
                    y += parent.getLineHeight();
                }
            }
        }

        private int leaderFirst(int posX, int y, int memberId)
        {
            MemberInfo memberInfo = new MemberInfo(posX, y, parent, memberId, parent::promote, parent::remove);
            memberInfo.promote.active = false;
            memberInfo.promote.visible = false;
            memberInfo.remove.active = player().getId() == memberId;
            memberInfo.remove.visible = player().getId() == memberId;
            parent.addButton(memberInfo.promote);
            parent.addButton(memberInfo.remove);
            memberButtons.add(memberInfo);
            return y + parent.getLineHeight();
        }

        private void renderMemberDisplay(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
        {
            fill(pMatrixStack, this.xPos - 1, this.yPos - 1, this.xPos + this.maxWidth + 1, this.yPos + this.maxHeight + 1, -6250336);
            fill(pMatrixStack, this.xPos, this.yPos, this.yPos + this.maxWidth - 2, this.yPos + this.maxHeight, -16777216);
            memberButtons.forEach(p -> p.memberDraw(pMatrixStack, pMouseX, pMouseY, pPartialTicks));
        }
    }

    private static class MemberInfo
    {
        static final ITextComponent PROMOTE = new StringTextComponent("▲").withStyle(TextFormatting.GREEN, TextFormatting.BOLD); // '▲' dec: 9650 hex: 25B2 BLACK UP-POINTING TRIANGLE
        static final ITextComponent REMOVE = new StringTextComponent("x").withStyle(TextFormatting.RED, TextFormatting.BOLD); // 'x'
        final int xPos;
        final int yPos;
        private final int nameWidth;
        private final int lineHeight;
        final MXButton promote;
        final MXButton remove;
        final int memberId;
        final ITextComponent name;

        MemberInfo(int xPos, int yPos, GuiGroup parent, int memberId, Button.IPressable pPromote, Button.IPressable pRemove)
        {
            this.xPos = xPos;
            this.yPos = yPos;
            this.nameWidth = parent.nameWidth;
            this.lineHeight = parent.lineHeight;
            this.memberId = memberId;
            this.name = player().level.getEntity(memberId) != null ? player().level.getEntity(memberId).getDisplayName() : new StringTextComponent(String.format("< %d >", memberId));
            promote = new MXButton(pPromote);
            promote.setMessage(MemberInfo.PROMOTE);
            promote.setLayout(xPos + nameWidth, yPos, 20, this.lineHeight);
            promote.setIndex(memberId);
            promote.active = GroupClient.isLeader(player().getId());

            remove = new MXButton(pRemove);
            remove.setMessage(MemberInfo.REMOVE);
            remove.setLayout(promote.x + 20, yPos, 20, this.lineHeight);
            remove.setIndex(memberId);
            remove.active = GroupClient.isLeader(player().getId()) || memberId == player().getId();
        }

        public int getNameWidth()
        {
            return nameWidth;
        }

        public int getLineHeight()
        {
            return lineHeight;
        }

        void memberDraw(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
        {
            mc().font.draw(pMatrixStack, name, (float)xPos + 2, (float)yPos + 2, GroupClient.isLeader(memberId) ? TextColorFg.YELLOW: TextColorFg.WHITE);
        }
    }
}
