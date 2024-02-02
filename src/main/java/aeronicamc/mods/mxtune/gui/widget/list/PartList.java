package aeronicamc.mods.mxtune.gui.widget.list;

import aeronicamc.mods.mxtune.gui.TextColorFg;
import aeronicamc.mods.mxtune.mxt.MXTunePart;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class PartList extends MXExtendedList<PartList.Entry>
{
    public PartList()
    {
        super();
    }

    public PartList(Minecraft minecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight, int pLeft, BiConsumer<Entry, Boolean> selectCallback)
    {
        super(minecraft, pWidth, pHeight, pY0, pY1, pItemHeight, pLeft, selectCallback);
    }

    public int add(MXTunePart part)
    {
        return addEntry(new Entry(part));
    }

    public void addAll(@Nullable List<MXTunePart> parts)
    {
        if (parts != null)
            parts.forEach(part -> addEntry(new Entry(part)));
        super.setScrollAmount(1);
    }

    public void clear()
    {
        clearEntries();
    }

    public List<PartList.Entry> getEntries()
    {
        return super.children();
    }

    @Override
    public void setCallBack(BiConsumer<Entry, Boolean> selectCallback)
    {
        this.selectCallback = selectCallback;
    }

    public class Entry extends MXExtendedList.AbstractListEntry<Entry>
    {
        protected MXTunePart part;

        public Entry(MXTunePart part)
        {
            this.part = part;
        }

        public MXTunePart getPart()
        {
            return part;
        }

        @Override
        public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks)
        {
            if (pIsMouseOver && isActive())
            {
                fill(pMatrixStack, pLeft - 2, pTop - 2, pLeft - 5 + width, pTop + itemHeight - 1, 0xA0A0A0A0);
            }

            ITextComponent translated = new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(part.getInstrumentId()));
            ITextProperties trimmed = minecraft.font.substrByWidth(translated, pWidth - 6);
            minecraft.font.drawShadow(pMatrixStack, trimmed.getString(), pLeft, pTop + 1F, getSelected() == this ? TextColorFg.WHITE : TextColorFg.GRAY, true);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
        {
            if (isMouseOver(pMouseX, pMouseY) && isActive() && selectCallback != null){
                changeFocus(true);
                setFocused(this);
                PartList.this.setSelected(this);
                selectCallback.accept(this, doubleClicked());
                minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            return false;
        }
    }
}


