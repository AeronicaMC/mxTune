package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.util.SoundFontProxy;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;

public class SoundFontProxyWidget extends ExtendedList<SoundFontProxyWidget.Entry>
{
    private int rowWidth;
    private int scrollBarPosition;

    public SoundFontProxyWidget(Minecraft minecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight, int pLeft)
    {
        super(minecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
        super.setLeftPos(pLeft);
        super.setRenderTopAndBottom(false);
        super.setRenderSelection(true);
        super.setRenderBackground(true);
    }

    @Override
    protected int getScrollbarPosition()
    {
        return x0 + width - 5;
    }

    @Override
    public int getRowWidth()
    {
        return rowWidth - 1;
    }

    public void setRowWidth(int rowWidth)
    {
        this.rowWidth = rowWidth;
    }

    public SoundFontProxyWidget init()
    {
        for (SoundFontProxy soundFontProxy: SoundFontProxyManager.soundFontProxyMapById.values())
        {
            SoundFontProxyWidget.Entry entry = new  SoundFontProxyWidget.Entry(soundFontProxy);
            super.addEntry(entry);
            assert minecraft.player != null;
            if (soundFontProxy.index == minecraft.player.getMainHandItem().getMaxDamage())
            {
                super.setSelected(entry);
            }
        }
        if (super.getSelected() != null)
        {
            super.centerScrollOn(super.getSelected());
        }
        return this;
    }

    @Override
    protected void renderBackground(MatrixStack pMatrixStack)
    {
        super.renderBackground(pMatrixStack);
    }

    public class Entry extends ExtendedList.AbstractListEntry<SoundFontProxyWidget.Entry>
    {
        SoundFontProxy soundFontProxy;

        public Entry(SoundFontProxy soundFontProxy)
        {
            this.soundFontProxy = soundFontProxy;
        }

        @Override
        public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks)
        {
            if (pIsMouseOver)
            {
                fill(pMatrixStack, pLeft - 2, pTop - 2, pLeft - 5 + width, pTop + itemHeight - 1, 0xA0A0A0A0);
            }

            ITextComponent translated = new TranslationTextComponent(String.format("item.mxtune.%s", soundFontProxy.id));
            ITextProperties trimmed = minecraft.font.substrByWidth(translated, pWidth - 6);
            minecraft.font.drawShadow(pMatrixStack, trimmed.getString(), (float) (pLeft), (float) (pTop + 1), 16777215, true);
        }
    }
}


