package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModBlocks;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.items.StageToolItem;
import aeronicamc.mods.mxtune.render.StageAreaRenderer;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
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
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class RenderEvents
{
    static final Minecraft mc = Minecraft.getInstance();

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
    public static void event(DrawHighlightEvent.HighlightBlock event)
    {
        if (mc.player == null )
            return;
        if (mc.options.renderDebug) return;
        if (!(mc.player.inventory.getSelected().getItem() instanceof StageToolItem)) return;
        if (event.isCancelable()) event.setCanceled(true);

        final BlockRayTraceResult blockRayTraceResult = event.getTarget();
        final IRenderTypeBuffer renderTypeBuffer = event.getBuffers();
        final ActiveRenderInfo activeRenderInfo = event.getInfo();
        final MatrixStack matrixStack = event.getMatrix();

        Vector3d vector3d = activeRenderInfo.getPosition();
        double camX = vector3d.x();
        double camY = vector3d.y();
        double camZ = vector3d.z();
        World level = mc.player.level;

        BlockPos blockPos = blockRayTraceResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockRayTraceResult.getBlockPos());

        if (!blockState.isAir(level, blockPos) && level.getWorldBorder().isWithinBounds(blockPos)) {
            IVertexBuilder ivertexBuilder = renderTypeBuffer.getBuffer(RenderType.lines());
            renderHitOutline(level, matrixStack, ivertexBuilder, activeRenderInfo.getEntity(), camX, camY, camZ, blockPos, blockState);
        }
    }

    private static void renderHitOutline(World level, MatrixStack pMatrixStack, IVertexBuilder pBuffer, Entity pEntity, double pX, double pY, double pZ, BlockPos pBlockPos, BlockState pBlockState) {
        renderShape(pMatrixStack, pBuffer, pBlockState.getShape(level, pBlockPos, ISelectionContext.of(pEntity)), (double)pBlockPos.getX() - pX, (double)pBlockPos.getY() - pY, (double)pBlockPos.getZ() - pZ, 1.0F, 0.0F, 1.0F, 0.9F);
    }

    private static void renderShape(MatrixStack pMatrixStack, IVertexBuilder pBuffer, VoxelShape pShape, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha) {
        Matrix4f matrix4f = pMatrixStack.last().pose();
        pShape.forAllEdges((edgeVertexBegin_X, edgeVertexBegin_Y, edgeVertexBegin_Z, edgeVertexEnd_X, edgeVertexEnd_Y, edgeVertexEnd_Z) -> {
            pBuffer.vertex(matrix4f, (float)(edgeVertexBegin_X + pX), (float)(edgeVertexBegin_Y + pY), (float)(edgeVertexBegin_Z + pZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
            pBuffer.vertex(matrix4f, (float)(edgeVertexEnd_X + pX), (float)(edgeVertexEnd_Y + pY), (float)(edgeVertexEnd_Z + pZ)).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        });
    }

    @SubscribeEvent
    public static void event(RenderGameOverlayEvent.Post event)
    {
        if (mc.player == null )
            return;
        if (mc.options.renderDebug) return;

        PlayerEntity player = mc.player;
        ItemStack itemStack = player.inventory.getSelected();

        // borrow toast render for testing some ideas.
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

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && (mc.screen == null) && itemStack.getItem() instanceof StageToolItem)
        {
            MatrixStack pPoseStack = event.getMatrixStack();
            ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getMainCamera();
            RayTraceResult raytraceresult = mc.hitResult;

            ITextComponent textComponent = new StringTextComponent("test").withStyle(TextFormatting.WHITE);
            int offset = Math.max(mc.font.width(textComponent) + 40, width);

            if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.BLOCK)
                StageAreaRenderer.renderFloatingText(textComponent, raytraceresult.getLocation(), pPoseStack , mc.renderBuffers().bufferSource(), activeRenderInfo, -1);
            mc.renderBuffers().bufferSource().endBatch();

            mc.getTextureManager().bind(TEXTURE);
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            blit(pPoseStack, 0, 0, 0, 0, width, height);
            blit(pPoseStack, ((offset - width)/2) + 5, 0, 10, 0, width-10, height);
            blit(pPoseStack, offset - width + 10, 0, 10, 0, width, height);

            mc.font.draw(pPoseStack, textComponent, 30.0F, 7.0F, -11534256);
            mc.font.draw(pPoseStack, textComponent, 30.0F, 17.0F, -11534256);
            mc.getItemRenderer().renderAndDecorateItem(itemStack, 8, 8);
        }
    }

    static void blit(MatrixStack pMatrixStack, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
        AbstractGui.blit(pMatrixStack, pX, pY, blitOffset, (float)pUOffset, (float)pVOffset, pUWidth, pVHeight, 256, 256);
    }

    // It's absolutely Fabulous... or maybe not, but at least in Fabulous Graphics mode it's not bad.
    public static void renderLast(MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, LightTexture pLightTexture, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, ClippingHelper pClippingHelper)
    {
        StageAreaRenderer.render(pMatrixStack, pBuffer, pLightTexture, pActiveRenderInfo, pPartialTicks, pClippingHelper);
    }
}
