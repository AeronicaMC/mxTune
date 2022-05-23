package aeronicamc.mods.mxtune.render.blockentity;

import aeronicamc.mods.mxtune.blocks.MusicBlock;
import aeronicamc.mods.mxtune.blocks.MusicBlockEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class MusicBlockEntityRenderer extends TileEntityRenderer<MusicBlockEntity>
{

    public MusicBlockEntityRenderer(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(MusicBlockEntity pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        pBlockEntity.getItemHandler().ifPresent(inv -> {
            double x = 0;
            double y = 0;
            int itemLighting = pBlockEntity.getBlockState().getValue(MusicBlock.PLAYING) ? 224 : 168;
            Direction facing = pBlockEntity.getBlockState().getValue(HORIZONTAL_FACING);

            pMatrixStack.pushPose();
            if (Direction.NORTH.equals(facing))
            {
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
                pMatrixStack.translate(-1, 0, -1);
            }
            else if (Direction.SOUTH.equals(facing))
            {
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(0));
                pMatrixStack.translate(0, 0, 0);
            }
            else if (Direction.EAST.equals(facing))
            {
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                pMatrixStack.translate(-1, 0, 0);
            }
            else if (Direction.WEST.equals(facing))
            {
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(270));
                pMatrixStack.translate(0, 0, -1);
            }

            for(int i = 0; i < inv.getSlots(); i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty())
                {
                    pMatrixStack.pushPose();

                    pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(0F));
                    pMatrixStack.translate(0.125 + x, 0.75, 0.125 + y);
                    pMatrixStack.scale(0.2F, 0.2F, 0.2F);
                    pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(pBlockEntity.getLevel().getGameTime()));

                    Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemCameraTransforms.TransformType.FIXED, itemLighting, pCombinedOverlay, pMatrixStack, pBuffer);

                    pMatrixStack.popPose();
                }
                x += 0.25;
                if (x >= 1.0)
                {
                    x = 0;
                    y += 0.25;
                }
            }
            pMatrixStack.popPose();
        });
    }
}
