package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.stages.ServerStageAreaProvider;
import aeronicamc.mods.mxtune.init.ModBlocks;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.items.StageToolItem;
import aeronicamc.mods.mxtune.render.StageAreaRenderer;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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
    public static void event(RenderGameOverlayEvent.Post event)
    {
        if (mc.player == null )
            return;

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
    }

    static void blit(MatrixStack pMatrixStack, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
        AbstractGui.blit(pMatrixStack, pX, pY, blitOffset, (float)pUOffset, (float)pVOffset, pUWidth, pVHeight, 256, 256);
    }

    @SubscribeEvent
    public static void event(RenderWorldLastEvent event)
    {
        if (mc.level != null && mc.player != null && !mc.player.getMainHandItem().isEmpty() && mc.player.getMainHandItem().getItem() instanceof StageToolItem)
        {
            Vector3d vector3d = mc.gameRenderer.getMainCamera().getPosition();
            double camX = vector3d.x();
            double camY = vector3d.y();
            double camZ = vector3d.z();

            IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();

            // This will actually need to filter on nearest stage areas and stream those to the renderer
            ServerStageAreaProvider.getServerStageAreas(mc.level).ifPresent(p -> {
                if (p.getStageAreas().isEmpty()) return;

                p.getStageAreas().forEach(
                        (area) -> {

                            IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.lightning());
                            StageAreaRenderer.renderFaces(event.getMatrixStack(), vertexBuilder, area.getAreaAABB(), camX, camY, camZ, 1F, 0F, 1F, 0.15F);
                        });
                p.getStageAreas().forEach(
                        (area) ->
                                  {
                                      VoxelShape cubeShape = VoxelShapes.create(area.getAreaAABB());
                                      IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.lines());
                                      StageAreaRenderer.renderEdges(event.getMatrixStack(), vertexBuilder, cubeShape, camX, camY, camZ, 1F, 1F, 1F, 0.7F);
                                      buffer.endBatch(RenderType.lines());
                                  });
                p.getStageAreas().forEach(
                        (area) -> StageAreaRenderer.renderFloatingText(
                                new StringTextComponent(area.getTitle()),
                                area.getAreaAABB().getCenter(),
                                event.getMatrixStack(),
                                mc.renderBuffers().bufferSource(), mc.gameRenderer.getMainCamera(), -1));

                buffer.endBatch(RenderType.lightning());
                buffer.endBatch(RenderType.lines());
            });
        }
    }

}
