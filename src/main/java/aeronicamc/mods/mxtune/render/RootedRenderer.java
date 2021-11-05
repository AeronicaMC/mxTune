package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.entity.RootedEntity;
import aeronicamc.mods.mxtune.util.AntiNull;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RootedRenderer extends EntityRenderer<RootedEntity>
{
    public RootedRenderer(EntityRendererManager rendererManager)
    {
        super(rendererManager);
    }

    /**
     * Returns the location of an entity's texture.
     *
     * @param pEntity
     */
    @Nullable
    @Override
    public ResourceLocation getTextureLocation(@Nullable RootedEntity pEntity)
    {
        return AntiNull.nonNullInjected();
    }
}
