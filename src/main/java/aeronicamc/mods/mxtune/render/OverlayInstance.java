package aeronicamc.mods.mxtune.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class OverlayInstance<T extends IOverlayItem> {
    private final IItemOverlayPosition overlayItemPosition;
    private final T overlayItem;
    private long animationTime = -1L;
    private long visibleTime = -1L;
    private IOverlayItem.Visibility visibility = IOverlayItem.Visibility.SHOW;

    public OverlayInstance(IItemOverlayPosition overlayItemPosition, T pOverlayItem) {
        this.overlayItemPosition = overlayItemPosition;
        this.overlayItem = pOverlayItem;
    }

    public T getOverlayItem() {
        return this.overlayItem;
    }

    private float getVisibility(long milliseconds) {
        float f = MathHelper.clamp((float) (milliseconds - this.animationTime) / 600.0F, 0.0F, 1.0F);
        f = f * f;
        return this.visibility == IOverlayItem.Visibility.HIDE ? 1.0F - f : f;
    }

    /**
     * Render an overlay instance.
     *
     * @param scaledWidth  Gui Scaled Width.
     * @param scaledHeight Gui Scaled Height.
     * @param instIndex    Instance index.
     * @param pPoseStack   matrix.
     * @return true when rendering is done for this instance. Slide in, pause, slide out.
     */
    @SuppressWarnings("deprecation")
    public boolean render(int scaledWidth, int scaledHeight, int instIndex, MatrixStack pPoseStack) {
        long ms = Util.getMillis();
        if (this.animationTime == -1L) {
            this.animationTime = ms;
            this.visibility.playSound(RenderHelper.mc.getSoundManager());
        }

        if (this.visibility == IOverlayItem.Visibility.SHOW && ms - this.animationTime <= 600L) {
            this.visibleTime = ms;
        }

        IOverlayItem.Position position = overlayItemPosition.getPosition(this.overlayItem);
        float decimalPercent = Math.max(Math.min(Math.abs(overlayItemPosition.getPercent(this.overlayItem) / 100F), 1F), 0F);
        float xPos;
        float yPos = ((scaledHeight - this.overlayItem.totalHeight()) * decimalPercent);
        float zPos = overlayItem.isManagedPosition() ? 0F + instIndex : 800F + instIndex;

        switch (position) {
            case LEFT:
                xPos = this.overlayItem.totalWidth() * (1 - this.getVisibility(ms)) + this.overlayItem.totalWidth() * (this.getVisibility(ms)) - this.overlayItem.totalWidth();
                break;
            case CENTER:
                xPos = ((scaledWidth * this.getVisibility(ms) * 0.5F)) + ((scaledWidth * (1F - this.getVisibility(ms)) * 0.5F)) - this.overlayItem.totalWidth() * 0.5F * (this.getVisibility(ms));
                break;
            case RIGHT:
                xPos = (float) scaledWidth - (float) this.overlayItem.totalWidth() * this.getVisibility(ms);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + position);
        }

        RenderSystem.pushMatrix();

        RenderSystem.translatef(xPos, yPos, zPos);
        RenderSystem.scalef(this.getVisibility(ms), this.getVisibility(ms), 1F);

        IOverlayItem.Visibility overlayVisibility = this.overlayItem.render(pPoseStack, ms - this.visibleTime);
        RenderSystem.popMatrix();
        if (overlayVisibility != this.visibility) {
            this.animationTime = ms - (long) ((int) ((1.0F - this.getVisibility(ms)) * 600.0F));
            this.visibility = overlayVisibility;
            this.visibility.playSound(RenderHelper.mc.getSoundManager());
        }

        return this.visibility == IOverlayItem.Visibility.HIDE && ms - this.animationTime > 600L;
    }
}
