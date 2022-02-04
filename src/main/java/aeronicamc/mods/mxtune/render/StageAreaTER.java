package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.blocks.MusicBlockTile;
import aeronicamc.mods.mxtune.caps.stages.ServerStageAreaProvider;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class StageAreaTER extends TileEntityRenderer<MusicBlockTile>
{
    static final Minecraft mc = Minecraft.getInstance();

    public StageAreaTER(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(MusicBlockTile pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        {
            final PlayerEntity player = mc.player;
            World level;
            if (player == null ||(level = player.level) == null) return;

            BlockPos pos = pBlockEntity.getBlockPos();
            Vector3d vector3d = mc.gameRenderer.getMainCamera().getPosition();
            double camX = vector3d.x();
            double camY = vector3d.y();
            double camZ = vector3d.z();

            IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();
            pMatrixStack.popPose();
            pMatrixStack.pushPose();
            ServerStageAreaProvider.getServerStageAreas(level).ifPresent(
                    areas -> {
                        areas.getStageAreas().forEach(
                                (area) -> {
                                    IVertexBuilder vertexBuilder1 = buffer.getBuffer(RenderType.lightning());
                                    StageAreaRenderer.renderFaces(pMatrixStack, vertexBuilder1, area.getAreaAABB(), camX, camY, camZ, 1F, 0F, 1F, 0.1F);
                                });

                        areas.getStageAreas().forEach(
                                (area) ->
                                {
                                    IVertexBuilder vertexBuilder2 = buffer.getBuffer(RenderType.lines());
                                    VoxelShape cubeShape = VoxelShapes.create(area.getAreaAABB());
                                    StageAreaRenderer.renderEdges(pMatrixStack, vertexBuilder2, cubeShape, camX, camY, camZ, 1F, 0F, 1F, 1F);
                                });

                        areas.getStageAreas().forEach(
                                (area) ->
                                {
                                    StageAreaRenderer.renderFloatingText(new StringTextComponent(area.getTitle()),
                                                                         area.getAreaAABB().getCenter(),
                                                                         pMatrixStack,
                                                                         buffer, mc.gameRenderer.getMainCamera(), -1);

                                    StageAreaRenderer.renderFloatingText(new StringTextComponent("Audience Spawn"),
                                                                         new Vector3d(area.getAudienceSpawn().getX(), area.getAudienceSpawn().getY() + 1, area.getAudienceSpawn().getZ()),
                                                                         pMatrixStack,
                                                                         buffer, mc.gameRenderer.getMainCamera(), -1);

                                    StageAreaRenderer.renderFloatingText(new StringTextComponent("Performer Spawn"),
                                                                         new Vector3d(area.getPerformerSpawn().getX(), area.getPerformerSpawn().getY() + 1, area.getPerformerSpawn().getZ()),
                                                                         pMatrixStack,
                                                                         buffer, mc.gameRenderer.getMainCamera(), -1);
                                });
                    });

        }
    }

    @Override
    public boolean shouldRenderOffScreen(MusicBlockTile pTe) {
        return true;
    }
}
