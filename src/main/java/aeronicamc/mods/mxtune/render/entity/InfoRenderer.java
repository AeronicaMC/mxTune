package aeronicamc.mods.mxtune.render.entity;

import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

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

    public @Nullable IVertexBuilder getInfoRenderVertexBuilder(IRenderTypeBuffer renderTypeBuffer, MusicVenueInfoEntity infoEntity){
        if (infoEntity == null) return null;
        InfoRenderer.Instance rendererInstance = this.infoRendererInstances.get(infoEntity.getId());

        if (rendererInstance == null)
        {
            createInfoRendererInstance(infoEntity);
            System.out.printf("***** CREATED INFO-RENDERER-INSTANCE %s\n", this.infoRendererInstances.size());
            return null;
        }

        return rendererInstance.getInfoRenderVertexBuilder(renderTypeBuffer);
    }

    public void updateInfoTexture(MusicVenueInfoEntity infoEntity) {
        InfoRenderer.Instance infoRendererInstance = this.infoRendererInstances.get(infoEntity.getId());
        if (infoRendererInstance != null)
            infoRendererInstance.updateInfoTexture(infoEntity);
    }

    public void updateAll()
    {
        infoRendererInstances.forEach((key, value) -> value.updateInfoTexture());
    }

    private void createInfoRendererInstance(MusicVenueInfoEntity infoEntity) {
        InfoRenderer.Instance infoRendererInstance = new InfoRenderer.Instance(infoEntity);
        infoRendererInstance.updateInfoTexture(infoEntity);
        this.infoRendererInstances.put(infoEntity.getId(), infoRendererInstance);
    }

    public void clearInfoRendererInstances()
    {
        infoRendererInstances.clear();
    }

    public void close(MusicVenueInfoEntity infoEntity)
    {
        infoRendererInstances.remove(infoEntity.getId());
        System.out.printf("***** REMOVED INFO-RENDERER-INSTANCE %s\n", this.infoRendererInstances.size());
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

        private void updateInfoTexture()
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

        private IVertexBuilder getInfoRenderVertexBuilder(IRenderTypeBuffer renderTypeBuffer)
        {
            return renderTypeBuffer.getBuffer(this.renderType);
        }

        @Override
        public void close() throws Exception
        {
            this.infoTexture.close();
        }
    }
}
