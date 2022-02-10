package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.caps.stages.ServerStageAreaProvider;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class StageAreaRenderer
{
    private static final Minecraft mc = Minecraft.getInstance();
    private StageAreaRenderer() { /* NOP */ }

    public static void render(MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, LightTexture pLightTexture, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, ClippingHelper pClippingHelper)
    {
        final PlayerEntity player = mc.player;
        World level;
        if (player == null || (level = player.level) == null) return;

        Vector3d camera = pActiveRenderInfo.getPosition();
        double camX = camera.x;
        double camY = camera.y;
        double camZ = camera.z;

        ServerStageAreaProvider.getServerStageAreas(level).ifPresent(
                areas -> {
                    areas.getStageAreas().stream().filter(area-> pClippingHelper.isVisible(area.getAreaAABB())).forEach(
                            (area) -> {
                                IVertexBuilder vertexBuilder1 = pBuffer.getBuffer(RenderType.lightning());
                                if (mc.options.graphicsMode.equals(GraphicsFanciness.FABULOUS))
                                    StageAreaRenderer.renderFaces(pMatrixStack, vertexBuilder1, area.getAreaAABB(), camX, camY, camZ, 1F, 0F, 1F, 0.1F);

                                IVertexBuilder vertexBuilder2 = pBuffer.getBuffer(RenderType.lines());
                                VoxelShape cubeShape = VoxelShapes.create(area.getAreaAABB());
                                StageAreaRenderer.renderEdges(pMatrixStack, vertexBuilder2, cubeShape, camX, camY, camZ, 1F, 0F, 1F, 1F);

                                if (!(pActiveRenderInfo.getEntity().distanceToSqr(area.getAreaAABB().getCenter()) > 512))
                                {
                                    StageAreaRenderer.renderFloatingText(new StringTextComponent(area.getTitle()),
                                                                         area.getAreaAABB().getCenter(),
                                                                         pMatrixStack,
                                                                         pBuffer, pActiveRenderInfo, -1);

                                    StageAreaRenderer.renderFloatingText(new StringTextComponent("Audience Spawn"),
                                                                         new Vector3d(area.getAudienceSpawn().getX() + 0.5, area.getAudienceSpawn().getY() + 1.5, area.getAudienceSpawn().getZ() + 0.5),
                                                                         pMatrixStack,
                                                                         pBuffer, pActiveRenderInfo, -1);

                                    StageAreaRenderer.renderFloatingText(new StringTextComponent("Performer Spawn"),
                                                                         new Vector3d(area.getPerformerSpawn().getX() + 0.5, area.getPerformerSpawn().getY() + 1.5, area.getPerformerSpawn().getZ() + 0.5),
                                                                         pMatrixStack,

                                                                         pBuffer, pActiveRenderInfo, -1);
                                }
                                // Need check if RenderType.lightning() is actually part of the ShaderGroup transparencyChain
                                if (mc.levelRenderer.transparencyChain != null)
                                {
                                    pBuffer.endBatch();
                                    pBuffer.endBatch(RenderType.lines());
                                    pBuffer.endBatch(RenderType.lightning());
                                } else
                                {
                                    pBuffer.endBatch();
                                    pBuffer.endBatch(RenderType.lines());
                                }
                            });
                });
    }

    public static void renderHitOutline(World level, MatrixStack pMatrixStack, IVertexBuilder pBuffer, Entity pEntity, double pX, double pY, double pZ, BlockPos pBlockPos, BlockState pBlockState) {
        renderEdges(pMatrixStack, pBuffer, pBlockState.getShape(level, pBlockPos, ISelectionContext.of(pEntity)), (double)pBlockPos.getX() - pX, (double)pBlockPos.getY() - pY, (double)pBlockPos.getZ() - pZ, 1.0F, 0.0F, 1.0F, 1.0F);
    }

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
