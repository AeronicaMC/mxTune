package aeronicamc.mods.mxtune.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class RenderHelper
{
    private RenderHelper() { /* NOP */ }

    /**
     * Defaults to 256x256 texture sheet for use with the vanilla toast textures. <p></p>
     * Inspired directly by the vanilla {@link AbstractGui} class instead of dealing w a non-static method.
     * @param pMatrixStack  The current matrix stack
     * @param pX            Screen X position
     * @param pY            Screen Y position
     * @param pUOffset      U offset into the texture sheet
     * @param pVOffset      V offset into the texture sheet
     * @param pUWidth       U width
     * @param pVHeight      V height
     */
    static void blit(MatrixStack pMatrixStack, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
        AbstractGui.blit(pMatrixStack, pX, pY, RenderEvents.blitOffset, (float)pUOffset, (float)pVOffset, pUWidth, pVHeight, 256, 256);
    }

    /**
     * A helper method also borrowed from the WorldRenderer for rendering a blocks hit-box
     * @param level         The level (world for you MCP laggards. Parchment Rulez!)
     * @param pMatrixStack  The current matrix stack
     * @param pBuffer       The vertex builder buffer
     * @param pEntity       The viewing entity (Player or other?)
     * @param pX            Camera X position
     * @param pY            Camera Y position
     * @param pZ            Camera Z position
     * @param pBlockPos     BlockPos in the level
     * @param pBlockState   BlockState at the BlockPos
     */
    static void renderHitOutline(World level, MatrixStack pMatrixStack, IVertexBuilder pBuffer, Entity pEntity, double pX, double pY, double pZ, BlockPos pBlockPos, BlockState pBlockState) {
        renderShape(pMatrixStack, pBuffer, pBlockState.getShape(level, pBlockPos, ISelectionContext.of(pEntity)), (double)pBlockPos.getX() - pX, (double)pBlockPos.getY() - pY, (double)pBlockPos.getZ() - pZ, 1.0F, 0.0F, 1.0F, 0.4F);
    }

    /**
     * An interesting method borrowed from the WorldRenderer that renders shape outlines.
     * @param pMatrixStack  The current matrix stack
     * @param pBuffer       The vertex builder buffer
     * @param pShape        The shape to be rendered
     * @param pX            Camera X position
     * @param pY            Camera Y position
     * @param pZ            Camera Z position
     * @param pRed          Red colour component (No I'm not British, but 'colour' it looks cooler and 'color')
     * @param pGreen        Green colour component
     * @param pBlue         Blue color component
     * @param pAlpha        Alpha component
     */
    static void renderShape(MatrixStack pMatrixStack, IVertexBuilder pBuffer, VoxelShape pShape, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha) {
        Matrix4f matrix4f = pMatrixStack.last().pose();
        pShape.forAllEdges((edgeVertexBegin_X, edgeVertexBegin_Y, edgeVertexBegin_Z, edgeVertexEnd_X, edgeVertexEnd_Y, edgeVertexEnd_Z) -> {
            pBuffer.vertex(matrix4f, (float)(edgeVertexBegin_X + pX), (float)(edgeVertexBegin_Y + pY), (float)(edgeVertexBegin_Z + pZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
            pBuffer.vertex(matrix4f, (float)(edgeVertexEnd_X + pX), (float)(edgeVertexEnd_Y + pY), (float)(edgeVertexEnd_Z + pZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        });
    }

    static void renderFloatingText(Vector3d pos, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, ActiveRenderInfo activeRenderInfo, int pColor, ITextComponent pDisplayName, int packedLight) {
        Vector3d cam = activeRenderInfo.getPosition();
        renderFloatingText(pDisplayName, pos, pMatrixStack, pBuffer, activeRenderInfo.rotation(), cam.x(), cam.y(), cam.z(), pColor, packedLight);
    }

    static void renderFloatingText(ITextComponent pDisplayName, BlockPos blockPos, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, ActiveRenderInfo activeRenderInfo, int pColor)
    {
        Vector3d cam = activeRenderInfo.getPosition();
        Vector3d pos = new Vector3d(blockPos.getX() + 0.5D, blockPos.getY() + 1.5D, blockPos.getZ() + 0.5D);
        renderFloatingText(pDisplayName, pos, pMatrixStack, pBuffer, activeRenderInfo.rotation(), cam.x(), cam.y(), cam.z(), pColor, 15728880);
    }

    /**
     * Based on {@link DebugRenderer#renderFloatingText(String, double, double, double, int, float, boolean, float, boolean)}
     * @param pDisplayName  Display text
     * @param pos           Position in the world
     * @param pMatrixStack  The current matrix stack
     * @param pBuffer       The raw render type buffer
     * @param rotation      The rotation of the viewing entities eyes {@link ActiveRenderInfo#rotation()}
     * @param pCamX         Camera X position
     * @param pCamY         Camera Y position
     * @param pCamZ         Camera Z position
     * @param pColor        colour and alpha
     * @param packedLight
     */
    static void renderFloatingText(ITextComponent pDisplayName, Vector3d pos, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, Quaternion rotation, double pCamX, double pCamY, double pCamZ, int pColor, int packedLight) {
        pMatrixStack.pushPose();
        pMatrixStack.translate(pos.x - pCamX, pos.y - pCamY, pos.z - pCamZ);
        pMatrixStack.mulPose(rotation);
        pMatrixStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = pMatrixStack.last().pose();
        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int)(f1 * 255.0F) << 24;
        FontRenderer fontrenderer = Minecraft.getInstance().font;
        float f2 = (float)((double) -fontrenderer.width(pDisplayName) / 2);
        fontrenderer.drawInBatch(pDisplayName, f2, (float)0, pColor, false, matrix4f, pBuffer, false, j, packedLight);
        pMatrixStack.popPose();
    }

    static void renderEdges(MatrixStack pMatrixStack, IVertexBuilder pBuffer, AxisAlignedBB alignedBB, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha)
    {
        renderShape(pMatrixStack, pBuffer, VoxelShapes.create(alignedBB), -pX, -pY, -pZ, pRed, pGreen, pBlue, pAlpha);
    }

    static void renderFaces(MatrixStack pMatrixStack, IVertexBuilder pBuffer, final AxisAlignedBB alignedBB, double camX, double camY, double camZ, float pRed, float pGreen, float pBlue, float pAlpha) {
        Matrix4f matrix4f = pMatrixStack.last().pose();
        AxisAlignedBB box = alignedBB;
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

//        // North outer
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//
//        // South outer
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//
//        // East outer
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//
//        // West outer
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//
//        // Bottom outer
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//
//        // Top outer
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
//        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
    }
}
