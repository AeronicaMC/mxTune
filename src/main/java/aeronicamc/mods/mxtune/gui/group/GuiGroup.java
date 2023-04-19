package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.gui.MXScreen;
import aeronicamc.mods.mxtune.gui.TextColorFg;
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

import static aeronicamc.mods.mxtune.network.messages.GroupCmdMessage.Cmd;

public class GuiGroup extends MXScreen implements IGroupClientChangedCallback
{
    private static final ITextComponent DISBAND = new TranslationTextComponent("gui.mxtune.button.disband");
    private static final ITextComponent LABEL_PIN = new TranslationTextComponent("gui.mxtune.label.pin");
    private static final ITextComponent LABEL_MODE = new TranslationTextComponent("gui.mxtune.label.mode");
    private static final ITextComponent PIN = new TranslationTextComponent(Group.Mode.Pin.getModeKey()).withStyle(TextFormatting.YELLOW);
    private static final ITextComponent OPEN = new TranslationTextComponent(Group.Mode.Open.getModeKey()).withStyle(TextFormatting.GREEN);

    private static final int PADDING = 4;
    private Group.Mode groupMode = Group.Mode.Pin;
    private final MXButton buttonNewPin = new MXButton(p -> newPin());
    private final MXButton buttonGroupMode = new MXButton(p -> modeToggle());
    private final MXButton buttonDone = new MXButton(p -> done());
    private final MXButton buttonDisband = new MXButton(p -> disband());
    private final MXLabel groupDisplay = new MXLabel();
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
        PacketDispatcher.sendToServer(new GroupCmdMessage("", Cmd.Pin, player().getId()));
        setMode(GroupClient.getGroupById(groupId).getMode());

        groupDisplay.setCentered(true);

        int groupDisplayWidth = getWidth(GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId));
        int leftSideWidth = memberDisplay.getWidth();
        int rightSideWidth = groupDisplayWidth + 50;
        int left = ((width / 2) - ((rightSideWidth + leftSideWidth) / 3));
        int top = (height / 2) - (memberDisplay.getHeight() / 2);

        memberDisplay.initMemberDisplay(left, top);
        int leftRight = memberDisplay.getRight() + PADDING;

        groupDisplay.setLayout(leftRight, top, rightSideWidth, 20);
        ITextComponent msg = GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId);
        lastHash = msg.hashCode();
        groupDisplay.setLabelText(msg);

        buttonNewPin.setLayout(leftRight, groupDisplay.getBottom(), rightSideWidth, 20);
        buttonGroupMode.setLayout(leftRight, buttonNewPin.getBottom(), rightSideWidth, 20);

        buttonDone.setLayout(leftRight,memberDisplay.getBottom() - 19 ,rightSideWidth,20);
        buttonDone.setMessage(DialogTexts.GUI_DONE);
        buttonDisband.setLayout(buttonDone.getLeft(), buttonDone.getTop() - 20, rightSideWidth, 20);
        buttonDisband.setMessage(DISBAND);

        addButton(buttonNewPin);
        addButton(buttonGroupMode);
        addButton(buttonDisband);
        addButton(buttonDone);
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
                buttonNewPin.setMessage(
                        LABEL_PIN.plainCopy().append(" ").withStyle(TextFormatting.WHITE)
                                .append(new StringTextComponent(GroupClient.getPrivatePin())
                                                .withStyle(TextFormatting.GREEN)));
                break;
            case Close:
                this.onClose();
                break;
        }

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
    }

    private void setMode(Group.Mode mode)
    {
        Group group = GroupClient.getGroupById(groupId);
        groupMode = mode;
        buttonGroupMode.setMessage(
                LABEL_MODE.plainCopy()
                        .append(" ").withStyle(TextFormatting.WHITE)
                        .append(mode.equals(Group.Mode.Pin) ? PIN : OPEN));

        buttonNewPin.active = mode.equals(Group.Mode.Pin) && player().getId() == group.getLeader();
        buttonGroupMode.active = player().getId() == group.getLeader();
        buttonDisband.active = player().getId() == group.getLeader();
    }

    private void newPin()
    {
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.NewPin, player().getId()));
    }

    private void disband()
    {
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.Disband, player().getId()));
        //this.onClose();
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
        memberDisplay.renderMemberDisplay(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
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

            y = leaderFirst(posX + padding, y + padding, group.getLeader());
            for (Integer memberId : GroupClient.getGroupById(parent.getGroupId()).getMembers())
            {
                if (!GroupClient.isLeader(memberId))
                {
                    MemberInfo memberInfo = new MemberInfo(posX + padding, y + padding, parent, memberId, parent::promote, parent::remove);
                    parent.addButton(memberInfo.promote);
                    parent.addButton(memberInfo.remove);
                    memberButtons.add(memberInfo);
                    y += parent.getLineHeight();
                }
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
            return maxHeight + padding;
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
            return y + parent.getLineHeight();
        }

        private void renderMemberDisplay(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
        {
            fill(pMatrixStack, xPos + padding - 1, yPos + padding - 1, xPos + padding + maxWidth + 1, yPos + padding + maxHeight + 1, -6250336);
            fill(pMatrixStack, xPos + padding, yPos + padding, xPos + padding + maxWidth, yPos + padding + maxHeight, -16777216);
            memberButtons.forEach(p -> p.memberDraw(pMatrixStack, pMouseX, pMouseY, pPartialTicks));
        }
    }

    private static class MemberInfo
    {
        static final ITextComponent PROMOTE = new StringTextComponent("▲").withStyle(TextFormatting.GREEN, TextFormatting.BOLD); // '▲' dec: 9650 hex: 25B2 BLACK UP-POINTING TRIANGLE
        static final ITextComponent REMOVE = new StringTextComponent("x").withStyle(TextFormatting.RED, TextFormatting.BOLD); // 'x'
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
            this.name = player().level.getEntity(memberId) != null ? player().level.getEntity(memberId).getDisplayName() : new StringTextComponent(String.format("< %d >", memberId));
            promote = new MXButton(pPromote);
            promote.setMessage(MemberInfo.PROMOTE);
            promote.setLayout(xPos + nameWidth, yPos, 20, lineHeight);
            promote.setIndex(memberId);
            promote.active = GroupClient.isLeader(player().getId());

            remove = new MXButton(pRemove);
            remove.setMessage(MemberInfo.REMOVE);
            remove.setLayout(promote.x + 20, yPos, 20, lineHeight);
            remove.setIndex(memberId);
            remove.active = GroupClient.isLeader(player().getId()) || memberId == player().getId();
        }

        void memberDraw(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
        {
            mc().font.draw(pMatrixStack, name, (float)xPos + 2, (float)yPos + 2, GroupClient.isLeader(memberId) ? TextColorFg.YELLOW: TextColorFg.WHITE);
        }
    }
}
