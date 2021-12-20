package aeronicamc.mods.mxtune.gui.widget.list;

import aeronicamc.mods.mxtune.gui.widget.ILayout;
import aeronicamc.mods.mxtune.util.AntiNull;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public abstract class MXExtendedList<E extends AbstractList.AbstractListEntry<E>> extends AbstractList<E> implements ILayout
{
    protected int padding;
    protected int rowWidth;
    protected Consumer<E> selectCallback;
    private boolean renderBackground;
    protected boolean active = true;
    private boolean renderTopAndBottom;

    public MXExtendedList()
    {
        this(Minecraft.getInstance(), 1, 1, 1, 1, Minecraft.getInstance().font.lineHeight + 4, 1, AntiNull.nonNullInjected());
    }

    public MXExtendedList(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight, int pLeft, Consumer<E> selectCallback)
    {
        super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
        this.rowWidth = pWidth;
        this.setLeftPos(pLeft);
        this.selectCallback = selectCallback;
        super.setRenderTopAndBottom(false);
        super.setRenderSelection(true);
        super.setRenderBackground(true);
    }

    @Override
    public void setPosition(int pX, int pY)
    {
        super.setLeftPos(pX);
        this.y0 = pY;
        this.y1 = y0 + height;
    }

    public void setLayout(int pX, int pY, int pWidth, int pHeight)
    {
        this.width = pWidth;
        this.setRowWidth(pWidth);
        this.setLeftPos(pX);
        this.height = pHeight;
        this.y0 = pY;
        this.y1 = y0 + pHeight;
    }

    @Override
    public int getLeft()
    {
        return super.getLeft();
    }

    @Override
    public int getTop()
    {
        return super.getTop();
    }

    @Override
    public int getRight()
    {
        return super.getRight() + padding;
    }

    @Override
    public int getBottom()
    {
        return super.getBottom() + padding;
    }

    @Override
    public int getPadding()
    {
        return padding;
    }

    @Override
    public void setPadding(int padding)
    {
        this.padding = padding;
    }

    public abstract void setCallBack(Consumer<E> selectCallback);

    @Override
    protected int getScrollbarPosition()
    {
        return x0 + width - 6;
    }

    @Override
    public void centerScrollOn(E pEntry)
    {
        super.centerScrollOn(pEntry);
    }

    @Override
    public void ensureVisible(E pEntry)
    {
        super.ensureVisible(pEntry);
    }

    @Override
    public int getRowWidth()
    {
        return this.rowWidth;
    }

    public void setRowWidth(int rowWidth)
    {
        this.rowWidth = rowWidth;
    }

    @Override
    public void setRenderBackground(boolean renderBackground)
    {
        this.renderBackground = renderBackground;
    }

    @Override
    public void setRenderTopAndBottom(boolean renderTopAndBottom)
    {
        this.renderTopAndBottom = renderTopAndBottom;
    }

    public void setActive(boolean pValue)
    {
        active = pValue;
        super.setRenderSelection(pValue);
    }

    public boolean isActive()
    {
        return active;
    }

    protected void renderBorder(MatrixStack pPoseStack, int pMouseX, int pMouseY)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        int topY = y0 - 1;
        int botY = y0 + this.height + this.headerHeight + 1;
        int leftX = getLeft() - 1 ;
        int rightX = getRight() + 1;
        RenderSystem.disableTexture();
        float f = this.isMouseOver(pMouseX, pMouseY) && active ? 1.0F : 0.3F;
        RenderSystem.color4f(f, f, f, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.vertex((double)leftX, (double)(botY), 0.0D).endVertex();
        bufferbuilder.vertex((double)rightX, (double)(botY), 0.0D).endVertex();
        bufferbuilder.vertex((double)rightX, (double)(topY), 0.0D).endVertex();
        bufferbuilder.vertex((double)leftX, (double)(topY), 0.0D).endVertex();
        tessellator.end();
        RenderSystem.enableTexture();
    }

    @Override
    protected void renderBackground(MatrixStack pMatrixStack)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        this.minecraft.getTextureManager().bind(AbstractGui.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        tessellator.end();
    }

    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        renderBorder(pMatrixStack, pMouseX, pMouseY);
        MainWindow client = minecraft.getWindow();
        double scaleW = (double) client.getWidth() / client.getGuiScaledWidth();
        double scaleH = (double) client.getHeight() / client.getGuiScaledHeight();
        RenderSystem.enableScissor((int) (this.x0 * scaleW), (int) (client.getScreenHeight() - (y1 * scaleH)),
                                   (int) ((x1 - x0) * scaleW), (int) ((y1 - y0) * scaleH));

        this.renderBackground(pMatrixStack);
        int i = this.getScrollbarPosition();
        int j = i + 6;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        if (this.renderBackground) {
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

        int j1 = this.getRowLeft();
        int k = this.y0 + 4 - (int)this.getScrollAmount();

        this.renderList(pMatrixStack, j1, k, pMouseX, pMouseY, pPartialTicks);
        if (this.renderTopAndBottom) {
            this.minecraft.getTextureManager().bind(AbstractGui.BACKGROUND_LOCATION);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            float f1 = 32.0F;
            int l = -100;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)this.y0, -100.0D).uv(0.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y0, -100.0D).uv((float)this.width / 32.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), 0.0D, -100.0D).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.height, -100.0D).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.height, -100.0D).uv((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y1, -100.0D).uv((float)this.width / 32.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y1, -100.0D).uv(0.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            tessellator.end();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.disableAlphaTest();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableTexture();
            int i1 = 4;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)(this.y0 + 4), 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)(this.y0 + 4), 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)(this.y1 - 4), 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)(this.y1 - 4), 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            tessellator.end();
        }

        int k1 = this.getMaxScroll();
        if (k1 > 0) {
            RenderSystem.disableTexture();
            int l1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            l1 = MathHelper.clamp(l1, 32, this.y1 - this.y0 - 8);
            int i2 = (int)this.getScrollAmount() * (this.y1 - this.y0 - l1) / k1 + this.y0;
            if (i2 < this.y0) {
                i2 = this.y0;
            }

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)i, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)j, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)j, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)i, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)i, (double)(i2 + l1), 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double)j, (double)(i2 + l1), 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double)j, (double)i2, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double)i, (double)(i2 + l1 - 1), 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((double)(j - 1), (double)(i2 + l1 - 1), 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((double)(j - 1), (double)i2, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            tessellator.end();
        }
        RenderSystem.disableScissor();
        this.renderDecorations(pMatrixStack, pMouseX, pMouseY);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    }

    public abstract static class AbstractListEntry<E extends MXExtendedList.AbstractListEntry<E>> extends AbstractList.AbstractListEntry<E> {
        public boolean changeFocus(boolean pFocus) {
            return false;
        }
    }
}
