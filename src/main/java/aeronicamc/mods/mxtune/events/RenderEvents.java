package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.MusicBlock;
import aeronicamc.mods.mxtune.init.ModBlocks;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.items.StageToolItem;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class RenderEvents extends RenderState
{
    static final Minecraft mc = Minecraft.getInstance();

    private RenderEvents(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_)
    {
        super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
    }

    @SubscribeEvent
    public static void event(ItemTooltipEvent event)
    {
        if (event.getItemStack().getItem().equals(ModBlocks.MUSIC_BLOCK.get().asItem()))
            event.getToolTip().add(new TranslationTextComponent("tooltip.mxtune.block_music.help").withStyle(TextFormatting.YELLOW));
        else if (event.getItemStack().getItem().equals(ModItems.MUSIC_PAPER.get()))
            event.getToolTip().add(new TranslationTextComponent("tooltip.mxtune.music_paper.help").withStyle(TextFormatting.YELLOW));
    }

    static ResourceLocation TEXTURE = new ResourceLocation("textures/gui/toasts.png");
    static int width = 160;
    static int height = 32;
    static int blitOffset = 0;

    @SubscribeEvent
    public static void event(RenderGameOverlayEvent.Post event)
    {
        if (mc.player == null )
            return;

        PlayerEntity player = mc.player;
        ItemStack itemStack = player.inventory.getSelected();

        // borrow toast render for testing some ideast.
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && (mc.screen == null) && itemStack.getItem() instanceof IInstrument)
        {
            ItemStack sheetMusic = SheetMusicHelper.getIMusicFromIInstrument(itemStack);
            int offset = Math.max(mc.font.width(SheetMusicHelper.getFormattedMusicTitle(sheetMusic)) + 40, width);
            MatrixStack pPoseStack = event.getMatrixStack();

            mc.getTextureManager().bind(TEXTURE);
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            blit(pPoseStack, 0, 0, 0, 0, width, height);
            blit(pPoseStack, ((offset - width)/2) + 5, 0, 10, 0, width-10, height);
            blit(pPoseStack, offset - width + 10, 0, 10, 0, width, height);

            mc.font.draw(pPoseStack, SheetMusicHelper.getFormattedMusicTitle(sheetMusic), 30.0F, 7.0F, -11534256);
            mc.font.draw(pPoseStack, SheetMusicHelper.getFormattedMusicDuration(sheetMusic), 30.0F, 17.0F, -11534256);
            mc.getItemRenderer().renderAndDecorateItem(itemStack, 8, 8);
        }
    }

    static void blit(MatrixStack pMatrixStack, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
        AbstractGui.blit(pMatrixStack, pX, pY, blitOffset, (float)pUOffset, (float)pVOffset, pUWidth, pVHeight, 256, 256);
    }

    @SubscribeEvent
    public static void event(RenderWorldLastEvent event)
    {
        if (mc.player != null && !mc.player.getMainHandItem().isEmpty() && mc.player.getMainHandItem().getItem() instanceof StageToolItem)
        {
            final float partialTicks = event.getPartialTicks();

            Vector3d vector3d = mc.gameRenderer.getMainCamera().getPosition();
            double camX = vector3d.x();
            double camY = vector3d.y();
            double camZ = vector3d.z();

            PlayerEntity player = mc.player;
//            double camX = player.xOld + (player.getX() - player.xOld) * partialTicks;
//            double camY = player.yOld + (player.getY() - player.yOld) * partialTicks;
//            double camZ = player.zOld + (player.getZ() - player.zOld) * partialTicks;

            AxisAlignedBB axisAlignedBB;
            IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();


            // Define and AABB in BlockPos coordinates
            final BlockPos b0 = new BlockPos(173, 70, -441);
            final BlockPos b1 = new BlockPos(177, 72, -445);
            // Create a VoxelShape for drawing the edges for the AABB BlockPos corner coordinates expanded and moved to encompass the maximum extents of the BlockPos
            AxisAlignedBB bb = new AxisAlignedBB(b0, b1).inflate(0.5).move(0.5,0.5,0.5);
            VoxelShape cubeShape = VoxelShapes.create(bb);

            IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.lines());
            renderShape(event.getMatrixStack(), vertexBuilder, cubeShape, -camX, -camY, -camZ, 1F, 0F, 1F, 0.7F);
            buffer.endBatch(RenderType.lines());

            vertexBuilder = buffer.getBuffer(RenderType.lightning());
            renderFaces(event.getMatrixStack(), vertexBuilder, bb, camX, camY, camZ, 1F, 0F, 1F, 0.15F);
            buffer.endBatch(RenderType.lightning());

            Vector3d center = bb.getCenter();
            renderFloatingText(new StringTextComponent("ABCDEFGHIJKLMNOPQRWXYZ0123456789"), center, event.getMatrixStack(), mc.renderBuffers().bufferSource(), mc.gameRenderer.getMainCamera(), -1);
        }
    }

    @SubscribeEvent
    public static void event(DrawHighlightEvent.HighlightBlock event)
    {
        final WorldRenderer worldRenderer = event.getContext();
        final BlockRayTraceResult blockRayTraceResult = event.getTarget();
        final IRenderTypeBuffer renderTypeBuffer = event.getBuffers();
        final ActiveRenderInfo activeRenderInfo = event.getInfo();
        final MatrixStack matrixStack = event.getMatrix();
        final boolean isCancelable = event.isCancelable();
        final float partialTicks = event.getPartialTicks();
        final EventPriority eventPriority = event.getPhase();
        final PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        Vector3d vector3d = activeRenderInfo.getPosition();
        double camX = vector3d.x();
        double camY = vector3d.y();
        double camZ = vector3d.z();

        World level = player.level;
        if (level.getBlockState(blockRayTraceResult.getBlockPos()).getBlock() instanceof MusicBlock)
        {
            BlockPos posAbove = blockRayTraceResult.getBlockPos().above();
            IVertexBuilder vertexBuilder = renderTypeBuffer.getBuffer(RenderType.lines());
            renderHitOutline(level, matrixStack, vertexBuilder, activeRenderInfo.getEntity(), camX, camY, camZ, blockRayTraceResult.getBlockPos(), level.getBlockState(blockRayTraceResult.getBlockPos()));
            BlockState blockState = level.getBlockState(blockRayTraceResult.getBlockPos());
            renderShape(matrixStack, vertexBuilder, blockState.getShape(level, blockRayTraceResult.getBlockPos(), ISelectionContext.of(activeRenderInfo.getEntity())), posAbove.getX() - camX, posAbove.getY() - camY, posAbove.getZ() - camZ, 0F, 1F, 1F, 0.4F);

            if (isCancelable) event.setCanceled(true);
        }
    }

    private static void renderHitOutline(World level, MatrixStack pMatrixStack, IVertexBuilder pBuffer, Entity pEntity, double pX, double pY, double pZ, BlockPos pBlockPos, BlockState pBlockState) {
        renderShape(pMatrixStack, pBuffer, pBlockState.getShape(level, pBlockPos, ISelectionContext.of(pEntity)), (double)pBlockPos.getX() - pX, (double)pBlockPos.getY() - pY, (double)pBlockPos.getZ() - pZ, 0.0F, 0.0F, 0.0F, 0.4F);
    }

    private static void renderShape(MatrixStack pMatrixStack, IVertexBuilder pBuffer, VoxelShape pShape, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha) {
        Matrix4f matrix4f = pMatrixStack.last().pose();
        pShape.forAllEdges((edgeVertexBegin_X, edgeVertexBegin_Y, edgeVertexBegin_Z, edgeVertexEnd_X, edgeVertexEnd_Y, edgeVertexEnd_Z) -> {
            pBuffer.vertex(matrix4f, (float)(edgeVertexBegin_X + pX), (float)(edgeVertexBegin_Y + pY), (float)(edgeVertexBegin_Z + pZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
            pBuffer.vertex(matrix4f, (float)(edgeVertexEnd_X + pX), (float)(edgeVertexEnd_Y + pY), (float)(edgeVertexEnd_Z + pZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        });
    }

    private static void renderFaces(MatrixStack pMatrixStack, IVertexBuilder pBuffer, AxisAlignedBB aabb, double camX, double camY, double camZ, float pRed, float pGreen, float pBlue, float pAlpha) {
        Matrix4f matrix4f = pMatrixStack.last().pose();
        AxisAlignedBB box = aabb.inflate(.001);
        // North inner
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // South inner
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // East inner
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // West inner
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // Bottom inner
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // top inner
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // North outer
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // South outer
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // East outer
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // West outer
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // Bottom outer
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.minY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();

        // Top outer
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.maxZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.maxX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pBuffer.vertex(matrix4f, (float)(box.minX - camX), (float)(box.maxY - camY), (float)(box.minZ - camZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
    }

    public static void renderFloatingText(ITextComponent pDisplayName, Vector3d pos,  MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, ActiveRenderInfo activeRenderInfo, int pColor) {
        pMatrixStack.pushPose();
        Vector3d cam = activeRenderInfo.getPosition();
        pMatrixStack.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        pMatrixStack.mulPose(activeRenderInfo.rotation());
        pMatrixStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = pMatrixStack.last().pose();
        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int)(f1 * 255.0F) << 24;
        FontRenderer fontrenderer = Minecraft.getInstance().font;
        float f2 = (float)(-fontrenderer.width(pDisplayName) / 2);
        fontrenderer.drawInBatch(pDisplayName, f2, (float)0, pColor, false, matrix4f, pBuffer, false, j, 15728880);
        pMatrixStack.popPose();
    }

}
