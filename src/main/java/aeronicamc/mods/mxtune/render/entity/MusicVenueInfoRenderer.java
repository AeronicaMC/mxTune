package aeronicamc.mods.mxtune.render.entity;

import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
import aeronicamc.mods.mxtune.render.ModRenderType;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.PaintingSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class MusicVenueInfoRenderer extends EntityRenderer<MusicVenueInfoEntity>
{
    public MusicVenueInfoRenderer(EntityRendererManager rendererManager)
    {
        super(rendererManager);
    }

    @Override
    public void render(MusicVenueInfoEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight)
    {
        pMatrixStack.pushPose();
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pEntityYaw));
        float f = 0.0625F;

        pMatrixStack.scale(0.0625F, 0.0625F, 0.0625F);
        PaintingSpriteUploader paintingspriteuploader = Minecraft.getInstance().getPaintingTextures();
        this.renderInfoPanel(pMatrixStack, pBuffer, pEntity, pEntity.getWidth(), pEntity.getHeight(), paintingspriteuploader.getBackSprite());
        pMatrixStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(MusicVenueInfoEntity pEntity) {
        return Minecraft.getInstance().getPaintingTextures().getBackSprite().atlas().location();
    }

    /**
     * Copied from the vanilla Painting Renderer and modified to use a dynamic texture.
     */
    private void renderInfoPanel(MatrixStack matrixStack, IRenderTypeBuffer pBuffer, MusicVenueInfoEntity venueInfoEntity, int width, int height, TextureAtlasSprite backSprite) {
        MatrixStack.Entry matrixStack$entry = matrixStack.last();
        Matrix4f matrix4f = matrixStack$entry.pose();
        Matrix3f matrix3f = matrixStack$entry.normal();
        float f = (float)(-width) / 2.0F;
        float f1 = (float)(-height) / 2.0F;
        float f2 = 0.5F;
        float f3 = backSprite.getU0();
        float f4 = backSprite.getU1();
        float f5 = backSprite.getV0();
        float f6 = backSprite.getV1();
        float f7 = backSprite.getU0();
        float f8 = backSprite.getU1();
        float f9 = backSprite.getV0();
        float f10 = backSprite.getV(1.0D);
        float f11 = backSprite.getU0();
        float f12 = backSprite.getU(1.0D);
        float f13 = backSprite.getV0();
        float f14 = backSprite.getV1();
        int hPanelCnt = width / 16;
        int vPanelCnt = height / 16;
        double d0 = 16.0D / (double)hPanelCnt;
        double d1 = 16.0D / (double)vPanelCnt;
        //System.out.printf("***RenderStart***\n");

        for(int hPanelPos = 0; hPanelPos < hPanelCnt; ++hPanelPos) {
            for(int vPanelPos = 0; vPanelPos < vPanelCnt; ++vPanelPos) {
                float f15 = f + (float)((hPanelPos + 1) * 16);
                float f16 = f + (float)(hPanelPos * 16);
                float f17 = f1 + (float)((vPanelPos + 1) * 16);
                float f18 = f1 + (float)(vPanelPos * 16);
                int i1 = MathHelper.floor(venueInfoEntity.getX());
                int j1 = MathHelper.floor(venueInfoEntity.getY() + (double)((f17 + f18) / 2.0F / 16.0F));
                int k1 = MathHelper.floor(venueInfoEntity.getZ());
                Direction direction = venueInfoEntity.getDirection();
                if (direction == Direction.NORTH) {
                    i1 = MathHelper.floor(venueInfoEntity.getX() + (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.WEST) {
                    k1 = MathHelper.floor(venueInfoEntity.getZ() - (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.SOUTH) {
                    i1 = MathHelper.floor(venueInfoEntity.getX() - (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.EAST) {
                    k1 = MathHelper.floor(venueInfoEntity.getZ() + (double)((f15 + f16) / 2.0F / 16.0F));
                }

                int lightColor = WorldRenderer.getLightColor(venueInfoEntity.level, new BlockPos(i1, j1, k1));

                // Painting
//                float f19 = paintingSprite.getU(d0 * (double)(i - k));
//                float f20 = paintingSprite.getU(d0 * (double)(i - (k + 1)));
//                float f21 = paintingSprite.getV(d1 * (double)(j - l));
//                float f22 = paintingSprite.getV(d1 * (double)(j - (l + 1)));
//
//                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f20, f21, -0.5F, 0, 0, -1, l1);
//                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f19, f21, -0.5F, 0, 0, -1, l1);
//                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f19, f22, -0.5F, 0, 0, -1, l1);
//                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f20, f22, -0.5F, 0, 0, -1, l1);

                // Dynamic Texture rendering for the information panel
                IVertexBuilder infoVertexBuilder = InfoRenderer.getInstance().getInfoRenderVertexBuilder(pBuffer, venueInfoEntity);
                int lightColorInfo = ModRenderType.FULL_BRIGHT_LIGHT_MAP;
                if (infoVertexBuilder != null)
                {

                    float u0 = (float) (d0 * (double)(hPanelCnt - hPanelPos))/16;
                    float u1 = (float) (d0 * (double)(hPanelCnt - (hPanelPos + 1)))/16;
                    float v0 = (float) (d1 * (double)(vPanelCnt - vPanelPos))/16;
                    float v1 = (float) (d1 * (double)(vPanelCnt - (vPanelPos + 1)))/16;
                    //System.out.printf("u0=%f u1=%f hpc=%d hpp=%d\n", u0, u1, hPanelCnt, hPanelPos);
                    //System.out.printf("v0=%f v1=%f vpc=%d vpp=%d\n\n", v0, v1, vPanelCnt, vPanelPos);

                    this.vertexInfo(matrix4f, matrix3f, infoVertexBuilder, f15, f18, u1, v0, -0.5F, 0, 0, -1, lightColorInfo);
                    this.vertexInfo(matrix4f, matrix3f, infoVertexBuilder, f16, f18, u0, v0, -0.5F, 0, 0, -1, lightColorInfo);
                    this.vertexInfo(matrix4f, matrix3f, infoVertexBuilder, f16, f17, u0, v1, -0.5F, 0, 0, -1, lightColorInfo);
                    this.vertexInfo(matrix4f, matrix3f, infoVertexBuilder, f15, f17, u1, v1, -0.5F, 0, 0, -1, lightColorInfo);
                }

                IVertexBuilder vertexBuilder = pBuffer.getBuffer(RenderType.entitySolid(this.getTextureLocation(venueInfoEntity)));
                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f3, f5, 0.5F, 0, 0, 1, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f4, f5, 0.5F, 0, 0, 1, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f4, f6, 0.5F, 0, 0, 1, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f3, f6, 0.5F, 0, 0, 1, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f7, f9, -0.5F, 0, 1, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f8, f9, -0.5F, 0, 1, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f8, f10, 0.5F, 0, 1, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f7, f10, 0.5F, 0, 1, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f7, f9, 0.5F, 0, -1, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f8, f9, 0.5F, 0, -1, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f8, f10, -0.5F, 0, -1, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f7, f10, -0.5F, 0, -1, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f12, f13, 0.5F, -1, 0, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f12, f14, 0.5F, -1, 0, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f11, f14, -0.5F, -1, 0, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f11, f13, -0.5F, -1, 0, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f12, f13, -0.5F, 1, 0, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f12, f14, -0.5F, 1, 0, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f11, f14, 0.5F, 1, 0, 0, lightColor);
                this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f11, f13, 0.5F, 1, 0, 0, lightColor);
            }
        }
        //System.out.printf("---RenderStop---\n\n");
    }

    private void vertex(Matrix4f matrix4f$pose, Matrix3f matrix3f$normal, IVertexBuilder vertexBuilder, float pPoseX, float pPoseY, float pU, float pV, float pPoseZ, int pNormalX, int pNormalY, int pNormalZ, int pLightMap) {
        vertexBuilder.vertex(matrix4f$pose, pPoseX, pPoseY, pPoseZ).color(255, 255, 255, 255).uv(pU, pV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pLightMap).normal(matrix3f$normal, (float)pNormalX, (float)pNormalY, (float)pNormalZ).endVertex();
    }

    private void vertexInfo(Matrix4f matrix4f$pose, Matrix3f matrix3f$normal, IVertexBuilder vertexBuilder, float pPoseX, float pPoseY, float pU, float pV, float pPoseZ, int pNormalX, int pNormalY, int pNormalZ, int pLightMap) {
        vertexBuilder.vertex(matrix4f$pose, pPoseX, pPoseY, pPoseZ).color(255, 255, 255, 255).uv(pU, pV).uv2(pLightMap).normal(matrix3f$normal, (float)pNormalX, (float)pNormalY, (float)pNormalZ).endVertex();
    }
}
