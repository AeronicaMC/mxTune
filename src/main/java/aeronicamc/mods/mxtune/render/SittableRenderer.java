package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class SittableRenderer extends EntityRenderer<MusicSourceEntity>
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
    public ResourceLocation getTextureLocation(MusicSourceEntity pEntity)
    {
        return null;
    }
}
