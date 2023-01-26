package aeronicamc.mods.mxtune.render.entity;

import aeronicamc.mods.mxtune.caps.venues.EntityVenueState;
import aeronicamc.mods.mxtune.caps.venues.MusicVenue;
import aeronicamc.mods.mxtune.caps.venues.MusicVenueHelper;
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
        System.out.printf("***** REMOVED INFO-RENDERER-INSTANCE %s\n", this.infoRendererInstances.size());
    }

    @Override
    public void close() throws Exception
    {
        System.out.printf("***** INFO-RENDERER-INSTANCES: REMOVING %s\n", this.infoRendererInstances.size());
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
        }

        private void updateInfoTexture(MusicVenueInfoEntity infoEntity) {
            if (count++ % 20 == 0)
                this.sourceVenueState = MusicVenueHelper.getEntityVenueState(infoEntity.level, infoEntity.getId());
            if (sourceVenueState.inVenue())
                colorBars();
            else
                randomLines();//noiseFill();
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
                // top
                plotLine(0,0, texWidth-1, 0, getABGR(0x192,0x0, 0x0));
                plotLine(0,1, texWidth-1, 1, getABGR(0x192,0x0, 0x0));
                // bottom
                plotLine(0,texHeight-1, texWidth-1, texHeight-1, getABGR(0x192,0x0, 0x0));
                plotLine(0,texHeight-2, texWidth-1, texHeight-2, getABGR(0x192,0x0, 0x0));
                // left
                plotLine(0,0, 0, texHeight-1, getABGR(0x192,0x0, 0x0));
                plotLine(1,0, 1, texHeight-1, getABGR(0x192,0x0, 0x0));
                // right
                plotLine(texWidth-1,0, texWidth-1, texHeight-1, getABGR(0x192,0x0, 0x0));
                plotLine(texWidth-2,0, texWidth-2, texHeight-1, getABGR(0x192,0x0, 0x0));
                this.infoTexture.upload();
            }
        }

        private void randomLines() {
            for (int i = 0; i < 64 ; i++)
            {
                int x1 = random.nextInt(texWidth);
                int y1 = random.nextInt(texHeight);
                int x2 = random.nextInt(texWidth);
                int y2 = random.nextInt(texHeight);
                plotLine(x1, y1, x2, y2, randomColor());
            }
            this.infoTexture.upload();
        }

        // Digital Differential Analyzer Line drawing Algorithm http://csis.pace.edu/~marchese/CG/Lect4/cg_l4.htm
        private void lineDDA (int x0, int y0, int xEnd, int yEnd, int color)
        {
            int dx = xEnd - x0,  dy = yEnd - y0,  steps,  k;
            float xIncrement, yIncrement, x = x0, y = y0;

            steps = Math.max(Math.abs(dx), Math.abs(dy));

            xIncrement = (float)dx / (float)steps;
            yIncrement = (float)dy / (float) steps;
            if (this.infoTexture.getPixels() != null)
            {
                this.infoTexture.getPixels().setPixelRGBA(Math.round(x), Math.round(y), color);

                for (k = 0; k < steps; k++)
                {

                    x += xIncrement;

                    y += yIncrement;
                    this.infoTexture.getPixels().setPixelRGBA(Math.round(x), Math.round(y), color);
                }
            }
        }

        // Bresenham's line algorithm https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
        private void plotLine(int x0, int y0, int x1, int y1, int color)
        {
            int dx = Math.abs(x1 - x0);
            int sx = x0 < x1 ? 1 : -1;
            int dy = -Math.abs(y1 - y0);
            int sy = y0 < y1 ? 1 : -1;
            int error = dx + dy;

            if (this.infoTexture.getPixels() != null)
            {
                while (true)
                {
                    this.infoTexture.getPixels().setPixelRGBA(x0, y0, color);
                    if (x0 == x1 && y0 == y1) break;
                    int e2 = 2 * error;
                    if (e2 >= dy)
                    {
                        if (x0 == x1) break;
                        error = error + dy;
                        x0 = x0 + sx;
                    }
                    if (e2 <= dx)
                    {
                        if (y0 == y1) break;
                        error = error + dx;
                        y0 = y0 + sy;
                    }
                }
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
            System.out.printf("***** REMOVED INFO-RENDERER-INSTANCE %s\n", this);
        }
    }
}
