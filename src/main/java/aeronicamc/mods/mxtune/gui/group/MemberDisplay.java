package aeronicamc.mods.mxtune.gui.group;

import aeronicamc.mods.mxtune.managers.Group;
import aeronicamc.mods.mxtune.managers.GroupClient;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;

import java.util.ArrayList;
import java.util.List;

public class MemberDisplay {
    private final GuiGroup guiGroup;
    private final GuiGroup parent;
    private final List<MemberInfo> memberButtons = new ArrayList<>();
    private int xPos;
    private int yPos;
    private int padding = 0;
    private final int maxWidth;
    private final int maxHeight;
    private final Split split;
    private int indexSplit;

    MemberDisplay(GuiGroup guiGroup, GuiGroup parent, Split split) {
        this.guiGroup = guiGroup;
        this.parent = parent;
        this.split = split;
        maxHeight = (this.parent.getLineHeight() * 8);
        maxWidth = this.parent.getNameWidth() + 40;
    }

    void initMemberDisplay(int posX, int posY) {
        this.xPos = posX;
        this.yPos = posY + 2;
        int y = this.yPos;
        indexSplit = 0;

        memberButtons.clear();
        Group group = GroupClient.getGroupById(parent.getGroupId());

        if (Split.M01_M08.equals(split))
            y = leaderFirst(posX + padding, y + padding, group.getLeader());
        for (Integer memberId : GroupClient.getGroupById(parent.getGroupId()).getMembers()) {
            if (!GroupClient.isLeader(memberId) && split.inSplit(indexSplit)) {
                MemberInfo memberInfo = new MemberInfo(posX + padding, y + padding, parent, memberId, parent::promote, parent::remove);
                parent.addButton(memberInfo.getButtonPromote());
                parent.addButton(memberInfo.getButtonRemove());
                memberButtons.add(memberInfo);
                y += parent.getLineHeight();
            }
            if (!GroupClient.isLeader(memberId))
                indexSplit++;
        }
    }

    public int getLeft() {
        return xPos;
    }

    public int getTop() {
        return yPos;
    }

    public int getRight() {
        return xPos + maxWidth + padding;
    }

    @SuppressWarnings("unused")
    public int getBottom() {
        return yPos + maxHeight + padding;
    }

    @SuppressWarnings("unused")
    public int getPadding() {
        return padding;
    }

    public int getHeight() {
        return maxHeight + padding;
    }

    public int getWidth() {
        return maxWidth + padding;
    }

    @SuppressWarnings("unused")
    public void setPadding(int padding) {
        this.padding = padding;
    }

    int leaderFirst(int posX, int y, int memberId) {
        MemberInfo memberInfo = new MemberInfo(posX, y, parent, memberId, parent::promote, parent::remove);
        memberInfo.getButtonPromote().active = false;
        memberInfo.getButtonPromote().visible = false;
        memberInfo.getButtonRemove().active = guiGroup.getPlayer().getId() == memberId;
        memberInfo.getButtonRemove().visible = guiGroup.getPlayer().getId() == memberId;
        parent.addButton(memberInfo.getButtonPromote());
        parent.addButton(memberInfo.getButtonRemove());
        memberButtons.add(memberInfo);
        indexSplit++;
        return y + parent.getLineHeight();
    }

    void renderMemberDisplay(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        AbstractGui.fill(pMatrixStack, xPos + padding - 1, yPos + padding - 1, xPos + padding + maxWidth + 1, yPos + padding + maxHeight + 1, -6250336);
        AbstractGui.fill(pMatrixStack, xPos + padding, yPos + padding, xPos + padding + maxWidth, yPos + padding + maxHeight, -16777216);
        memberButtons.forEach(p -> p.memberDraw(pMatrixStack, pMouseX, pMouseY, pPartialTicks));
    }

    enum Split {
        M01_M08(0, 7), M09_M16(7, 15);
        final int start;
        final int end;

        Split(int start, int end) {
            this.start = start;
            this.end = end;
        }

        boolean inSplit(int index) {
            return index >= start && index <= end;
        }
    }
}
