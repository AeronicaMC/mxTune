package aeronicamc.mods.mxtune.render.entity;

import aeronicamc.mods.mxtune.caps.venues.EntityVenueState;
import aeronicamc.mods.mxtune.caps.venues.MusicVenue;
import aeronicamc.mods.mxtune.caps.venues.MusicVenueHelper;
import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
import aeronicamc.mods.mxtune.render.PixelFu;
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
            return null;
        }

        return rendererInstance.getInfoRenderVertexBuilder(renderTypeBuffer);
    }

    public void updateInfoTexture(MusicVenueInfoEntity infoEntity) {
        InfoRenderer.Instance infoRendererInstance = this.infoRendererInstances.get(infoEntity.getId());
        if (infoRendererInstance != null)
            infoRendererInstance.updateInfoTexture(infoEntity);
    }

    public boolean inVenue(MusicVenueInfoEntity infoEntity)
    {
        boolean result = false;
        InfoRenderer.Instance infoRendererInstance = this.infoRendererInstances.get(infoEntity.getId());
        if (infoRendererInstance != null)
            result = infoRendererInstance.inVenue();
        return result;
    }

    public MusicVenue getVenue(MusicVenueInfoEntity infoEntity)
    {
        InfoRenderer.Instance infoRendererInstance = this.infoRendererInstances.get(infoEntity.getId());
        if (infoRendererInstance != null)
            return infoRendererInstance.getVenue();
        else
            return MusicVenue.EMPTY;
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
        private final PixelFu pixelFu;
        private EntityVenueState sourceVenueState = EntityVenueState.INVALID;
        private int count = 0;

        private Instance(MusicVenueInfoEntity pInfoEntity)
        {
            this.entityId = pInfoEntity.getId();
            this.texWidth = pInfoEntity.getWidth() * 4;
            this.texHeight = pInfoEntity.getHeight() * 4;
            this.infoTexture = new DynamicTexture(this.texWidth, this.texHeight, true);
            this.dynamicTextureLocation = InfoRenderer.this.textureManager.register("info/" + pInfoEntity.getId(), this.infoTexture);
            this.renderType = RenderType.text(dynamicTextureLocation);
            this.count = pInfoEntity.isNewPanel() ? 1 : 0;
            this.pixelFu = new PixelFu(infoTexture);
        }

        private void updateInfoTexture(MusicVenueInfoEntity infoEntity) {
            if (count++ % 20 == 0)
                this.sourceVenueState = MusicVenueHelper.getEntityVenueState(infoEntity.level, infoEntity.getId());
            if (sourceVenueState.inVenue())
                colorBars();
            else
                randomBoxes();//noiseFill();
        }

        private void noiseFill() {
            if (pixelFu.isReady()) {
                for (int pixelY = 0; pixelY < this.texHeight; pixelY++) {
                    for (int pixelX = 0; pixelX < this.texWidth; pixelX++) {
                        int color = randomColor();
                        pixelFu.pixel(pixelX, pixelY, color);
                    }
                }
                pixelFu.upload();
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
            if (pixelFu.isReady()) {
                int x = 0;
                for (int xBar = 0; xBar < 8; xBar++) {
                    int color = getABGR(upperBars[xBar][0], upperBars[xBar][1], upperBars[xBar][2]);
                    pixelFu.fillRect(x, 0, barWidth , upperBarHeight, color);

                    color = getABGR(lowerBars[xBar][0], lowerBars[xBar][1], lowerBars[xBar][2]);
                    pixelFu.fillRect(x, upperBarHeight, barWidth , texHeight - upperBarHeight, color);
                    x += barWidth;
                }
                pixelFu.border(0,0, this.texWidth, this.texHeight, 2, getABGR(0xCC, 0x00, 0x00));
                pixelFu.upload();
            }
        }

        private void randomBoxes() {
            if (pixelFu.isReady())
            {
                pixelFu.fillRect(0, 0, texWidth, texHeight, getABGR(0, 0, 0));
                for (int i = 0; i < 16; i += 2)
                {
                    int x1 = random.nextInt(texWidth/2);
                    int y1 = random.nextInt(texHeight/2);
                    int x2 = random.nextInt(texWidth/2) + texWidth/2;
                    int y2 = random.nextInt(texHeight/2) + texHeight/2;
                    //pixelFu.plotLine(x1, y1, x2, y2, randomColor());
                    pixelFu.border(x1, y1, x2, y2, 2, randomColor());
                    //pixelFu.border(i, i , texWidth - i, texHeight - i, 1, randomColor());
                }

                pixelFu.upload();
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

        public boolean inVenue()
        {
            return sourceVenueState.inVenue();
        }

        public MusicVenue getVenue()
        {
            return sourceVenueState.getVenue();
        }

        @Override
        public void close() throws Exception
        {
            this.infoTexture.close();
            InfoRenderer.getInstance().textureManager.release(dynamicTextureLocation);
        }
    }
}
