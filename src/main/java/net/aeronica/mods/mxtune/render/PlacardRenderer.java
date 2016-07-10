/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.render;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class PlacardRenderer
{
    private static final int PLAC_ICON_SIZE = 18;
    private static final int PLAC_ICON_BASE_U_OFFSET = 0;
    private static final int PLAC_ICON_BASE_V_OFFSET = 165;
    private static final int PLAC_ICONS_PER_ROW = 8;
    private static final int PLAC_TEXTURE_SIZE = 256;
    /*
     * this.drawTexturedModalRect(xPos, yPos, PLAC_ICON_BASE_U_OFFSET + index %
     * PLAC_ICONS_PER_ROW * PLAC_ICON_SIZE, PLAC_ICON_BASE_V_OFFSET + index /
     * PLAC_ICONS_PER_ROW * PLAC_ICON_SIZE, PLAC_ICON_SIZE, PLAC_ICON_SIZE);
     */

    private final ResourceLocation placardTextures = new ResourceLocation(MXTuneMain.prependModID("textures/gui/manage_group.png"));
    private final float PLACARD_RANGE = 32.0f;
    private int index = 0;

    private PlacardRenderer() {}

    private static class PlacardRendererHolder {private static final PlacardRenderer INSTANCE = new PlacardRenderer();}

    public static PlacardRenderer getInstance() {return PlacardRendererHolder.INSTANCE;}

    public void setPlacard(int index) {this.index = index;}

    public void doRender(net.minecraftforge.client.event.RenderLivingEvent.Specials.Post<EntityLivingBase> event)
    {
        RenderLivingBase<?> renderer = (RenderLivingBase<?>) event.getRenderer().getRenderManager().getEntityRenderObject(event.getEntity());
        renderer.bindTexture(placardTextures);

        double d0 = event.getEntity().getDistanceSqToEntity(renderer.getRenderManager().renderViewEntity);
        if (d0 <= (double) (this.PLACARD_RANGE * this.PLACARD_RANGE))
        {
            double placardHeight = event.getEntity().isSneaking() ? 0.22d : 0.35d;

            double f2 = (double) (PLAC_ICON_BASE_U_OFFSET + index % PLAC_ICONS_PER_ROW * PLAC_ICON_SIZE) / PLAC_TEXTURE_SIZE;
            double f3 = (double) (PLAC_ICON_BASE_U_OFFSET + index % PLAC_ICONS_PER_ROW * PLAC_ICON_SIZE + PLAC_ICON_SIZE) / PLAC_TEXTURE_SIZE;
            double f4 = (double) (PLAC_ICON_BASE_V_OFFSET + index / PLAC_ICONS_PER_ROW * PLAC_ICON_SIZE) / PLAC_TEXTURE_SIZE;
            double f5 = (double) (PLAC_ICON_BASE_V_OFFSET + index / PLAC_ICONS_PER_ROW * PLAC_ICON_SIZE + PLAC_ICON_SIZE) / PLAC_TEXTURE_SIZE;

            GlStateManager.pushMatrix();
            GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.disableLighting();

            GlStateManager.translate(event.getX(), event.getY() + event.getEntity().height + placardHeight + 0.25D, event.getZ());

            GlStateManager.rotate(-renderer.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) (renderer.getRenderManager().options.thirdPersonView == 2 ? -1 : 1) * renderer.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

            float f11 = 0.5F;
            GlStateManager.scale(f11, f11, f11);

            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();

            double w = 0.5D;
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(w, 0, 0).tex(f2, f5).endVertex(); // 1
            vertexbuffer.pos(-w, 0, 0).tex(f3, f5).endVertex(); // 2
            vertexbuffer.pos(-w, 1, 0).tex(f3, f4).endVertex(); // 3
            vertexbuffer.pos(w, 1, 0).tex(f2, f4).endVertex(); // 4
            Tessellator.getInstance().draw();

            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }
}
