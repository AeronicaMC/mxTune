package aeronicamc.mods.mxtune.gui.widget.list;

import aeronicamc.mods.mxtune.gui.TextColorFg;
import aeronicamc.mods.mxtune.managers.GroupClient;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public class GroupMemberList extends MXExtendedList<GroupMemberList.Entry>
{
    private int suggestedWidth;
    private Set<Integer> toRemove = new HashSet<>(16);
    private Set<Integer> toAdd = new HashSet<>(16);

    public GroupMemberList()
    {
        super();
    }

    public GroupMemberList(Minecraft minecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight, int pLeft, BiConsumer<Entry, Boolean> selectCallback)
    {
        super(minecraft, pWidth, pHeight, pY0, pY1, pItemHeight, pLeft, selectCallback);
    }

    public GroupMemberList init(int groupId)
    {
        super.children().clear();
        for (int member : GroupClient.getGroupById(groupId).getMembers())
        {
            LivingEntity entity = (LivingEntity) Objects.requireNonNull(minecraft.player).level.getEntity(member);
            if (entity != null)
            {
                suggestedWidth = calculateWidth(suggestedWidth, entity.getDisplayName().getString());
                GroupMemberList.Entry entry = new GroupMemberList.Entry(new Member(entity.getId(), entity.getDisplayName()));
                super.addEntry(entry);
                if (GroupClient.isLeader(entry.getId()))
                {
//                    super.setSelected(entry);
//                    super.ensureVisible(entry);
                }
            }
        }
        if (super.getSelected() == null)
        {
            //super.setSelected(children().get(0));
        }
        //super.centerScrollOn(super.getSelected());
        suggestedWidth += 10; // Roughly Account for scrollbar
        return this;
    }

    private int calculateWidth(int lastWidth, String id)
    {
        ITextComponent name = new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(id));
        return Math.max(lastWidth, minecraft.font.width(name));
    }

    public int getSuggestedWidth()
    {
        return suggestedWidth;
    }

    @Override
    public void setCallBack(BiConsumer<Entry, Boolean> selectCallback)
    {
        this.selectCallback = selectCallback;
    }

    public class Entry extends MXExtendedList.AbstractListEntry<GroupMemberList.Entry>
    {
        protected Member member;

        public Entry(Member member)
        {
            this.member = member;
        }

        public Member getMember()
        {
            return member;
        }

        public int getId()
        {
            return member.getMemberId();
        }

        @Override
        public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks)
        {
            if (pIsMouseOver && isActive())
            {
                fill(pMatrixStack, pLeft - 2, pTop - 2, pLeft - 5 + width, pTop + itemHeight - 1, 0xA0A0A0A0);
            }

            ITextComponent name = member.getName();
            ITextProperties trimmed = minecraft.font.substrByWidth(name, pWidth - 6);
            int color = GroupClient.isLeader(member.getMemberId()) ? TextColorFg.YELLOW : TextColorFg.WHITE;
            minecraft.font.drawShadow(pMatrixStack, trimmed.getString(), pLeft, (pTop + 1), color, true);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
        {
            if (isMouseOver(pMouseX, pMouseY) && isActive() && selectCallback != null){
                changeFocus(true);
                setFocused(this);
                GroupMemberList.this.setSelected(this);
                selectCallback.accept(this, doubleClicked());
                minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            return false;
        }
    }

    public static class Member
    {
        protected int memberId;
        protected ITextComponent name;
        private Member() { /* NOP */ }

        public Member(int memberId, ITextComponent name)
        {
            this.memberId = memberId;
            this.name = name;
        }

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


