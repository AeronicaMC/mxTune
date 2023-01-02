package aeronicamc.mods.mxtune.render.entity;

import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
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
import net.minecraft.entity.item.PaintingType;
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
        PaintingType paintingtype = PaintingType.BOMB;
        float f = 0.0625F;
        pMatrixStack.scale(0.0625F, 0.0625F, 0.0625F);
        IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RenderType.entitySolid(this.getTextureLocation(pEntity)));
        PaintingSpriteUploader paintingspriteuploader = Minecraft.getInstance().getPaintingTextures();
        this.renderPainting(pMatrixStack, ivertexbuilder, pEntity, paintingtype.getWidth(), paintingtype.getHeight(), paintingspriteuploader.get(paintingtype), paintingspriteuploader.getBackSprite());
        pMatrixStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(MusicVenueInfoEntity pEntity) {
        return Minecraft.getInstance().getPaintingTextures().getBackSprite().atlas().location();
    }

    private void renderPainting(MatrixStack p_229122_1_, IVertexBuilder p_229122_2_, MusicVenueInfoEntity p_229122_3_, int p_229122_4_, int p_229122_5_, TextureAtlasSprite p_229122_6_, TextureAtlasSprite p_229122_7_) {
        MatrixStack.Entry matrixstack$entry = p_229122_1_.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();
        float f = (float)(-p_229122_4_) / 2.0F;
        float f1 = (float)(-p_229122_5_) / 2.0F;
        float f2 = 0.5F;
        float f3 = p_229122_7_.getU0();
        float f4 = p_229122_7_.getU1();
        float f5 = p_229122_7_.getV0();
        float f6 = p_229122_7_.getV1();
        float f7 = p_229122_7_.getU0();
        float f8 = p_229122_7_.getU1();
        float f9 = p_229122_7_.getV0();
        float f10 = p_229122_7_.getV(1.0D);
        float f11 = p_229122_7_.getU0();
        float f12 = p_229122_7_.getU(1.0D);
        float f13 = p_229122_7_.getV0();
        float f14 = p_229122_7_.getV1();
        int i = p_229122_4_ / 16;
        int j = p_229122_5_ / 16;
        double d0 = 16.0D / (double)i;
        double d1 = 16.0D / (double)j;

        for(int k = 0; k < i; ++k) {
            for(int l = 0; l < j; ++l) {
                float f15 = f + (float)((k + 1) * 16);
                float f16 = f + (float)(k * 16);
                float f17 = f1 + (float)((l + 1) * 16);
                float f18 = f1 + (float)(l * 16);
                int i1 = MathHelper.floor(p_229122_3_.getX());
                int j1 = MathHelper.floor(p_229122_3_.getY() + (double)((f17 + f18) / 2.0F / 16.0F));
                int k1 = MathHelper.floor(p_229122_3_.getZ());
                Direction direction = p_229122_3_.getDirection();
                if (direction == Direction.NORTH) {
                    i1 = MathHelper.floor(p_229122_3_.getX() + (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.WEST) {
                    k1 = MathHelper.floor(p_229122_3_.getZ() - (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.SOUTH) {
                    i1 = MathHelper.floor(p_229122_3_.getX() - (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.EAST) {
                    k1 = MathHelper.floor(p_229122_3_.getZ() + (double)((f15 + f16) / 2.0F / 16.0F));
                }

                int l1 = WorldRenderer.getLightColor(p_229122_3_.level, new BlockPos(i1, j1, k1));
                float f19 = p_229122_6_.getU(d0 * (double)(i - k));
                float f20 = p_229122_6_.getU(d0 * (double)(i - (k + 1)));
                float f21 = p_229122_6_.getV(d1 * (double)(j - l));
                float f22 = p_229122_6_.getV(d1 * (double)(j - (l + 1)));
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f18, f20, f21, -0.5F, 0, 0, -1, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f18, f19, f21, -0.5F, 0, 0, -1, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f17, f19, f22, -0.5F, 0, 0, -1, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f17, f20, f22, -0.5F, 0, 0, -1, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f17, f3, f5, 0.5F, 0, 0, 1, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f17, f4, f5, 0.5F, 0, 0, 1, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f18, f4, f6, 0.5F, 0, 0, 1, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f18, f3, f6, 0.5F, 0, 0, 1, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f17, f7, f9, -0.5F, 0, 1, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f17, f8, f9, -0.5F, 0, 1, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f17, f8, f10, 0.5F, 0, 1, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f17, f7, f10, 0.5F, 0, 1, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f18, f7, f9, 0.5F, 0, -1, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f18, f8, f9, 0.5F, 0, -1, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f18, f8, f10, -0.5F, 0, -1, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f18, f7, f10, -0.5F, 0, -1, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f17, f12, f13, 0.5F, -1, 0, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f18, f12, f14, 0.5F, -1, 0, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f18, f11, f14, -0.5F, -1, 0, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f15, f17, f11, f13, -0.5F, -1, 0, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f17, f12, f13, -0.5F, 1, 0, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f18, f12, f14, -0.5F, 1, 0, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f18, f11, f14, 0.5F, 1, 0, 0, l1);
                this.vertex(matrix4f, matrix3f, p_229122_2_, f16, f17, f11, f13, 0.5F, 1, 0, 0, l1);
            }
        }

    }

    private void vertex(Matrix4f p_229121_1_, Matrix3f p_229121_2_, IVertexBuilder p_229121_3_, float p_229121_4_, float p_229121_5_, float p_229121_6_, float p_229121_7_, float p_229121_8_, int p_229121_9_, int p_229121_10_, int p_229121_11_, int p_229121_12_) {
        p_229121_3_.vertex(p_229121_1_, p_229121_4_, p_229121_5_, p_229121_8_).color(255, 255, 255, 255).uv(p_229121_6_, p_229121_7_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_229121_12_).normal(p_229121_2_, (float)p_229121_9_, (float)p_229121_10_, (float)p_229121_11_).endVertex();
    }
}
