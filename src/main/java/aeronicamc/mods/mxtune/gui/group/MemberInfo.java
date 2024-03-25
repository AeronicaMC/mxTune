package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.gui.TextColorFg;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.managers.GroupClient;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class MemberInfo {
    static final ITextComponent PROMOTE = new StringTextComponent("▲").withStyle(TextFormatting.GREEN, TextFormatting.BOLD); // '▲' dec: 9650 hex: 25B2 BLACK UP-POINTING TRIANGLE
    static final ITextComponent REMOVE = new StringTextComponent("x").withStyle(TextFormatting.RED, TextFormatting.BOLD); // 'x'
    static final ITextComponent PROMOTE_HELP01 = new TranslationTextComponent("gui.mxtune.button.member_promote.help01").withStyle(TextFormatting.RESET).append(PROMOTE);
    static final ITextComponent PROMOTE_HELP02 = new TranslationTextComponent("gui.mxtune.button.member_promote.help02").withStyle(TextFormatting.GREEN);
    static final ITextComponent PROMOTE_HELP03 = new TranslationTextComponent("gui.mxtune.button.member_promote.help03").withStyle(TextFormatting.YELLOW);
    static final ITextComponent REMOVE_HELP01 = new TranslationTextComponent("gui.mxtune.button.member_remove.help01").withStyle(TextFormatting.RESET).append(REMOVE);
    static final ITextComponent REMOVE_HELP02 = new TranslationTextComponent("gui.mxtune.button.member_remove.help02").withStyle(TextFormatting.GREEN);
    static final ITextComponent REMOVE_HELP03 = new TranslationTextComponent("gui.mxtune.button.member_remove.help03").withStyle(TextFormatting.YELLOW);

    private final GuiGroup parent;
    private final int xPosMember;
    private final int yPosMember;
    private final MXButton buttonPromote;
    private final MXButton buttonRemove;
    private final int memberId;
    private final ITextComponent name;

    MemberInfo(int xPosMember, int yPosMember, GuiGroup parent, int memberId, Button.IPressable pPromote, Button.IPressable pRemove) {
        this.xPosMember = xPosMember;
        this.yPosMember = yPosMember;
        this.parent = parent;
        int nameWidth = parent.nameWidth;
        int lineHeight = parent.lineHeight;
        this.memberId = memberId;
        Entity entity = parent.getPlayer().level.getEntity(memberId);
        this.name = entity != null ? entity.getDisplayName() : StringTextComponent.EMPTY;
        buttonPromote = new MXButton(pPromote);
        buttonPromote.setMessage(MemberInfo.PROMOTE);
        buttonPromote.setLayout(xPosMember + nameWidth, yPosMember, 20, lineHeight);
        buttonPromote.setIndex(memberId);
        buttonPromote.addHooverText(true, PROMOTE_HELP01);
        buttonPromote.addHooverText(false, PROMOTE_HELP02);
        buttonPromote.addHooverText(false, PROMOTE_HELP03);
        buttonPromote.active = GroupClient.isLeader(parent.getPlayer().getId());
        buttonPromote.visible = GroupClient.isLeader(parent.getPlayer().getId());

        buttonRemove = new MXButton(pRemove);
        buttonRemove.setMessage(MemberInfo.REMOVE);
        buttonRemove.setLayout(buttonPromote.x + 20, yPosMember, 20, lineHeight);
        buttonRemove.setIndex(memberId);
        buttonRemove.addHooverText(true, REMOVE_HELP01);
        buttonRemove.addHooverText(false, REMOVE_HELP02);
        buttonRemove.addHooverText(false, REMOVE_HELP03);
        buttonRemove.active = GroupClient.isLeader(parent.getPlayer().getId()) || memberId == parent.getPlayer().getId();
        buttonRemove.visible = GroupClient.isLeader(parent.getPlayer().getId()) || memberId == parent.getPlayer().getId();
    }

    @SuppressWarnings("unused")
    void memberDraw(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        parent.getFont().draw(pMatrixStack, name, (float) xPosMember + 2, (float) yPosMember + 2, GroupClient.isLeader(memberId) ? TextColorFg.YELLOW : TextColorFg.WHITE);
    }

    MXButton getButtonPromote() {
        return buttonPromote;
    }

    MXButton getButtonRemove() {
        return buttonRemove;
    }
}
