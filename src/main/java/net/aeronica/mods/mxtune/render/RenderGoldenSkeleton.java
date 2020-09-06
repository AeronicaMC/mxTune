package net.aeronica.mods.mxtune.render;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.entity.living.EntityGoldenSkeleton;
import net.aeronica.mods.mxtune.model.entity.ModelGoldenSkeleton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class RenderGoldenSkeleton extends RenderBiped<EntityGoldenSkeleton>
{
    private static final ResourceLocation SKELETON_TEXTURES = new ResourceLocation(Reference.MOD_ID, "textures/entity/skeleton/mob_golden_skeleton.png");
    
    public static final IRenderFactory<EntityGoldenSkeleton> FACTORY = (RenderManager manager) -> new net.aeronica.mods.mxtune.render.RenderGoldenSkeleton(manager);

    public RenderGoldenSkeleton(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelGoldenSkeleton(), 0.5F);
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerBipedArmor(this)
        {
            protected void initArmor()
            {
                this.modelLeggings = new ModelGoldenSkeleton(0.5F, true);
                this.modelArmor = new ModelGoldenSkeleton(1.0F, true);
            }
        });
    }

    public void transformHeldFull3DItemLayer()
    {
        GlStateManager.translate(0.09375F, 0.1875F, 0.0F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityGoldenSkeleton entity)
    {
        return SKELETON_TEXTURES;
    }
}
