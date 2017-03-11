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
package net.aeronica.mods.mxtune.blocks;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Function;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.init.IReBakeModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RendererPiano extends TileEntitySpecialRenderer<TilePiano> implements IReBakeModel
{
    double xMusicOffset = 0D, zMusicOffset = 0D;
    EnumFacing facing = EnumFacing.NORTH;
    double xRackOffset = 0D, zRackOffset = 0D;
    double xBenchOffset = 0D, zBenchOffset = 0D;
    /** Ordering index for D-U-N-S-W-E */
    float face[] = {0, 0, 90, 270, 180, 0, 0, 0};
    private IModel rackModel;
    private IBakedModel bakedRackModel;
    private IModel benchModel;
    private IBakedModel bakedBenchModel;

    @Override
    public void reBakeModel()
    {
        bakedRackModel = null;
        bakedBenchModel = null;   
    }
    
    private IBakedModel getRackBakedModel()
    {
        /** Since we cannot bake in preInit() we do lazy baking of the model as soon as we need it for rendering */
        if (bakedRackModel == null)
        {
            try
            {
                rackModel = ModelLoaderRegistry.getModel(new ResourceLocation(MXTuneMain.MODID, "block/piano_rack"));
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            bakedRackModel = rackModel.bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK, new Function<ResourceLocation, TextureAtlasSprite>()
            {
                @Override
                public TextureAtlasSprite apply(ResourceLocation location)
                {
                    return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
                }
            });
        }
        return bakedRackModel;
    }

    private IBakedModel getBenchBakedModel()
    {
        if (bakedBenchModel == null)
        {
            try
            {
                benchModel = ModelLoaderRegistry.getModel(new ResourceLocation(MXTuneMain.MODID, "block/piano_bench"));
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            bakedBenchModel = benchModel.bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK, new Function<ResourceLocation, TextureAtlasSprite>()
            {
                @Override
                public TextureAtlasSprite apply(ResourceLocation location)
                {
                    return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
                }
            });
        }
        return bakedBenchModel;
    }

    @Override
    public void renderTileEntityAt(TilePiano te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        /** Translations for the sheet music, music rack and bench */
        facing = te.getFacing();
        if (facing.equals(EnumFacing.NORTH))
        {
            /** Sheet Music (Item) translations */
            xMusicOffset = 0.0D;
            zMusicOffset = -0.5D;
            /** Rack (Block) translations */
            xRackOffset = 0.5D;
            zRackOffset = 0D;
            /** Bench (Block) translations */
            xBenchOffset = 1.375D;
            zBenchOffset = 0D;
        } else if (facing.equals(EnumFacing.SOUTH))
        {
            xMusicOffset = 0.0D;
            zMusicOffset = 0.5D;
            xRackOffset = 0.5D;
            zRackOffset = 1D;
            xBenchOffset = -0.375D;
            zBenchOffset = 1D;
        } else if (facing.equals(EnumFacing.EAST))
        {
            xMusicOffset = 0.5D;
            zMusicOffset = 0.0D;
            xRackOffset = 1D;
            zRackOffset = 0.5D;
            xBenchOffset = 1D;
            zBenchOffset = 1.375D;
        }
        if (facing.equals(EnumFacing.WEST))
        {
            xMusicOffset = -0.5D;
            zMusicOffset = 0.0D;
            xRackOffset = 0D;
            zRackOffset = 0.5D;
            xBenchOffset = 0D;
            zBenchOffset = -0.375D;
        }

        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        /** Translate to the location of our tile entity */
        GlStateManager.disableRescaleNormal();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + xBenchOffset, y, z + zBenchOffset);

        renderBench(te);

        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + xRackOffset, y + 1, z + zRackOffset);
        
        renderRack(te);

        GlStateManager.popMatrix();
        GlStateManager.translate(x + xMusicOffset, y + 1.250, z + zMusicOffset);

        renderSheetMusic(te);

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    private void renderRack(TileInstrument te)
    {
        GlStateManager.pushMatrix();

        // long angle = (System.currentTimeMillis() / 10) % 360;
        GlStateManager.rotate(face[facing.getIndex()] - 90, 0, 1, 0);
        // GlStateManager.rotate(10, 0, 0, 1);
        RenderHelper.disableStandardItemLighting();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (Minecraft.isAmbientOcclusionEnabled())
        {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else
        {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        World world = te.getWorld();
        /** Translate back to local view coordinates so that we can do the actual rendering here */
        GlStateManager.translate(-te.getPos().getX() - .5, -te.getPos().getY(), -te.getPos().getZ() - .5);

        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world, getRackBakedModel(), world.getBlockState(te.getPos()), te.getPos(),
                Tessellator.getInstance().getBuffer(), true);
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private void renderBench(TileInstrument te)
    {
        GlStateManager.pushMatrix();

        // long angle = (System.currentTimeMillis() / 10) % 360;
        GlStateManager.rotate(face[facing.getIndex()] - 90, 0, 1, 0);
        // GlStateManager.rotate(10, 0, 0, 1);
        RenderHelper.disableStandardItemLighting();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (Minecraft.isAmbientOcclusionEnabled())
        {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else
        {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        World world = te.getWorld();
        /** Translate back to local view coordinates so that we can do the actual rendering here */
        GlStateManager.translate(-te.getPos().getX() - .5, -te.getPos().getY(), -te.getPos().getZ() - .5);

        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world, getBenchBakedModel(), world.getBlockState(te.getPos()), te.getPos(),
                Tessellator.getInstance().getBuffer(), true);
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private void renderSheetMusic(TileInstrument te)
    {
        ItemStack stack = te.getInventory().getStackInSlot(0);
        if (stack != null)
        {
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableLighting();
            GlStateManager.pushMatrix();

            /** Translate to the center of the block and .9 points higher */
            GlStateManager.translate(.5, 0, .5);
            GlStateManager.rotate(face[facing.getIndex()], 0, 1, 0);
            GlStateManager.scale(.4f, .4f, .4f);

            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);

            GlStateManager.popMatrix();
        }
    }

//    /** https://github.com/sinkillerj/ProjectE/blob/MC19/src/main/java/moze_intel/projecte/rendering/PedestalRenderer.java */
//    public void OldrenderTileEntityAt(TilePiano te, double x, double y, double z, float partialTicks, int destroyStage)
//    {
//        if (!te.isInvalid())
//        {
//            if (te.getInventory().getStackInSlot(0) != null)
//            {
//                facing = te.getFacing();
//                if (facing.equals(EnumFacing.NORTH))
//                {
//                    xMusicOffset = 0.0D;
//                    zMusicOffset = -0.5D;
//                } else if (facing.equals(EnumFacing.SOUTH))
//                {
//                    xMusicOffset = 0.0D;
//                    zMusicOffset = 0.5D;
//                } else if (facing.equals(EnumFacing.EAST))
//                {
//                    xMusicOffset = 0.5D;
//                    zMusicOffset = 0.0D;
//                }
//                if (facing.equals(EnumFacing.WEST))
//                {
//                    xMusicOffset = -0.5D;
//                    zMusicOffset = 0.0D;
//                }
//
//                GlStateManager.pushMatrix();
//                GlStateManager.translate(x + 0.5 + xMusicOffset, y + 1.2, z + 0.5 + zMusicOffset);
//                GlStateManager.scale(0.5, 0.5, 0.5);
//                GlStateManager.translate(0, 0.1 * Math.sin(0.1 * (te.getWorld().getTotalWorldTime() + partialTicks)), 0);
//                float angle = (te.getWorld().getTotalWorldTime() + partialTicks) / 20.0F * (180F / (float) Math.PI);
//                GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);
//                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//                Minecraft.getMinecraft().getRenderItem().renderItem(te.getInventory().getStackInSlot(0), ItemCameraTransforms.TransformType.GROUND);
//                GlStateManager.popMatrix();
//            }
//        }
//    }
}
