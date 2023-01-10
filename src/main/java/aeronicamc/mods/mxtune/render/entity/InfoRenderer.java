package aeronicamc.mods.mxtune.render.entity;

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

public class InfoRenderer implements AutoCloseable
{
    private static InfoRenderer instance;
    private final TextureManager textureManager;
    private final Map<String, InfoRenderer.Instance> infoRendererInstances = Maps.newHashMap();

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

    public void renderInfo(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, String musicVenueId, int combinedLight)
    {
        if (musicVenueId == null) return;

        InfoRenderer.Instance rendererInstance = this.getInfoRendererInstance(musicVenueId, 16, 16, false);

        if (rendererInstance == null)
        {
            // FIXME queue texture update
            return;
        }

        rendererInstance.render(matrixStack, renderTypeBuffer, combinedLight);
    }

    public void updateInfoTexture(String musicVenueId, int width, int height) {
        this.getInfoRendererInstance(musicVenueId, width, height, true).updateInfoTexture(musicVenueId, width, height);
    }

    // FIXME
    private @Nullable InfoRenderer.Instance getInfoRendererInstance(String musicVenueId, int width, int height, boolean create) {
        InfoRenderer.Instance infoRendererInstance = this.infoRendererInstances.get(musicVenueId);

        if (create && infoRendererInstance == null) {
            this.createInfoRendererInstance(musicVenueId, width, height);
        }

        return infoRendererInstance;
    }

    // FIXME
    private void createInfoRendererInstance(String musicVenueId, int width, int height) {
        InfoRenderer.Instance infoRendererInstance = new InfoRenderer.Instance(
                musicVenueId, width, height);
        infoRendererInstance.updateInfoTexture(musicVenueId, width, height);
        this.infoRendererInstances.put(musicVenueId, infoRendererInstance);
    }

    public void clearInfoRendererInstances()
    {
        infoRendererInstances.clear();
    }

    @Override
    public void close() throws Exception
    {
        this.clearInfoRendererInstances();
    }

    class Instance implements AutoCloseable
    {
        private final String musicVenueId;
        private final DynamicTexture infoTexture;
        private final RenderType renderType;

        private final int width;
        private final int height;

        private Instance(String musicVenueId, int width, int height)
        {
            this.musicVenueId = musicVenueId;
            this.infoTexture = new DynamicTexture(width, height, true);
            ResourceLocation dynamicTextureLocation = InfoRenderer.this.textureManager.register("info/" + musicVenueId, this.infoTexture);
            this.renderType = RenderType.text(dynamicTextureLocation);

            this.width = width;
            this.height = height;
        }

        // FIXME
        private void updateInfoTexture(String musicVenueId, int width, int height)
        {

        }

        private void render(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int combinedLight)
        {
            Matrix4f matrix4f = matrixStack.last().pose();
            IVertexBuilder ivertexbuilder = renderTypeBuffer.getBuffer(this.renderType);

            ivertexbuilder.vertex(matrix4f, 0.0F, (float) this.height, 0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(combinedLight).endVertex();
            ivertexbuilder.vertex(matrix4f, (float) this.width, (float) this.height, 0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(combinedLight).endVertex();
            ivertexbuilder.vertex(matrix4f, (float) this.width, 0.0F, 0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(combinedLight).endVertex();
            ivertexbuilder.vertex(matrix4f, 0.0F, 0.0F, 0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(combinedLight).endVertex();
        }


        @Override
        public void close() throws Exception
        {
            this.infoTexture.close();
        }
    }
}
