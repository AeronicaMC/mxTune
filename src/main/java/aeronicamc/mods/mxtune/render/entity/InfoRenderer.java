package aeronicamc.mods.mxtune.render.entity;

import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

public class InfoRenderer implements AutoCloseable
{
    private static InfoRenderer instance;
    private final TextureManager textureManager;
    private final Map<Integer, InfoRenderer.Instance> infoRendererInstances = Maps.newHashMap();

    public InfoRenderer(TextureManager textureManager)
    {
        this.textureManager = textureManager;
        instance = this;
    }

    public static InfoRenderer getInstance()
    {
        return instance;
    }

    // stuff to handle a never saved dynamic texture for drawing

    public void renderInfo(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, MusicVenueInfoEntity infoEntity, int combinedLight)
    {
        if (infoEntity == null) return;

        InfoRenderer.Instance rendererInstance = this.getInfoRendererInstance(infoEntity, false);

        if (rendererInstance == null)
        {
            createInfoRendererInstance(infoEntity);
            System.out.println("***** CREATED INFO-RENDERER-INSTANCE");
            return;
        }
        rendererInstance.render(matrixStack, renderTypeBuffer, combinedLight);
    }

    public void updateInfoTexture(MusicVenueInfoEntity infoEntity) {
        this.getInfoRendererInstance(infoEntity, true).updateInfoTexture(infoEntity);
    }

    // FIXME
    private @Nullable InfoRenderer.Instance getInfoRendererInstance(MusicVenueInfoEntity infoEntity, boolean create) {
        InfoRenderer.Instance infoRendererInstance = this.infoRendererInstances.get(infoEntity.getId());

        if (create && infoRendererInstance == null) {
            this.createInfoRendererInstance(infoEntity);
            this.updateInfoTexture(infoEntity);
        }

        return infoRendererInstance;
    }

    // FIXME
    private void createInfoRendererInstance(MusicVenueInfoEntity infoEntity) {
        InfoRenderer.Instance infoRendererInstance = new InfoRenderer.Instance(
                infoEntity);
        infoRendererInstance.updateInfoTexture(infoEntity);
        this.infoRendererInstances.put(infoEntity.getId(), infoRendererInstance);
        System.out.printf("***** %s infoRenderInstances\n", this.infoRendererInstances.size());
    }

    public void clearInfoRendererInstances()
    {
        infoRendererInstances.clear();
    }

    public void close(MusicVenueInfoEntity infoEntity)
    {
        infoRendererInstances.remove(infoEntity.getId());
    }

    @Override
    public void close() throws Exception
    {
        this.clearInfoRendererInstances();
    }

    class Instance implements AutoCloseable
    {
        private final MusicVenueInfoEntity infoEntity;
        private final DynamicTexture infoTexture;
        private final RenderType renderType;
        private final Random random = new Random();

        private final int width;
        private final int height;

        private Instance(MusicVenueInfoEntity infoEntity)
        {
            this.infoEntity = infoEntity;
            this.width = infoEntity.getWidth();
            this.height = infoEntity.getWidth();
            this.infoTexture = new DynamicTexture(infoEntity.getWidth(), infoEntity.getHeight(), true);
            ResourceLocation dynamicTextureLocation = InfoRenderer.this.textureManager.register("info/" + infoEntity.getId(), this.infoTexture);
            this.renderType = RenderType.text(dynamicTextureLocation);
        }

        // FIXME
        private void updateInfoTexture(MusicVenueInfoEntity infoEntity)
        {
            for(int pixelY = 0; pixelY < infoEntity.getHeight(); pixelY++) {
                for(int pixelX = 0; pixelX < infoEntity.getWidth(); pixelX++) {
                    int color = randomColor();
                    this.infoTexture.getPixels().setPixelRGBA(pixelX, pixelY, color);
                }
            }
            this.infoTexture.upload();
        }

        private int randomColor()
        {
            int x = random.nextInt();
            return ((0xFF000000)) |       //AA______
                    ((x & 0x00FF0000) >> 16) | //______RR
                    ((x & 0x0000FF00)) |       //____GG__
                    ((x & 0x000000FF) << 16);  //__BB____
        }

        private void render(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int combinedLight)
        {
            Matrix4f matrix4f = matrixStack.last().pose();
            float pZ = 0F;
            IVertexBuilder ivertexbuilder = renderTypeBuffer.getBuffer(this.renderType);
            ivertexbuilder.vertex(matrix4f, 0F, (float) this.height, pZ).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(combinedLight).endVertex();
            ivertexbuilder.vertex(matrix4f, (float) this.width, (float) this.height, pZ).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(combinedLight).endVertex();
            ivertexbuilder.vertex(matrix4f, (float) this.width, 0F, pZ).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(combinedLight).endVertex();
            ivertexbuilder.vertex(matrix4f, 0F, 0F, pZ).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(combinedLight).endVertex();
        }


        @Override
        public void close() throws Exception
        {
            this.infoTexture.close();
        }
    }
}
