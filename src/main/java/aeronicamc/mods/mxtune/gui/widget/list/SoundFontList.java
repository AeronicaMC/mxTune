package aeronicamc.mods.mxtune.gui.widget.list;

import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SoundFontProxy;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;
import java.util.function.Consumer;

public class SoundFontList extends MXExtendedList<SoundFontList.Entry>
{
    private int suggestedWidth;

    public SoundFontList()
    {
        super();
    }

    public SoundFontList(Minecraft minecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight, int pLeft, Consumer<Entry> selectCallback)
    {
        super(minecraft, pWidth, pHeight, pY0, pY1, pItemHeight, pLeft, selectCallback);
    }

    public SoundFontList init()
    {
        int suggestedWidth = 0;
        for (SoundFontProxy soundFontProxy: SoundFontProxyManager.soundFontProxyMapById.values())
        {
            suggestedWidth = calculateWidth(suggestedWidth, soundFontProxy.id);
            SoundFontList.Entry entry = new  SoundFontList.Entry(soundFontProxy);
            super.addEntry(entry);
            if ((Objects.requireNonNull(minecraft.player).inventory.getSelected().getItem()) instanceof IInstrument &&
                    (soundFontProxy.index == minecraft.player.inventory.getSelected().getMaxDamage()))
            {
                super.setSelected(entry);
            }
        }
        if (super.getSelected() == null)
        {
            super.setSelected(children().get(0));
        }
        super.centerScrollOn(super.getSelected());
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
    public void setCallBack(Consumer<Entry> selectCallback)
    {
        this.selectCallback = selectCallback;
    }

    public class Entry extends MXExtendedList.AbstractListEntry<Entry>
    {
        protected SoundFontProxy soundFontProxy;

        public Entry(SoundFontProxy soundFontProxy)
        {
            this.soundFontProxy = soundFontProxy;
        }

        public int getIndex()
        {
            return soundFontProxy.index;
        }

        public String getId()
        {
            return soundFontProxy.id;
        }

        public int getPackedPreset()
        {
            return soundFontProxy.packed_preset;
        }

        @Override
        public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks)
        {
            if (pIsMouseOver && isActive())
            {
                fill(pMatrixStack, pLeft - 2, pTop - 2, pLeft - 5 + width, pTop + itemHeight - 1, 0xA0A0A0A0);
            }

            ITextComponent translated = new TranslationTextComponent(String.format("item.mxtune.%s", soundFontProxy.id));
            ITextProperties trimmed = minecraft.font.substrByWidth(translated, pWidth - 6);
            minecraft.font.drawShadow(pMatrixStack, trimmed.getString(), (float) (pLeft), (float) (pTop + 1), 16777215, true);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
        {
            if (isMouseOver(pMouseX, pMouseY) && isActive() && selectCallback != null){
                changeFocus(true);
                setFocused(this);
                SoundFontList.this.setSelected(this);
                selectCallback.accept(this);
                minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            return false;
        }
    }
}


