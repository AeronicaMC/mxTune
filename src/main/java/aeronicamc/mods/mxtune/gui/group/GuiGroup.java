package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.gui.MXScreen;
import aeronicamc.mods.mxtune.gui.ModGuiHelper;
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
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;

import static aeronicamc.mods.mxtune.gui.ModGuiHelper.*;
import static aeronicamc.mods.mxtune.network.messages.GroupCmdMessage.Cmd;

public class GuiGroup extends MXScreen implements IGroupClientChangedCallback
{
    private static final ITextComponent MAKE_GROUP = new TranslationTextComponent("gui.mxtune.button.make_group");
    private static final ITextComponent DISBAND = new TranslationTextComponent("gui.mxtune.button.disband");
    private static final ITextComponent LABEL_PIN = new TranslationTextComponent("gui.mxtune.label.pin");
    private static final ITextComponent LABEL_MODE = new TranslationTextComponent("gui.mxtune.label.mode");
    private static final ITextComponent PIN = new TranslationTextComponent(Group.Mode.PIN.getModeKey()).withStyle(TextFormatting.YELLOW);
    private static final ITextComponent OPEN = new TranslationTextComponent(Group.Mode.OPEN.getModeKey()).withStyle(TextFormatting.GREEN);
    private static final ITextComponent PIN_HELP01 = new TranslationTextComponent("gui.mxtune.button.new_pin.help01").withStyle(TextFormatting.GREEN);
    private static final ITextComponent PIN_HELP02 = new TranslationTextComponent("gui.mxtune.button.new_pin.help02").withStyle(TextFormatting.YELLOW);
    private static final ITextComponent MAKE_GROUP_HELP01 = new TranslationTextComponent("gui.mxtune.button.make_group.help01").withStyle(TextFormatting.RESET);
    private static final ITextComponent MAKE_GROUP_HELP02 = new TranslationTextComponent("gui.mxtune.button.make_group.help02").withStyle(TextFormatting.GREEN);
    private static final ITextComponent MAKE_GROUP_HELP03 = new TranslationTextComponent("gui.mxtune.button.make_group.help03").withStyle(TextFormatting.YELLOW);
    private static final ITextComponent DISBAND_HELP01 = new TranslationTextComponent("gui.mxtune.button.disband.help01").withStyle(TextFormatting.RESET);
    private static final ITextComponent DISBAND_HELP02 = new TranslationTextComponent("gui.mxtune.button.disband.help02").withStyle(TextFormatting.GREEN);
    private static final ITextComponent DISBAND_HELP03 = new TranslationTextComponent("gui.mxtune.button.disband.help03").withStyle(TextFormatting.YELLOW);

    private static final int PADDING = 4;
    Group.Mode groupMode = Group.Mode.PIN;
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
    final int lineHeight;
    final int nameWidth;

    public GuiGroup()
    {
        super(StringTextComponent.EMPTY);
        lineHeight = getFont().lineHeight + 6;
        nameWidth = getFont().width("MMMMMMMMMMMM") + 4;
        memberDisplayLeft = new MemberDisplay(this, this, MemberDisplay.Split.M01_M08);
        memberDisplayRight = new MemberDisplay(this, this, MemberDisplay.Split.M09_M16);
        GroupClient.setCallback(this);
    }

    int getGroupId()
    {
        return this.groupId;
    }

    int getLineHeight()
    {
        return this.lineHeight;
    }

    int getNameWidth()
    {
        return this.nameWidth;
    }

    @Override
    protected void init()
    {
        super.init();
        group = GroupClient.getGroup(getPlayer().getId());
        groupId = group.getGroupId();
        PacketDispatcher.sendToServer(new GroupCmdMessage("", Cmd.PIN, getPlayer().getId()));
        setMode(GroupClient.getGroupById(groupId).getMode());
        groupDisplay.setCentered(true);

        int groupDisplayWidth = getWidth(GuiPin.getGroupLeaderInfo(groupDisplay, getPlayer(), groupId));
        int leftSideWidth = memberDisplayLeft.getWidth();
        int middleWidth = groupDisplayWidth + 50;
        int rightSideWidth = memberDisplayRight.getWidth();
        int left = (width - (middleWidth + leftSideWidth + rightSideWidth)) / 2;
        int top = (height - memberDisplayLeft.getHeight()) / 2;

        memberDisplayLeft.initMemberDisplay(left, top);
        int leftRight = memberDisplayLeft.getRight() + PADDING;

        groupDisplay.setLayout(leftRight, top, middleWidth, 20);
        ITextComponent msg = GuiPin.getGroupLeaderInfo(groupDisplay, getPlayer(), groupId);
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
        buttonNewPin.addHooverText(false, PIN_HELP01);
        buttonNewPin.addHooverText(false, PIN_HELP02);
        buttonGroupMode.addHooverText(true, getGroupModeName(groupMode));
        buttonGroupMode.addHooverText(false, new TranslationTextComponent(groupMode.getHelp01Key()).withStyle(TextFormatting.GREEN));
        buttonGroupMode.addHooverText(false, new TranslationTextComponent(groupMode.getHelp02Key()).withStyle(TextFormatting.YELLOW));
        buttonDisband.addHooverText(true, DISBAND_HELP01);
        buttonDisband.addHooverText(false, DISBAND_HELP02);
        buttonDisband.addHooverText(false, DISBAND_HELP03);
        helpButton.addHooverText(true, HELP_HELP01);
        helpButton.addHooverText(false, helpButton.isHelpEnabled() ? HELP_HELP02 : HELP_HELP03);
        buttons.stream().filter(IHooverText.class::isInstance)
                .forEach(b -> ((IHooverText) b).setHooverTextOverride(helpButton.isHelpEnabled()));
    }

    void promote(Button button)
    {
        MXButton mxButton = (MXButton) button;
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.PROMOTE, mxButton.getIndex()));
    }

    void remove(Button button)
    {
        MXButton mxButton = (MXButton) button;
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.REMOVE, mxButton.getIndex()));
    }

    void modeToggle()
    {
        Cmd cmd = Cmd.NIL;
        if (Objects.requireNonNull(groupMode) == Group.Mode.PIN) {
            setMode(Group.Mode.OPEN);
            cmd = Cmd.MODE_OPEN;
        } else if (groupMode == Group.Mode.OPEN) {
            setMode(Group.Mode.PIN);
            cmd = Cmd.MODE_PIN;
        }
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, cmd, getPlayer().getId()));
        updateButtonState();
    }

    private void setMode(Group.Mode mode)
    {
        Group groupLocal = GroupClient.getGroupById(groupId);
        groupMode = mode;
        buttonGroupMode.setMessage(getGroupModeName(groupMode));

        buttonNewPin.active = mode.equals(Group.Mode.PIN) && getPlayer().getId() == groupLocal.getLeader();
        buttonGroupMode.active = getPlayer().getId() == groupLocal.getLeader();
        buttonDisband.active = getPlayer().getId() == groupLocal.getLeader();
    }

    private ITextComponent getGroupModeName(Group.Mode mode)
    {
        return LABEL_MODE.plainCopy()
                .append(" ").withStyle(TextFormatting.RESET)
                .append(mode.equals(Group.Mode.PIN) ? PIN : OPEN);
    }

    private ITextComponent getPinText(String text)
    {
        return LABEL_PIN.plainCopy().append(" ").withStyle(TextFormatting.RESET)
                .append(new StringTextComponent(text)
                                .withStyle(TextFormatting.GREEN));
    }

    private void makeGroup()
    {
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.CREATE_GROUP, getPlayer().getId()));
    }

    private void newPin()
    {
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.NEW_PIN, getPlayer().getId()));
    }

    private void disband()
    {
        PacketDispatcher.sendToServer(new GroupCmdMessage(null, Cmd.DISBAND, getPlayer().getId()));
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
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.fillGradient(pMatrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        memberDisplayLeft.renderMemberDisplay(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        groupDisplay.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        memberDisplayRight.renderMemberDisplay(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        ModGuiHelper.drawHooveringHelp(pMatrixStack, this, children, pMouseX, pMouseY);
    }

    @Override
    public void tick()
    {
        if (counter++ % 20 == 0)
        {
            ITextComponent msg = GuiPin.getGroupLeaderInfo(groupDisplay, getPlayer(), groupId);
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

    private int getWidth(ITextComponent pText)
    {
        return getFont().width(pText);
    }
}
