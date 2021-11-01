package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.entity.SittableEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class SittableRenderer extends EntityRenderer<SittableEntity>
{
    public SittableRenderer(EntityRendererManager rendererManager)
    {
        super(rendererManager);
    }

    /**
     * Returns the location of an entity's texture.
     *
     * @param pEntity
     */
    @Override
    public ResourceLocation getTextureLocation(SittableEntity pEntity)
    {
        return null;
    }
}
