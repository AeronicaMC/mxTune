package aeronicamc.mods.mxtune.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;

public class StageAreaRenderer
{
    private StageAreaRenderer() { /* NOP */ }

    public static void renderEdges(MatrixStack pMatrixStack, IVertexBuilder pBuffer, VoxelShape pShape, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha) {
        Matrix4f matrix4f = pMatrixStack.last().pose();
        pShape.forAllEdges((edgeVertexBegin_X, edgeVertexBegin_Y, edgeVertexBegin_Z, edgeVertexEnd_X, edgeVertexEnd_Y, edgeVertexEnd_Z) -> {
            pBuffer.vertex(matrix4f, (float)(edgeVertexBegin_X - pX), (float)(edgeVertexBegin_Y - pY), (float)(edgeVertexBegin_Z - pZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
            pBuffer.vertex(matrix4f, (float)(edgeVertexEnd_X - pX), (float)(edgeVertexEnd_Y - pY), (float)(edgeVertexEnd_Z - pZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        });
    }

    public static void renderFaces(MatrixStack pMatrixStack, IVertexBuilder pBuffer, final AxisAlignedBB aabb, double camX, double camY, double camZ, float pRed, float pGreen, float pBlue, float pAlpha) {
        Matrix4f matrix4f = pMatrixStack.last().pose();
        AxisAlignedBB box = aabb.inflate(.001);
        // North inner
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // South inner
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // East inner
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // West inner
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // Bottom inner
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // top inner
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // North outer
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // South outer
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // East outer
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // West outer
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // Bottom outer
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // Top outer
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
    }

    public static void renderFloatingText(ITextComponent pDisplayName, Vector3d pos, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, ActiveRenderInfo activeRenderInfo, int pColor) {
        pMatrixStack.pushPose();
        Vector3d cam = activeRenderInfo.getPosition();
        pMatrixStack.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        pMatrixStack.mulPose(activeRenderInfo.rotation());
        pMatrixStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = pMatrixStack.last().pose();
        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int)(f1 * 255.0F) << 24;
        FontRenderer fontrenderer = Minecraft.getInstance().font;
        float f2 = (float)(-fontrenderer.width(pDisplayName) / 2);
        fontrenderer.drawInBatch(pDisplayName, f2, (float)0, pColor, false, matrix4f, pBuffer, false, j, 15728880);
        pMatrixStack.popPose();
    }
}
