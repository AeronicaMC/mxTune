/*
    The MIT License (MIT)

    Copyright (c) 2016

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */
package aeronicamc.mods.mxtune.render;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

import java.util.OptionalDouble;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL12.GL_QUADS;

public class ModRenderType extends RenderType
{
    @SuppressWarnings("unused")
    private ModRenderType(String name, VertexFormat vertexFormat, int pi3, int pi4, boolean pi5, boolean pb6, Runnable runnable7, Runnable runnable8)
    {
        super(name, vertexFormat, pi3, pi4, pi5, pb6, runnable7, runnable8);
    }

    public static final int FULL_BRIGHT_LIGHT_MAP = (0xF << 4) | (0xF << 20);

    private static final LineState FAT_LINES = new LineState(OptionalDouble.of(4.0D));

    public static final RenderType THICK_LINES = create("thick_select_lines",
                                                          DefaultVertexFormats.POSITION_COLOR, GL_LINES, 256,
                                                          State.builder().setLineState(FAT_LINES)
                                                                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                                                                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                                                .setOutputState(ITEM_ENTITY_TARGET)
                                                                .setWriteMaskState(COLOR_DEPTH_WRITE)
                                                                .createCompositeState(false));

    @SuppressWarnings("unused")
    public static final RenderType OVERLAY_LINES = create("overlay_lines",
                                                          DefaultVertexFormats.POSITION_COLOR, GL_LINES, 256,
                                                          State.builder().setLineState(FAT_LINES)
                                                                  .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                                                                  .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                                                  .setTextureState(NO_TEXTURE)
                                                                  .setDepthTestState(NO_DEPTH_TEST)
                                                                  .setCullState(NO_CULL)
                                                                  .setLightmapState(NO_LIGHTMAP)
                                                                  .setWriteMaskState(COLOR_WRITE)
                                                                  .createCompositeState(false));

    public static final RenderType TRANSPARENT_QUADS_NO_TEXTURE = create("transparent_quads_no_texture",
                                                                         DefaultVertexFormats.POSITION_COLOR, GL_QUADS, 256,
                                                                         State.builder()
                                                                                 .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                                                                                 .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                                                                 .setTextureState(NO_TEXTURE)
                                                                                 //.setDepthTestState(NO_DEPTH_TEST)
                                                                                 .setCullState(NO_CULL)
                                                                                 .setLightmapState(NO_LIGHTMAP)
                                                                                 .setWriteMaskState(COLOR_WRITE)
                                                                                 .createCompositeState(true));
}
