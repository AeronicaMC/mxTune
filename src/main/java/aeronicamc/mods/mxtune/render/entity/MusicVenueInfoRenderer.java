package aeronicamc.mods.mxtune.render.entity;

import aeronicamc.mods.mxtune.caps.venues.EntityVenueState;
import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
import aeronicamc.mods.mxtune.render.ModRenderType;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

/**
 * Based on the vanilla PaintingRenderer and related classes
 */
public class MusicVenueInfoRenderer extends EntityRenderer<MusicVenueInfoEntity>
{
    EntityVenueState sourceVenueState;
    public MusicVenueInfoRenderer(EntityRendererManager rendererManager)
    {
        super(rendererManager);
    }

    @Override
    public void render(MusicVenueInfoEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight)
    {
        pMatrixStack.pushPose();
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pEntityYaw));
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
        float f = (-width) / 2.0F;
        float f1 = (-height) / 2.0F;
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
        double d0 = 16.0D / hPanelCnt;
        double d1 = 16.0D / vPanelCnt;

        for(int hPanelPos = 0; hPanelPos < hPanelCnt; ++hPanelPos) {
            for(int vPanelPos = 0; vPanelPos < vPanelCnt; ++vPanelPos) {
                float f15 = f + ((hPanelPos + 1F) * 16);
                float f16 = f + (hPanelPos * 16F);
                float f17 = f1 + ((vPanelPos + 1F) * 16);
                float f18 = f1 + (vPanelPos * 16F);
                int i1 = MathHelper.floor(venueInfoEntity.getX());
                int j1 = MathHelper.floor(venueInfoEntity.getY() + ((f17 + f18) / 2.0F / 16.0F));
                int k1 = MathHelper.floor(venueInfoEntity.getZ());
                Direction direction = venueInfoEntity.getDirection();
                if (direction == Direction.NORTH) {
                    i1 = MathHelper.floor(venueInfoEntity.getX() + ((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.WEST) {
                    k1 = MathHelper.floor(venueInfoEntity.getZ() - ((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.SOUTH) {
                    i1 = MathHelper.floor(venueInfoEntity.getX() - ((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.EAST) {
                    k1 = MathHelper.floor(venueInfoEntity.getZ() + ((f15 + f16) / 2.0F / 16.0F));
                }

                int lightColor = WorldRenderer.getLightColor(venueInfoEntity.level, new BlockPos(i1, j1, k1));

                // Dynamic Texture rendering for the information panel
                IVertexBuilder infoVertexBuilder = InfoRenderer.getInstance().getInfoRenderVertexBuilder(pBuffer, venueInfoEntity);
                int lightColorInfo = ModRenderType.FULL_BRIGHT_LIGHT_MAP;
                if (infoVertexBuilder != null)
                {
                    float u0 = (float) (d0 * (hPanelCnt - hPanelPos))/16;
                    float u1 = (float) (d0 * (hPanelCnt - (hPanelPos + 1)))/16;
                    float v0 = (float) (d1 * (vPanelCnt - vPanelPos))/16;
                    float v1 = (float) (d1 * (vPanelCnt - (vPanelPos + 1)))/16;
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
        // Render text over the dynamic background image
        matrixStack.pushPose();
        // Move 0,0 from center to upper left corner of the image
        matrixStack.translate(8 * width/16F,  8 * height/16F, -0.52);
        matrixStack.scale(-0.25F, -0.25F, 1F);

        if (InfoRenderer.getInstance().inVenue(venueInfoEntity))
        {
            FontRenderer fontrenderer = Minecraft.getInstance().font;
            ITextComponent iTextComponent1 = new StringTextComponent("mxTune\u266b");
            ITextComponent iTextComponent2 = new StringTextComponent(String.format("%dX%dpx", height * 4, width * 4)).withStyle(TextFormatting.AQUA);
            ITextComponent iTextComponent3 = new StringTextComponent(InfoRenderer.getInstance().getVenue(venueInfoEntity).getId());
            String id = Minecraft.getInstance().font.substrByWidth(iTextComponent3, (width * 4) - 2).getString();
            Minecraft.getInstance().font.drawShadow(matrixStack, "TEST", 1, 1, 0x00dddddd);
            Minecraft.getInstance().font.drawShadow(matrixStack, id, 1, 11, 0x0000FF00);
            Minecraft.getInstance().font.drawShadow(matrixStack, String.format("%dX%d Blk", height / 16, width / 16), 1, 21, 0x00dddddd);
            fontrenderer.drawInBatch(iTextComponent1, 1.0F, 41.0F, -1, false, matrixStack.last().pose(), pBuffer, false, Integer.MIN_VALUE, ModRenderType.FULL_BRIGHT_LIGHT_MAP);
            fontrenderer.drawInBatch(iTextComponent2, 1.0F, 51.0F, -1, false, matrixStack.last().pose(), pBuffer, false, Integer.MIN_VALUE, ModRenderType.FULL_BRIGHT_LIGHT_MAP);
        }
        matrixStack.popPose();
    }

    private void vertex(Matrix4f matrix4f$pose, Matrix3f matrix3f$normal, IVertexBuilder vertexBuilder, float pPoseX, float pPoseY, float pU, float pV, float pPoseZ, int pNormalX, int pNormalY, int pNormalZ, int pLightMap) {
        vertexBuilder.vertex(matrix4f$pose, pPoseX, pPoseY, pPoseZ).color(255, 255, 255, 255).uv(pU, pV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pLightMap).normal(matrix3f$normal, pNormalX, pNormalY, pNormalZ).endVertex();
    }

    private void vertexInfo(Matrix4f matrix4f$pose, Matrix3f matrix3f$normal, IVertexBuilder vertexBuilder, float pPoseX, float pPoseY, float pU, float pV, float pPoseZ, int pNormalX, int pNormalY, int pNormalZ, int pLightMap) {
        vertexBuilder.vertex(matrix4f$pose, pPoseX, pPoseY, pPoseZ).color(255, 255, 255, 255).uv(pU, pV).uv2(pLightMap).normal(matrix3f$normal, pNormalX, pNormalY, pNormalZ).endVertex();
    }

    private void dummyPress() {}
}
