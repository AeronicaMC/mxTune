package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.caps.stages.ServerStageAreaProvider;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import static aeronicamc.mods.mxtune.render.RenderHelper.renderShape;

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
                                IVertexBuilder vertexBuilder1 = pBuffer.getBuffer(ModRenderType.TRANSPARENT_QUADS_NO_TEXTURE);
                                renderFaces(pMatrixStack, vertexBuilder1, area.getAreaAABB(), camX, camY, camZ, 1F, 0F, 1F, 0.1F);

                                IVertexBuilder vertexBuilder2 = pBuffer.getBuffer(RenderType.lines());
                                renderEdges(pMatrixStack, vertexBuilder2, area.getAreaAABB(), camX, camY, camZ, 1F, 0F, 1F, 1F);

                                if (!(pActiveRenderInfo.getEntity().distanceToSqr(area.getAreaAABB().getCenter()) > 512))
                                {
                                    RenderHelper.renderFloatingText(area.getAreaAABB().getCenter(), pMatrixStack, pBuffer, pActiveRenderInfo, -1, new StringTextComponent(area.getTitle()),
                                                                    RenderHelper.PACKED_LIGHT_MAX);

                                    RenderHelper.renderFloatingText(new Vector3d(area.getAudienceSpawn().getX() + 0.5, area.getAudienceSpawn().getY() + 1.5, area.getAudienceSpawn().getZ() + 0.5), pMatrixStack, pBuffer, pActiveRenderInfo, -1, new StringTextComponent("Audience Spawn"),
                                                                    RenderHelper.PACKED_LIGHT_MAX);

                                    RenderHelper.renderFloatingText(new Vector3d(area.getPerformerSpawn().getX() + 0.5, area.getPerformerSpawn().getY() + 1.5, area.getPerformerSpawn().getZ() + 0.5), pMatrixStack, pBuffer, pActiveRenderInfo, -1, new StringTextComponent("Performer Spawn"),

                                                                    RenderHelper.PACKED_LIGHT_MAX);
                                }
                                pBuffer.endBatch();
                                pBuffer.endBatch(RenderType.lines());
                                pBuffer.endBatch(ModRenderType.TRANSPARENT_QUADS_NO_TEXTURE);
                            });
                });
    }

    static void renderEdges(MatrixStack pMatrixStack, IVertexBuilder pBuffer, AxisAlignedBB alignedBB, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha)
    {
        renderShape(pMatrixStack, pBuffer, VoxelShapes.create(alignedBB), -pX, -pY, -pZ, pRed, pGreen, pBlue, pAlpha);
    }

    public static void renderFaces(MatrixStack pMatrixStack, IVertexBuilder pBuffer, final AxisAlignedBB alignedBB, double camX, double camY, double camZ, float pRed, float pGreen, float pBlue, float pAlpha) {
        Matrix4f matrix4f = pMatrixStack.last().pose();
        AxisAlignedBB box = alignedBB.inflate(.001);
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

}
