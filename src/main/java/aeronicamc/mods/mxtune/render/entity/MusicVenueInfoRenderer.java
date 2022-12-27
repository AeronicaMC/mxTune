package aeronicamc.mods.mxtune.render.entity;

import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class MusicVenueInfoRenderer extends EntityRenderer<MusicVenueInfoEntity>
{
    public MusicVenueInfoRenderer(EntityRendererManager rendererManager)
    {
        super(rendererManager);
    }

    @Override
    public void render(MusicVenueInfoEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight)
    {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    /**
     * Returns the location of an entity's texture.
     *
     * @param pEntity
     */
    @Override
    public ResourceLocation getTextureLocation(MusicVenueInfoEntity pEntity)
    {
        return null;
    }
}
