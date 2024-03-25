package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
import aeronicamc.mods.mxtune.gui.OverlayManagerScreen;
import aeronicamc.mods.mxtune.init.ModBlocks;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.items.MusicVenueInfoItem;
import aeronicamc.mods.mxtune.items.MusicVenueToolItem;
import aeronicamc.mods.mxtune.managers.GroupClient;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static aeronicamc.mods.mxtune.render.ModRenderType.*;
import static aeronicamc.mods.mxtune.render.RenderHelper.getPlayer;
import static aeronicamc.mods.mxtune.render.RenderHelper.mc;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class RenderEvents
{
    private RenderEvents() { /* NOOP */ }

    @SubscribeEvent
    public static void event(ItemTooltipEvent event)
    {
        if (event.getItemStack().getItem().equals(ModBlocks.MUSIC_BLOCK.get().asItem()))
            event.getToolTip().add(new TranslationTextComponent("tooltip.mxtune.block_music.help").withStyle(TextFormatting.YELLOW));
        if (event.getItemStack().getItem().equals(ModItems.MUSIC_PAPER.get()))
            event.getToolTip().add(new TranslationTextComponent("tooltip.mxtune.music_paper.help").withStyle(TextFormatting.YELLOW));
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void event(DrawHighlightEvent.HighlightBlock event)
    {
        if (mc.options.renderDebug) return;
        final BlockRayTraceResult blockRayTraceResult = event.getTarget();
        final IRenderTypeBuffer renderTypeBuffer = event.getBuffers();
        final ActiveRenderInfo activeRenderInfo = event.getInfo();
        final MatrixStack matrixStack = event.getMatrix();

        final Vector3d camera = activeRenderInfo.getPosition();
        final World level = getPlayer().level;

        final BlockPos blockPos = blockRayTraceResult.getBlockPos();
        final BlockState blockState = level.getBlockState(blockRayTraceResult.getBlockPos());

        // Highlight blocks for tool use
        if (getPlayer().inventory.getSelected().getItem() instanceof MusicVenueToolItem)
        {
            if (event.isCancelable()) event.setCanceled(true);

            if (!blockState.isAir(level, blockPos) && level.getWorldBorder().isWithinBounds(blockPos))
            {
                final IVertexBuilder ivertexBuilder = renderTypeBuffer.getBuffer(THICK_LINES);
                RenderHelper.renderHitOutline(level, matrixStack, ivertexBuilder, activeRenderInfo.getEntity(), camera.x(), camera.y(), camera.z(), blockPos, blockState);
            }
        }

        // Show the pre-placement info panel outline.
        if (getPlayer().inventory.getSelected().getItem() instanceof MusicVenueInfoItem)
        {
            if (event.isCancelable()) event.setCanceled(true);

            if (!blockState.isAir(level, blockPos) && level.getWorldBorder().isWithinBounds(blockPos))
            {
                final ItemUseContext useContext = new ItemUseContext(getPlayer(), Hand.MAIN_HAND, blockRayTraceResult);
                final BlockPos clickedPos = useContext.getClickedPos();
                final Direction facing = useContext.getClickedFace();
                final BlockPos placementPos = clickedPos.relative(facing);

                if (MusicVenueInfoItem.mayPlace(getPlayer(), facing, useContext.getItemInHand(), placementPos))
                {
                    final HangingEntity infoEntity = new MusicVenueInfoEntity(useContext.getLevel(), placementPos, facing);
                    if (infoEntity.survives())
                    {
                        final IVertexBuilder vertexBuilder1 = renderTypeBuffer.getBuffer(TRANSPARENT_QUADS_NO_TEXTURE);
                        RenderHelper.renderFaces(matrixStack, vertexBuilder1, infoEntity.getBoundingBox().inflate(0.001D), camera.x(), camera.y(), camera.z(), 1.0F, 0.0F, 1.0F, 0.4F);
                        final IVertexBuilder vertexBuilder2 = renderTypeBuffer.getBuffer(LINES);
                        RenderHelper.renderEdges(matrixStack, vertexBuilder2, infoEntity.getBoundingBox().inflate(0.001D), camera.x(), camera.y(), camera.z(), 1.0F, 0.0F, 1.0F, 0.4F);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void event(DrawHighlightEvent.HighlightEntity event)
    {
        if (mc.options.renderDebug) return;
        if (!(getPlayer().inventory.getSelected().getItem() instanceof MusicVenueToolItem)) return;
        if (event.isCancelable()) event.setCanceled(true);

        final EntityRayTraceResult entityRayTraceResult = event.getTarget();
        final IRenderTypeBuffer renderTypeBuffer = event.getBuffers();
        final ActiveRenderInfo activeRenderInfo = event.getInfo();
        final MatrixStack matrixStack = event.getMatrix();
        final Vector3d camera = activeRenderInfo.getPosition();

        // Draw the entity bounding box when using the MusicVenueToolItem and looking at en entity
        IVertexBuilder vertexBuilder2 = renderTypeBuffer.getBuffer(LINES);
        RenderHelper.renderEdges(matrixStack, vertexBuilder2, entityRayTraceResult.getEntity().getBoundingBox().inflate(0.001D), camera.x(), camera.y(), camera.z(), 1.0F, 0.0F, 1.0F, 0.4F);
    }

    private static final Set<RenderGameOverlayEvent.ElementType> NO_RENDER_ELEMENTS = new HashSet<>(Arrays.asList(
            RenderGameOverlayEvent.ElementType.HOTBAR, RenderGameOverlayEvent.ElementType.FOOD,
            RenderGameOverlayEvent.ElementType.HEALTH, RenderGameOverlayEvent.ElementType.ARMOR,
            RenderGameOverlayEvent.ElementType.HELMET, RenderGameOverlayEvent.ElementType.EXPERIENCE)
    );
    @SubscribeEvent
    public static void event(RenderGameOverlayEvent.Pre event)
    {
        if (event.isCancelable() && mc.screen instanceof OverlayManagerScreen)
        {
            if (NO_RENDER_ELEMENTS.contains(event.getType()))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void event(RenderGameOverlayEvent.Post event)
    {
        if (mc.options.renderDebug ) return;
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && (mc.screen == null)) {
            RenderHelper.getOverlayItemGui().render(event.getMatrixStack());
            ActiveAudioOverlay.render(event);
        }
    }

    static void renderGroupStatusPlacard(MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, ClippingHelper pClippingHelper)
    {
        if (GroupClient.hasGroups())
        {
            ItemStack placardStack = new ItemStack(ModItems.PLACARD_ITEM.get());
            Vector3d cam = pActiveRenderInfo.getPosition();
            List<Entity> nearLivingEntities = getPlayer().level.getEntities(null, getPlayer().getBoundingBox().inflate(48));
            if (!nearLivingEntities.isEmpty()) {
                nearLivingEntities.stream().filter(p -> pClippingHelper.isVisible(p.getBoundingBoxForCulling())).forEach(livingEntity -> {

                    if (GroupClient.isGrouped(livingEntity.getId())) {
                        placardStack.setDamageValue(GroupClient.getPlacardState(livingEntity.getId()));
                        Vector3d entityPos = livingEntity.getPosition(pPartialTicks);

                        pMatrixStack.pushPose();
                        pMatrixStack.translate(entityPos.x() - cam.x(), entityPos.y() - cam.y(), entityPos.z() - cam.z());
                        pMatrixStack.translate(0.0D, livingEntity.getBbHeight() + 0.8D, 0.0D);
                        pMatrixStack.mulPose(RenderHelper.getYAxisRotation(pActiveRenderInfo.rotation())); // imperfect
                        pMatrixStack.scale(0.5F, 0.5F, 0.5F);

                        Minecraft.getInstance().getItemRenderer().renderStatic(placardStack, ItemCameraTransforms.TransformType.FIXED, FULL_BRIGHT_LIGHT_MAP, OverlayTexture.NO_OVERLAY, pMatrixStack, pBuffer);
                        pMatrixStack.popPose();
                    }
                    pBuffer.endBatch();
                });
            }
        }
    }

    // It's absolutely Fabulous... or maybe not, but at least in Fabulous Graphics mode it's not bad.
    public static void renderLast(MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, ClippingHelper pClippingHelper)
    {
        MusicVenueRenderer.render(pMatrixStack, pBuffer, pActiveRenderInfo, pClippingHelper);
        renderGroupStatusPlacard(pMatrixStack, pBuffer, pActiveRenderInfo, pPartialTicks, pClippingHelper);
    }
}
