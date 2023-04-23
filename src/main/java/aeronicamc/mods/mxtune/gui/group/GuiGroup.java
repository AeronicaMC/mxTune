package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.gui.MXScreen;
import aeronicamc.mods.mxtune.gui.ModGuiHelper;
import aeronicamc.mods.mxtune.gui.TextColorFg;
import aeronicamc.mods.mxtune.gui.widget.GuiHelpButton;
import aeronicamc.mods.mxtune.gui.widget.IHooverText;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXLabel;
import aeronicamc.mods.mxtune.managers.Group;
import aeronicamc.mods.mxtune.managers.GroupClient;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.GroupCmdMessage;
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

import static aeronicamc.mods.mxtune.gui.ModGuiHelper.*;
import static aeronicamc.mods.mxtune.network.messages.GroupCmdMessage.Cmd;

public class GuiGroup extends MXScreen implements IGroupClientChangedCallback
{
    private static final ITextComponent MAKE_GROUP = new TranslationTextComponent("gui.mxtune.button.make_group");
    private static final ITextComponent DISBAND = new TranslationTextComponent("gui.mxtune.button.disband");
    private static final ITextComponent LABEL_PIN = new TranslationTextComponent("gui.mxtune.label.pin");
    private static final ITextComponent LABEL_MODE = new TranslationTextComponent("gui.mxtune.label.mode");
    private static final ITextComponent PIN = new TranslationTextComponent(Group.Mode.Pin.getModeKey()).withStyle(TextFormatting.YELLOW);
    private static final ITextComponent OPEN = new TranslationTextComponent(Group.Mode.Open.getModeKey()).withStyle(TextFormatting.GREEN);
    private static final ITextComponent PIN_HELP01 = new TranslationTextComponent("gui.mxtune.button.new_pin.help01").withStyle(TextFormatting.RESET);
    private static final ITextComponent PIN_HELP02 = new TranslationTextComponent("gui.mxtune.button.new_pin.help02").withStyle(TextFormatting.GREEN);
    private static final ITextComponent PIN_HELP03 = new TranslationTextComponent("gui.mxtune.button.new_pin.help03").withStyle(TextFormatting.YELLOW);
    private static final ITextComponent MAKE_GROUP_HELP01 = new TranslationTextComponent("gui.mxtune.button.make_group.help01").withStyle(TextFormatting.RESET);
    private static final ITextComponent MAKE_GROUP_HELP02 = new TranslationTextComponent("gui.mxtune.button.make_group.help02").withStyle(TextFormatting.GREEN);
    private static final ITextComponent MAKE_GROUP_HELP03 = new TranslationTextComponent("gui.mxtune.button.make_group.help03").withStyle(TextFormatting.YELLOW);
    private static final ITextComponent DISBAND_HELP01 = new TranslationTextComponent("gui.mxtune.button.disband.help01").withStyle(TextFormatting.RESET);
    private static final ITextComponent DISBAND_HELP02 = new TranslationTextComponent("gui.mxtune.button.disband.help02").withStyle(TextFormatting.GREEN);
    private static final ITextComponent DISBAND_HELP03 = new TranslationTextComponent("gui.mxtune.button.disband.help03").withStyle(TextFormatting.YELLOW);

    private static final int PADDING = 4;
    private Group.Mode groupMode = Group.Mode.Pin;
    private final MXButton buttonNewPin = new MXButton(p -> newPin());
    private final MXButton buttonGroupMode = new MXButton(p -> modeToggle());
    private final MXButton buttonDone = new MXButton(p -> done());
    private final MXButton buttonDisband = new MXButton(p -> disband());
    private final MXButton buttonMakeGroup = new MXButton(p -> makeGroup());
    private final GuiHelpButton helpButton = new GuiHelpButton(p -> helpClicked());
    private final MXLabel groupDisplay = new MXLabel();
    private final MemberDisplay memberDisplayLeft;
    private final MemberDisplay memberDisplayRight;
    private int counter;
    private Group group = Group.EMPTY;
    private int groupId;
    private int lastHash;
    private final int lineHeight;
    private final int nameWidth;

    public GuiGroup()
    {
        super(StringTextComponent.EMPTY);
        lineHeight = mc().font.lineHeight + 6;
        nameWidth = mc().font.width("MMMMMMMMMMMM") + 4;
        memberDisplayLeft = new MemberDisplay(this, MemberDisplay.Split.M01_M08);
        memberDisplayRight = new MemberDisplay(this, MemberDisplay.Split.M09_M16);
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
        group = GroupClient.getGroup(player().getId());
        groupId = group.getGroupId();
        PacketDispatcher.sendToServer(new GroupCmdMessage("", Cmd.Pin, player().getId()));
        setMode(GroupClient.getGroupById(groupId).getMode());
        groupDisplay.setCentered(true);

        int groupDisplayWidth = getWidth(GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId));
        int leftSideWidth = memberDisplayLeft.getWidth();
        int middleWidth = groupDisplayWidth + 50;
        int rightSideWidth = memberDisplayRight.getWidth();
        int left = ((width - (middleWidth + leftSideWidth + rightSideWidth))) / 2;
        int top = (height - memberDisplayLeft.getHeight()) / 2;

        memberDisplayLeft.initMemberDisplay(left, top);
        int leftRight = memberDisplayLeft.getRight() + PADDING;

        groupDisplay.setLayout(leftRight, top, middleWidth, 20);
        ITextComponent msg = GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId);
        lastHash = msg.hashCode();
        groupDisplay.setLabelText(msg);

        memberDisplayRight.initMemberDisplay(groupDisplay.getRight() + PADDING, top);

        buttonMakeGroup.setLayout(leftRight, top, middleWidth, 20);
        buttonMakeGroup.setMessage(MAKE_GROUP);

        buttonNewPin.setLayout(leftRight, groupDisplay.getBottom(), middleWidth, 20);
        buttonNewPin.setMessage(getPinText("----"));

        buttonGroupMode.setLayout(leftRight, buttonNewPin.getBottom(), middleWidth, 20);

        buttonDone.setLayout(leftRight, memberDisplayLeft.getBottom() - 19 , middleWidth, 20);
        buttonDone.setMessage(DialogTexts.GUI_DONE);
        buttonDisband.setLayout(buttonDone.getLeft(), buttonDone.getTop() - 20, middleWidth - 20, 20);
        buttonDisband.setMessage(DISBAND);
        helpButton.setPosition(buttonDisband.getRight(), buttonDisband.getTop());

        addButton(buttonMakeGroup);
        addButton(buttonNewPin);
        addButton(buttonGroupMode);
        addButton(buttonDisband);
        addButton(buttonDone);
        addButton(helpButton);
        updateButtonState();
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
                buttonNewPin.setMessage(getPinText(GroupClient.getPrivatePin()));
                break;
            case Close:
                this.onClose();
                break;
        }
        updateButtonState();
    }

    private void reInit()
    {
        this.buttons.clear();
        this.children.clear();
        this.setFocused(null);
        this.init();
    }

    private void updateButtonState()
    {
        if (group.isValid())
        {
            buttonMakeGroup.active = false;
            buttonMakeGroup.visible = false;
            groupDisplay.setVisible(true);
        } else
        {
            buttonMakeGroup.active = true;
            buttonMakeGroup.visible = true;
            groupDisplay.setVisible(false);
        }
        buttonMakeGroup.addHooverText(true, MAKE_GROUP_HELP01);
        buttonMakeGroup.addHooverText(false, MAKE_GROUP_HELP02);
        buttonMakeGroup.addHooverText(false, MAKE_GROUP_HELP03);
        buttonNewPin.addHooverText(true, getPinText(group.isValid() ? GroupClient.getPrivatePin() : "----"));
        buttonNewPin.addHooverText(false, PIN_HELP02);
        buttonNewPin.addHooverText(false, PIN_HELP03);
        buttonGroupMode.addHooverText(true, getGroupModeName(groupMode));
        buttonGroupMode.addHooverText(false, new TranslationTextComponent(groupMode.getHelp02Key()).withStyle(TextFormatting.GREEN));
        buttonGroupMode.addHooverText(false, new TranslationTextComponent(groupMode.getHelp03Key()).withStyle(TextFormatting.YELLOW));
        buttonDisband.addHooverText(true, DISBAND_HELP01);
        buttonDisband.addHooverText(false, DISBAND_HELP02);
        buttonDisband.addHooverText(false, DISBAND_HELP03);
        helpButton.addHooverText(true, HELP_HELP01);
        helpButton.addHooverText(false, helpButton.isHelpEnabled() ? HELP_HELP02 : HELP_HELP03);
        buttons.stream().filter(b -> b instanceof IHooverText)
                .forEach(b -> ((IHooverText) b).setHooverTextOverride(helpButton.isHelpEnabled()));
    }

    private void promote(Button button)
    {
        MXButton mxButton = (MXButton) button;
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.Promote, mxButton.getIndex()));
    }

    private void remove(Button button)
    {
        MXButton mxButton = (MXButton) button;
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.Remove, mxButton.getIndex()));
    }

    private void modeToggle()
    {
        Cmd cmd = Cmd.Nil;
        switch (groupMode)
        {
            case Pin:
                setMode(Group.Mode.Open);
                cmd = Cmd.ModeOpen;
                break;
            case Open:
                setMode(Group.Mode.Pin);
                cmd = Cmd.ModePin;
                break;
        }
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, cmd, player().getId()));
        updateButtonState();
    }

    private void setMode(Group.Mode mode)
    {
        Group group = GroupClient.getGroupById(groupId);
        groupMode = mode;
        buttonGroupMode.setMessage(getGroupModeName(groupMode));

        buttonNewPin.active = mode.equals(Group.Mode.Pin) && player().getId() == group.getLeader();
        buttonGroupMode.active = player().getId() == group.getLeader();
        buttonDisband.active = player().getId() == group.getLeader();
    }

    private ITextComponent getGroupModeName(Group.Mode mode)
    {
        return LABEL_MODE.plainCopy()
                .append(" ").withStyle(TextFormatting.RESET)
                .append(mode.equals(Group.Mode.Pin) ? PIN : OPEN);
    }

    private ITextComponent getPinText(String text)
    {
        return LABEL_PIN.plainCopy().append(" ").withStyle(TextFormatting.RESET)
                .append(new StringTextComponent(text)
                                .withStyle(TextFormatting.GREEN));
    }

    private void makeGroup()
    {
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.CreateGroup, player().getId()));
    }

    private void newPin()
    {
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.NewPin, player().getId()));
    }

    private void disband()
    {
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.Disband, player().getId()));
        this.onClose();
    }

    private void done()
    {
        this.onClose();
    }

    private void helpClicked()
    {
        helpButton.setHelpEnabled(!helpButton.isHelpEnabled());
        updateButtonState();
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
        memberDisplayLeft.renderMemberDisplay(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        groupDisplay.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        memberDisplayRight.renderMemberDisplay(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        ModGuiHelper.drawHooveringHelp(pMatrixStack, this, buttons, pMouseX, pMouseY);
    }

    @Override
    public void tick()
    {
        if (counter++ % 20 == 0)
        {
            ITextComponent msg = GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId);
            groupDisplay.setLabelText(msg);
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

    private static int getWidth(String pText)
    {
        return mc().font.width(pText);
    }

    private static int getWidth(ITextComponent pText)
    {
        return mc().font.width(pText);
    }

    private static class MemberDisplay
    {
        private final GuiGroup parent;
        private final List<MemberInfo> memberButtons = new ArrayList<>();
        int xPos;
        int yPos;
        int padding = 0;
        final int maxWidth;
        final int maxHeight;
        final Split split;
        int indexSplit;

        MemberDisplay(GuiGroup parent, Split split)
        {
            this.parent = parent;
            this.split = split;
            maxHeight = (this.parent.getLineHeight() * 8);
            maxWidth = this.parent.getNameWidth() + 40;
        }

        private void initMemberDisplay(int posX, int posY)
        {
            this.xPos = posX;
            this.yPos = posY + 2;
            int y = this.yPos;
            indexSplit = 0;

            memberButtons.clear();
            Group group = GroupClient.getGroupById(parent.getGroupId());

            if (Split.M01_M08.equals(split))
                y = leaderFirst(posX + padding, y + padding, group.getLeader());
            for (Integer memberId : GroupClient.getGroupById(parent.getGroupId()).getMembers())
            {
                if (!GroupClient.isLeader(memberId) && split.inSplit(indexSplit))
                {
                    MemberInfo memberInfo = new MemberInfo(posX + padding, y + padding, parent, memberId, parent::promote, parent::remove);
                    parent.addButton(memberInfo.promote);
                    parent.addButton(memberInfo.remove);
                    memberButtons.add(memberInfo);
                    y += parent.getLineHeight();
                }
                if (!GroupClient.isLeader(memberId))
                    indexSplit++;
            }
        }

        public int getLeft()
        {
            return xPos;
        }

        public int getTop()
        {
            return yPos;
        }

        public int getRight()
        {
            return xPos + maxWidth + padding;
        }

        public int getBottom()
        {
            return yPos + maxHeight + padding;
        }

        public int getPadding()
        {
            return padding;
        }

        public int getHeight()
        {
            return maxHeight + padding;
        }

        public int getWidth()
        {
            return maxWidth + padding;
        }

        public void setPadding(int padding)
        {
            this.padding = padding;
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
            indexSplit++;
            return y + parent.getLineHeight();
        }

        private void renderMemberDisplay(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
        {
            fill(pMatrixStack, xPos + padding - 1, yPos + padding - 1, xPos + padding + maxWidth + 1, yPos + padding + maxHeight + 1, -6250336);
            fill(pMatrixStack, xPos + padding, yPos + padding, xPos + padding + maxWidth, yPos + padding + maxHeight, -16777216);
            memberButtons.forEach(p -> p.memberDraw(pMatrixStack, pMouseX, pMouseY, pPartialTicks));
        }

        enum Split
        {
            M01_M08(0, 7), M09_M16(7, 15);
            final int start;
            final int end;

            Split(int start, int end)
            {
                this.start = start;
                this.end = end;
            }
            boolean inSplit(int index)
            {
                return index >= start && index <= end;
            }
        }
    }

    private static class MemberInfo
    {
        static final ITextComponent PROMOTE = new StringTextComponent("▲").withStyle(TextFormatting.GREEN, TextFormatting.BOLD); // '▲' dec: 9650 hex: 25B2 BLACK UP-POINTING TRIANGLE
        static final ITextComponent REMOVE = new StringTextComponent("x").withStyle(TextFormatting.RED, TextFormatting.BOLD); // 'x'
        static final ITextComponent PROMOTE_HELP01 = new TranslationTextComponent("gui.mxtune.button.member_promote.help01").withStyle(TextFormatting.RESET).append(PROMOTE);
        static final ITextComponent PROMOTE_HELP02 = new TranslationTextComponent("gui.mxtune.button.member_promote.help02").withStyle(TextFormatting.GREEN);
        static final ITextComponent PROMOTE_HELP03 = new TranslationTextComponent("gui.mxtune.button.member_promote.help03").withStyle(TextFormatting.YELLOW);
        static final ITextComponent REMOVE_HELP01 = new TranslationTextComponent("gui.mxtune.button.member_remove.help01").withStyle(TextFormatting.RESET).append(REMOVE);
        static final ITextComponent REMOVE_HELP02 = new TranslationTextComponent("gui.mxtune.button.member_remove.help02").withStyle(TextFormatting.GREEN);
        static final ITextComponent REMOVE_HELP03 = new TranslationTextComponent("gui.mxtune.button.member_remove.help03").withStyle(TextFormatting.YELLOW);

        final int xPos;
        final int yPos;
        final MXButton promote;
        final MXButton remove;
        final int memberId;
        final ITextComponent name;

        MemberInfo(int xPos, int yPos, GuiGroup parent, int memberId, Button.IPressable pPromote, Button.IPressable pRemove)
        {
            this.xPos = xPos;
            this.yPos = yPos;
            int nameWidth = parent.nameWidth;
            int lineHeight = parent.lineHeight;
            this.memberId = memberId;
            this.name = player().level.getEntity(memberId) != null ? player().level.getEntity(memberId).getDisplayName() : StringTextComponent.EMPTY;
            promote = new MXButton(pPromote);
            promote.setMessage(MemberInfo.PROMOTE);
            promote.setLayout(xPos + nameWidth, yPos, 20, lineHeight);
            promote.setIndex(memberId);
            promote.addHooverText(true, PROMOTE_HELP01);
            promote.addHooverText(false, PROMOTE_HELP02);
            promote.addHooverText(false, PROMOTE_HELP03);
            promote.active = GroupClient.isLeader(player().getId());
            promote.visible = GroupClient.isLeader(player().getId());

            remove = new MXButton(pRemove);
            remove.setMessage(MemberInfo.REMOVE);
            remove.setLayout(promote.x + 20, yPos, 20, lineHeight);
            remove.setIndex(memberId);
            remove.addHooverText(true, REMOVE_HELP01);
            remove.addHooverText(false, REMOVE_HELP02);
            remove.addHooverText(false, REMOVE_HELP03);
            remove.active = GroupClient.isLeader(player().getId()) || memberId == player().getId();
            remove.visible = GroupClient.isLeader(player().getId()) || memberId == player().getId();
        }

        void memberDraw(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
        {
            mc().font.draw(pMatrixStack, name, (float)xPos + 2, (float)yPos + 2, GroupClient.isLeader(memberId) ? TextColorFg.YELLOW: TextColorFg.WHITE);
        }
    }
}
