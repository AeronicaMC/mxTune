package aeronicamc.mods.mxtune.render;

import net.minecraft.client.renderer.texture.DynamicTexture;

import java.util.Objects;

public class PixelFu
{
    private final DynamicTexture texture;
    private final int width;
    private final int height;
    public PixelFu(DynamicTexture texture)
    {
        this.texture = texture;
        this.width = texture.getPixels() != null ? texture.getPixels().getWidth() : 0;
        this.height = texture.getPixels() != null ? texture.getPixels().getHeight() : 0;
    }

    public boolean isReady()
    {
        return this.texture.getPixels() != null;
    }

    public void upload()
    {
        this.texture.upload();
    }

    public void pixel(int x, int y, int rgbaColor)
    {
        if ((x >= 0) && (x <= this.width-1) && (y >= 0) && (y <= this.height - 1))
            Objects.requireNonNull(this.texture.getPixels()).setPixelRGBA(x, y, rgbaColor);
    }

    public void fillRect(int x, int y, int width, int height, int rgbaColor)
    {
        Objects.requireNonNull(this.texture.getPixels()).fillRect(x, y, width, height, rgbaColor);
    }

    public void border(int x0, int y0, int width, int height, int thickness, int rgbaColor)
    {
        if (thickness > 0)
        {
            for (int i = 0; i < thickness; i++)
            {
                // top
                plotLine(x0 + i, y0 + i, width - 1 - i , y0 + i, rgbaColor);
                // bottom
                plotLine(x0 + i,height - 1 - i, width - 1 - i, height - 1 -i, rgbaColor);
                // left
                plotLine(x0 + i,y0 + i, x0 + i, height - 1 - i, rgbaColor);
                // right
                plotLine(width - 1 - i, y0 + i, width - 1 - i, height - 1 - i, rgbaColor);
            }
        }
    }


    // Bresenham's line algorithm https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
    public void plotLine(int x0, int y0, int x1, int y1, int rgbaColor)
    {
        int dx = Math.abs(x1 - x0);
        int sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0);
        int sy = y0 < y1 ? 1 : -1;
        int error = dx + dy;

        if (this.isReady())
        {
            while (true)
            {
                this.pixel(x0, y0, rgbaColor);
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
}
