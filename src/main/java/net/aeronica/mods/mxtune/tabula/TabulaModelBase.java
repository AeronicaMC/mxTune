package net.aeronica.mods.mxtune.tabula;

import java.util.ArrayList;
import java.util.List;

import net.aeronica.mods.mxtune.tabula.components.TabulaAnimation;
import net.aeronica.mods.mxtune.tabula.components.TabulaCubeContainer;
import net.aeronica.mods.mxtune.tabula.components.TabulaCubeGroupContainer;
import net.aeronica.mods.mxtune.tabula.components.TabulaModelContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

/**
 * Direct from iChunUtil sources, but modified for my class names and texture handling
 * 
 * Model class used to render a ProjectInfo (Tabula format) in game. Not meant to be discarded by GC, no memory freeing is done in this class.
 * Meant to used on Projects which have a texture as well.
 * If you want to use a Model you can discard and recreate without leaking memory, use ModelBaseDummy in the same package.
 */
public class TabulaModelBase extends ModelBase
{

    public final TabulaModelContainer tabulaModelInfo;
    public List<TabulaCubeContainer> cubes;

    public TabulaModelBase(TabulaModelContainer tabulaModelData)
    {
        this.tabulaModelInfo = tabulaModelData;
        this.textureHeight = tabulaModelInfo.getTextureHeight();
        this.textureWidth = tabulaModelInfo.getTextureWidth();

        this.cubes = new ArrayList<TabulaCubeContainer>();

        for(int i = 0; i < tabulaModelInfo.getCubeGroups().size(); i++)
        {
            createGroupCubes(tabulaModelInfo.getCubeGroups().get(i));
        }
        for(int i = 0 ; i < tabulaModelInfo.getCubes().size(); i++)
        {
            tabulaModelInfo.getCubes().get(i).createModel(this);
            cubes.add(tabulaModelInfo.getCubes().get(i));
        }
    }

    public TabulaModelContainer getTabulaModelInfo()
    {
        return tabulaModelInfo;
    }

    @Override
    public void render(Entity ent, float f, float f1, float f2, float f3, float f4, float f5)
    {
        render(f5, false, false, 1F, 1F, 1F, 1F);
    }

    public void render(float f5, boolean useTexture, boolean useOpacity)
    {
        render(f5, useTexture, useOpacity, 1F, 1F, 1F, 1F);
    }

    public void render(float f5, boolean useTexture, boolean useOpacity, float r, float g, float b, float alpha)
    {
//        if(useTexture && projectInfo.bufferedTexture != null)
//        {
//            if(projectInfo.bufferedTextureId == -1)
//            {
//                projectInfo.bufferedTextureId = TextureUtil.uploadTextureImage(TextureUtil.glGenTextures(), projectInfo.bufferedTexture);
//            }
//            GlStateManager.bindTexture(projectInfo.bufferedTextureId);
//        }
        float renderTicks = Minecraft.getMinecraft().getRenderPartialTicks();
        AnimationUtils.applyModelAnimations(tabulaModelInfo, renderTicks); // FIXME Bad way to get partialTicks?!?!
        GlStateManager.pushMatrix();
        GlStateManager.scale(1D / tabulaModelInfo.getScale()[0], 1D / tabulaModelInfo.getScale()[1], 1D / tabulaModelInfo.getScale()[2]);

        for(TabulaCubeContainer info : cubes)
        {
            if(info.getModelCube() != null && !info.isHidden())
            {
                GlStateManager.pushMatrix();
                if(useOpacity)
                {
                    GlStateManager.color(r, g, b, alpha * (float)(info.opacity / 100D));
                }

                if(!(info.scale[0] == 1D && info.scale[1] == 1D && info.scale[2] == 1D))
                {
                    GlStateManager.translate(info.getModelCube().offsetX, info.getModelCube().offsetY, info.getModelCube().offsetZ);
                    GlStateManager.translate(info.getModelCube().rotationPointX * f5, info.getModelCube().rotationPointY * f5, info.getModelCube().rotationPointZ * f5);
                    GlStateManager.scale(info.scale[0], info.scale[1], info.scale[2]);
                    GlStateManager.translate(-info.getModelCube().offsetX, -info.getModelCube().offsetY, -info.getModelCube().offsetZ);
                    GlStateManager.translate(-info.getModelCube().rotationPointX * f5, -info.getModelCube().rotationPointY * f5, -info.getModelCube().rotationPointZ * f5);
                }

                info.getModelCube().render(f5);

                GlStateManager.popMatrix();
            }
        }

        GlStateManager.popMatrix();
        AnimationUtils.resetModelAnimations(tabulaModelInfo, renderTicks); // FIXME Bad way to get partialTicks?!?!
        for(TabulaAnimation anim : tabulaModelInfo.getAnims())
        {
            anim.update();
        }
    }

//    public void bindTexture(BufferedImage image)
//    {
//        projectInfo.bufferedTexture = image;
//        projectInfo.bufferedTextureId = TextureUtil.uploadTextureImage(TextureUtil.glGenTextures(), projectInfo.bufferedTexture);
//    }

    private void createGroupCubes(TabulaCubeGroupContainer group)
    {
        for(int i = 0; i < group.getCubeGroups().size(); i++)
        {
            createGroupCubes(group.getCubeGroups().get(i));
        }
        for(int i = 0; i < group.getCubes().size(); i++)
        {
            group.getCubes().get(i).createModel(this);
            cubes.add(group.getCubes().get(i)); 
        }
    }
}
