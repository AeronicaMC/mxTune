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
            infoRendererInstance.updateInfoTexture();
    }

    public void updateAll()
    {
        infoRendererInstances.forEach((key, value) -> value.updateInfoTexture());
    }

    private void createInfoRendererInstance(MusicVenueInfoEntity infoEntity) {
        InfoRenderer.Instance infoRendererInstance = new InfoRenderer.Instance(infoEntity);
        infoRendererInstance.updateInfoTexture();
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
        private final DynamicTexture infoTexture;
        private final RenderType renderType;
        private final Random random = new Random();
        private final int entityId;
        private final int texWidth;
        private final int texHeight;
        private final ResourceLocation dynamicTextureLocation;

        private Instance(MusicVenueInfoEntity pInfoEntity)
        {
            this.entityId = pInfoEntity.getHeight();
            this.texWidth = pInfoEntity.getWidth() * 4;
            this.texHeight = pInfoEntity.getHeight() * 4;
            this.infoTexture = new DynamicTexture(this.texWidth, this.texHeight, true);
            this.dynamicTextureLocation = InfoRenderer.this.textureManager.register("info/" + pInfoEntity.getId(), this.infoTexture);
            this.renderType = RenderType.text(dynamicTextureLocation);
        }

        private void updateInfoTexture() {
            // noiseFill();
            colorBars();
        }

        private void noiseFill() {
            if (this.infoTexture.getPixels() != null) {
                for (int pixelY = 0; pixelY < this.texHeight; pixelY++) {
                    for (int pixelX = 0; pixelX < this.texWidth; pixelX++) {
                        int color = randomColor();
                        this.infoTexture.getPixels().setPixelRGBA(pixelX, pixelY, color);
                    }
                }
                this.infoTexture.upload();
            }
        }

        int[][] upperBars = {
                { 104, 104, 104 },  // 40% gray
                { 180, 180, 180 },  // 75% white
                { 180, 180, 16  },  // 75% yellow
                { 16,  180, 180 },  // 75% cyan
                { 16,  180, 16  },  // 75% green
                { 180, 16,  180 },  // 75% magenta
                { 180, 16,  16  },  // 75% red
                { 16,  16,  180 },  // 75% blue
        };

        int[][] lowerBars = {
                { 16,  16,  16  },  // 40% black
                { 235, 235, 235 },  // 100% white
                { 72,  16,  118 },  // +Q
                { 106, 52,  16  },  // +I
                { 16,  70,  106 },  // -I
                { 0,   0,   0   },  // FULL black
                { 255, 255, 255 },  // FULL white
                { 255, 255, 0   },  // FULL yellow
        };

        private void colorBars() {
            int barWidth = this.texWidth / 8;
            int upperBarHeight = (this.texHeight / 8) * 6;
            if (this.infoTexture.getPixels() != null) {
                int x = 0;
                for (int xBar = 0; xBar < 8; xBar++) {
                    int color = getABGR(upperBars[xBar][0], upperBars[xBar][1], upperBars[xBar][2]);
                    this.infoTexture.getPixels().fillRect(x, 0, barWidth , upperBarHeight, color);

                    color = getABGR(lowerBars[xBar][0], lowerBars[xBar][1], lowerBars[xBar][2]);
                    this.infoTexture.getPixels().fillRect(x, upperBarHeight, barWidth , texHeight - upperBarHeight, color);
                    x += barWidth;
                }
                this.infoTexture.upload();
            }
        }

        private int getABGR(int r, int g, int b)
        {
            return  ((r & 0x000000FF) | ((g & 0x000000FF) << 8) | ((b & 0x000000FF) << 16) | (0xFF000000));
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

        private int getEntityId()
        {
            return entityId;
        }

        private int getTexWidth()
        {
            return texWidth;
        }

        private int getTexHeight()
        {
            return texHeight;
        }

        @Override
        public void close() throws Exception
        {
            this.infoTexture.close();
            InfoRenderer.getInstance().textureManager.release(dynamicTextureLocation);
        }
    }
}
