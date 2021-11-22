package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.util.SoundFontProxy;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.TranslationTextComponent;

public class SoundFontProxyWidget extends ExtendedList<SoundFontProxyWidget.List.Entry>
{
    SoundFontProxyWidget.List list;

    public SoundFontProxyWidget(Minecraft minecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight)
    {
        super(minecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
    }

    public static class List extends ExtendedList<SoundFontProxyWidget.List.Entry>
    {
        public List(Minecraft minecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight)
        {
            super(minecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
            for (SoundFontProxy soundFontProxy: SoundFontProxyManager.soundFontProxyMapById.values())
            {
                SoundFontProxyWidget.List.Entry entry = new  SoundFontProxyWidget.List.Entry(soundFontProxy);
                this.addEntry(entry);
                assert minecraft.player != null;
                if (soundFontProxy.index == minecraft.player.getMainHandItem().getMaxDamage())
                    this.setSelected(entry);
            }
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        @Override
        protected void renderBackground(MatrixStack pMatrixStack)
        {
            super.renderBackground(pMatrixStack);
        }

        public class Entry extends ExtendedList.AbstractListEntry<SoundFontProxyWidget.List.Entry>
        {
            SoundFontProxy soundFontProxy;

            public Entry(SoundFontProxy soundFontProxy)
            {
                this.soundFontProxy = soundFontProxy;
            }

            @Override
            public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks)
            {
                String s = new TranslationTextComponent(String.format("item.mxtune.%s", soundFontProxy.id)).getString();
                minecraft.font.drawShadow(pMatrixStack, s, (float)(list.getLeft() + 2), (float)(pTop + 1), 16777215, true);
            }
        }
    }
}
