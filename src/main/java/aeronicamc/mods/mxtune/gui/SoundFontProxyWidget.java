package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.util.AntiNull;
import aeronicamc.mods.mxtune.util.SoundFontProxy;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;

public class SoundFontProxyWidget extends ModExtendedList<SoundFontProxyWidget.Entry>
{

    public SoundFontProxyWidget()
    {
        this(Minecraft.getInstance(), 1, 1, 1, 1, Minecraft.getInstance().font.lineHeight + 4, 1, AntiNull.nonNullInjected());
    }

    public SoundFontProxyWidget(Minecraft minecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight, int pLeft, Consumer<Entry> selectCallback)
    {
        super(minecraft, pWidth, pHeight, pY0, pY1, pItemHeight, pLeft, selectCallback);
        super.setLeftPos(pLeft);
        super.setRenderTopAndBottom(false);
        super.setRenderSelection(true);
        super.setRenderBackground(true);
    }

    public void setLayout(int pWidth, int pHeight, int pY0, int pY1, int pLeft)
    {
        this.width = pWidth;
        this.setRowWidth(pWidth);
        this.setLeftPos(pLeft);
        this.height = pHeight;
        super.y0 = pY0;
        super.y1 = pY1;
        super.setRenderTopAndBottom(false);
        super.setRenderSelection(true);
        super.setRenderBackground(true);
    }

    public void setCallBack(Consumer<Entry> selectCallback)
    {
        this.selectCallback = selectCallback;
    }

    @Override
    protected int getScrollbarPosition()
    {
        return x0 + width - 6;
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
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        this.minecraft.getTextureManager().bind(AbstractGui.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        tessellator.end();
    }

    public class Entry extends ModExtendedList.AbstractListEntry<Entry>
    {
        SoundFontProxy soundFontProxy;

        public Entry(SoundFontProxy soundFontProxy)
        {
            this.soundFontProxy = soundFontProxy;
        }

        public int getIndex()
        {
            return soundFontProxy.index;
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

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
        {
            if (isMouseOver(pMouseX, pMouseY)){
                changeFocus(true);
                setFocused(this);
                SoundFontProxyWidget.this.setSelected(this);
                selectCallback.accept(this);
                minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            return false;
        }
    }
}


