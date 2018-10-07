/*
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

import net.aeronica.mods.mxtune.Reference;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class PlacardRenderer
{
    
    private static final int PLACARD_ICON_SIZE = 24;
    private static final int PLACARD_ICON_BASE_U_OFFSET = 54;
    private static final int PLACARD_ICON_BASE_V_OFFSET = 200;
    private static final int PLACARD_ICONS_PER_ROW = 8;
    private static final int PLACARD_TEXTURE_SIZE = 256;
    private static final ResourceLocation placardTextures = new ResourceLocation(Reference.MOD_ID, "textures/gui/status_widgets.png");
    private static final double PLACARD_RANGE = 32.0d;
    private int index = 0;

    private PlacardRenderer() {}

    private static class PlacardRendererHolder {
        private static final PlacardRenderer INSTANCE = new PlacardRenderer();
        private PlacardRendererHolder() {}
    }

    public static PlacardRenderer getInstance() {return PlacardRendererHolder.INSTANCE;}

    public void setPlacard(int index) {this.index = index;}

    public void doRender(net.minecraftforge.client.event.RenderLivingEvent.Specials.Post<EntityLivingBase> event)
    {
        RenderLivingBase<?> renderer = (RenderLivingBase<?>) event.getRenderer().getRenderManager().getEntityRenderObject(event.getEntity());
        renderer.bindTexture(placardTextures);

        double d0 = event.getEntity().getDistanceSq(renderer.getRenderManager().renderViewEntity);
        if (d0 <= (PLACARD_RANGE * PLACARD_RANGE))
        {
            double placardHeight = event.getEntity().isSneaking() ? 0.22d : 0.35d;

            double f2 = (double) (PLACARD_ICON_BASE_U_OFFSET + index % PLACARD_ICONS_PER_ROW * PLACARD_ICON_SIZE) / PLACARD_TEXTURE_SIZE;
            double f3 = (double) (PLACARD_ICON_BASE_U_OFFSET + index % PLACARD_ICONS_PER_ROW * PLACARD_ICON_SIZE + PLACARD_ICON_SIZE) / PLACARD_TEXTURE_SIZE;
            double f4 = (double) (PLACARD_ICON_BASE_V_OFFSET + index / PLACARD_ICONS_PER_ROW * PLACARD_ICON_SIZE) / PLACARD_TEXTURE_SIZE;
            double f5 = (double) (PLACARD_ICON_BASE_V_OFFSET + index / PLACARD_ICONS_PER_ROW * PLACARD_ICON_SIZE + PLACARD_ICON_SIZE) / PLACARD_TEXTURE_SIZE;

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
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            double w = 0.5D;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(w, 0, 0).tex(f2, f5).endVertex(); // 1
            bufferbuilder.pos(-w, 0, 0).tex(f3, f5).endVertex(); // 2
            bufferbuilder.pos(-w, 1, 0).tex(f3, f4).endVertex(); // 3
            bufferbuilder.pos(w, 1, 0).tex(f2, f4).endVertex(); // 4
            Tessellator.getInstance().draw();

            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

}
