package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.gui.MXScreen;
import aeronicamc.mods.mxtune.gui.TextColorFg;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
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
    private final List<MemberButtons> memberButtons = new ArrayList<>();
    private int counter;
    private final int groupId;
    private int lastHash;
    public GuiGroup()
    {
        super(StringTextComponent.EMPTY);
        groupId = GroupClient.getGroup(player().getId()).getGroupId();
    }

    @Override
    protected void init()
    {
        super.init();
        GroupClient.setCallback(this);
        PacketDispatcher.sendToServer(new GetGroupPinMessage());
        initMemberDisplay(10, 10);

        int groupDisplayWidth = mc().font.width(GuiPin.getGroupLeaderInfo(groupDisplay, player(), groupId));
        int pinDisplayWidth = mc().font.width("0000");
        groupDisplay.setLayout(width/2 - groupDisplayWidth /2, 10, groupDisplayWidth + 8, 20);
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
            case Member:
                this.init();
                break;
            case Pin:
                pinDisplay.setValue(GroupClient.getPrivatePin());
                break;
        }
        System.out.printf("callback: %s\n", type);
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
        memberButtons.forEach(p -> p.memberDraw(pMatrixStack, pMouseX, pMouseY, pPartialTicks));
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

    private void initMemberDisplay(int posX, int posY)
    {
        int y = posY;
        memberButtons.clear();
        for (Integer memberId : GroupClient.getGroupById(groupId).getMembers())
        {
            if (!GroupClient.isLeader(memberId))
            {
                MemberButtons memberButton = new MemberButtons(posX, y, memberId, this::promote, this::remove);
                addButton(memberButton.promote);
                addButton(memberButton.remove);
                memberButtons.add(memberButton);
                y += Member.height;
            }
        }
        System.out.printf("memberButtons size: %d\n", memberButtons.size());
    }

    private static class MemberButtons
    {
        static final ITextComponent PROMOTE = new StringTextComponent("^").withStyle(TextFormatting.GREEN, TextFormatting.BOLD);
        static final ITextComponent REMOVE = new StringTextComponent("X").withStyle(TextFormatting.RED, TextFormatting.BOLD);
        final int xPos;
        final int yPos;
        final MXButton promote;
        final MXButton remove;
        final Member member;

        MemberButtons(int xPos, int yPos, int memberId, Button.IPressable pPromote, Button.IPressable pRemove)
        {
            this.xPos = xPos;
            this.yPos = yPos;
            member = new Member(memberId, Objects.requireNonNull(player().level.getEntity(memberId)).getDisplayName());
            promote = new MXButton(pPromote);
            promote.setMessage(MemberButtons.PROMOTE);
            promote.setLayout(xPos + Member.width, yPos - 2, 20, Member.height);
            promote.setIndex(memberId);
            promote.active = GroupClient.isLeader(player().getId());

            remove = new MXButton(pRemove);
            remove.setMessage(MemberButtons.REMOVE);
            remove.setLayout(promote.x + 20, yPos - 2, 20, Member.height);
            remove.setIndex(memberId);
            remove.active = GroupClient.isLeader(player().getId()) || memberId == player().getId();
        }

        void memberDraw(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
        {
            mc().font.draw(pMatrixStack, member.getName(), xPos, yPos, GroupClient.isLeader(member.getMemberId()) ? TextColorFg.YELLOW: TextColorFg.WHITE);
        }
    }

    private static class Member
    {
        int memberId;
        ITextComponent name;

        Member(int memberId, ITextComponent name)
        {
            this.memberId = memberId;
            this.name = name;
        }

        static int width = mc().font.width("MMMMMMMMMMMM") + 4;
        static int height = mc().font.lineHeight + 4;

        public int getMemberId()
        {
            return memberId;
        }

        public ITextComponent getName()
        {
            return name;
        }
    }
}
