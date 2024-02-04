package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.caps.venues.MusicVenueProvider;
import aeronicamc.mods.mxtune.init.ModItems;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.Objects;

public class MusicVenueRenderer
{
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ItemStack MUSIC_VENUE_ITEM_STACK = new ItemStack(ModItems.MUSIC_VENUE_TOOL.get());
    private MusicVenueRenderer() { /* NOP */ }

    public static void render(MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, LightTexture pLightTexture, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, ClippingHelper pClippingHelper)
    {
        final PlayerEntity player = mc.player;
        World level;
        if (player == null || (level = player.level) == null) return;

        Vector3d camera = pActiveRenderInfo.getPosition();
        double camX = camera.x;
        double camY = camera.y;
        double camZ = camera.z;

        MusicVenueProvider.getMusicVenues(level).filter(areas -> isToolInHotBar(player)).ifPresent(
                areas -> {
                    areas.getVenueList().stream().filter(Objects::nonNull)
                            // Sort areas so the transparency renders properly with regard to each other and the camera.
                            .sorted((o1, o2) -> Double.compare(o2.getVenueAABB().getCenter().distanceToSqr(camera), o1.getVenueAABB().getCenter().distanceToSqr(camera)))
                            .filter(venue -> pClippingHelper.isVisible(venue.getVenueAABB())).forEach(
                                (venue) -> {
                                    IVertexBuilder vertexBuilder1 = pBuffer.getBuffer(ModRenderType.TRANSPARENT_QUADS_NO_TEXTURE);
                                    RenderHelper.renderFaces(pMatrixStack, vertexBuilder1, venue.getVenueAABB(), camX, camY, camZ, venue.getR(), venue.getG(), venue.getB(), 0.1F);

                                    IVertexBuilder vertexBuilder2 = pBuffer.getBuffer(ModRenderType.THICK_LINES);
                                    RenderHelper.renderEdges(pMatrixStack, vertexBuilder2, venue.getVenueAABB().inflate(-0.001D), camX, camY, camZ, venue.getR(), venue.getG(), venue.getB(), 0.4F);

                                    if ((pActiveRenderInfo.getEntity().distanceToSqr(venue.getVenueAABB().getCenter()) <= 512))
                                    {
                                        if (!venue.getName().isEmpty())
                                            RenderHelper.renderFloatingText(venue.getVenueAABB().getCenter(), pMatrixStack, pBuffer, pActiveRenderInfo, -1, new StringTextComponent(venue.getName()),
                                                                            ModRenderType.FULL_BRIGHT_LIGHT_MAP);

                                        RenderHelper.renderFloatingText(new Vector3d(venue.getAudienceSpawn().getX() + 0.5, venue.getAudienceSpawn().getY() + 1.5, venue.getAudienceSpawn().getZ() + 0.5), pMatrixStack, pBuffer, pActiveRenderInfo, -1, new StringTextComponent("Audience Spawn"),
                                                                        ModRenderType.FULL_BRIGHT_LIGHT_MAP);

                                        RenderHelper.renderFloatingText(new Vector3d(venue.getPerformerSpawn().getX() + 0.5, venue.getPerformerSpawn().getY() + 1.5, venue.getPerformerSpawn().getZ() + 0.5), pMatrixStack, pBuffer, pActiveRenderInfo, -1, new StringTextComponent("Performer Spawn"),
                                                                        ModRenderType.FULL_BRIGHT_LIGHT_MAP);
                                    }
                                    pBuffer.endBatch(ModRenderType.TRANSPARENT_QUADS_NO_TEXTURE);
                                    pBuffer.endBatch(RenderType.lines());
                                    pBuffer.endBatch();
                                });
                });
    }

    private static boolean isToolInHotBar(PlayerEntity player)
    {
        int slot = player.inventory.findSlotMatchingItem(MUSIC_VENUE_ITEM_STACK);
        return slot >= 0 && slot < 9;
    }
}
