package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.stages.ServerStageAreaProvider;
import aeronicamc.mods.mxtune.init.ModBlocks;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.render.StageAreaRenderer;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
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

    // It's absolutely Fabulous
    public static void renderLast(MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, LightTexture pLightTexture, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, ClippingHelper pClippingHelper)
    {
        final PlayerEntity player = mc.player;
        World level;
        if (player == null || (level = player.level) == null) return;

        Vector3d camera = pActiveRenderInfo.getPosition();
        double camX = camera.x;
        double camY = camera.y;
        double camZ = camera.z;

        ServerStageAreaProvider.getServerStageAreas(level).ifPresent(
                areas -> {
                    areas.getStageAreas().stream().filter(area-> pClippingHelper.isVisible(area.getAreaAABB())).forEach(
                            (area) -> {
                                IVertexBuilder vertexBuilder1 = pBuffer.getBuffer(RenderType.lightning());
                                StageAreaRenderer.renderFaces(pMatrixStack, vertexBuilder1, area.getAreaAABB(), camX, camY, camZ, 1F, 0F, 1F, 0.1F);


                                IVertexBuilder vertexBuilder2 = pBuffer.getBuffer(RenderType.lines());
                                VoxelShape cubeShape = VoxelShapes.create(area.getAreaAABB());
                                StageAreaRenderer.renderEdges(pMatrixStack, vertexBuilder2, cubeShape, camX, camY, camZ, 1F, 0F, 1F, 1F);

                                StageAreaRenderer.renderFloatingText(new StringTextComponent(area.getTitle()),
                                                                     area.getAreaAABB().getCenter(),
                                                                     pMatrixStack,
                                                                     pBuffer, pActiveRenderInfo, -1);

                                StageAreaRenderer.renderFloatingText(new StringTextComponent("Audience Spawn"),
                                                                     new Vector3d(area.getAudienceSpawn().getX() + 0.5, area.getAudienceSpawn().getY() + 1.5, area.getAudienceSpawn().getZ() + 0.5),
                                                                     pMatrixStack,
                                                                     pBuffer, pActiveRenderInfo, -1);

                                StageAreaRenderer.renderFloatingText(new StringTextComponent("Performer Spawn"),
                                                                     new Vector3d(area.getPerformerSpawn().getX() + 0.5, area.getPerformerSpawn().getY() + 1.5, area.getPerformerSpawn().getZ() + 0.5),
                                                                     pMatrixStack,
                                                                     pBuffer, pActiveRenderInfo, -1);
                            });
                });
    }
}
