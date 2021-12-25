package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import aeronicamc.mods.mxtune.util.Misc;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class MusicSourceRenderer extends EntityRenderer<MusicSourceEntity>
{
    public MusicSourceRenderer(EntityRendererManager rendererManager)
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
    public ResourceLocation getTextureLocation(@Nullable MusicSourceEntity pEntity) { return Misc.nonNullInjected(); }
}
