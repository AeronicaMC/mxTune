package aeronicamc.mods.mxtune.render;

import aeronicamc.mods.mxtune.caps.venues.MusicVenueProvider;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class MusicVenueRenderer
{
    private static final Minecraft mc = Minecraft.getInstance();
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

        MusicVenueProvider.getMusicVenues(level).ifPresent(
                areas -> {
                    areas.getMusicVenues().stream()
                            // Sort areas so the transparency renders properly with regard to each other and the camera.
                            .sorted((o1, o2) -> ((Double)o2.getVenueAABB().getCenter().distanceToSqr(camera))
                                    .compareTo(o1.getVenueAABB().getCenter().distanceToSqr(camera)))
                            .filter(venue-> pClippingHelper.isVisible(venue.getVenueAABB())).forEach(
                                (venue) -> {
                                    IVertexBuilder vertexBuilder1 = pBuffer.getBuffer(ModRenderType.TRANSPARENT_QUADS_NO_TEXTURE);
                                    RenderHelper.renderFaces(pMatrixStack, vertexBuilder1, venue.getVenueAABB(), camX, camY, camZ, venue.getR(), venue.getG(), venue.getB(), 0.1F);

                                    IVertexBuilder vertexBuilder2 = pBuffer.getBuffer(RenderType.lines());
                                    RenderHelper.renderEdges(pMatrixStack, vertexBuilder2, venue.getVenueAABB(), camX, camY, camZ, venue.getR(), venue.getG(), venue.getB(), 0.4F);

                                    if (!(pActiveRenderInfo.getEntity().distanceToSqr(venue.getVenueAABB().getCenter()) > 512))
                                    {
                                        RenderHelper.renderFloatingText(venue.getVenueAABB().getCenter(), pMatrixStack, pBuffer, pActiveRenderInfo, -1, new StringTextComponent(venue.getName()),
                                                                        RenderHelper.PACKED_LIGHT_MAX);

                                        RenderHelper.renderFloatingText(new Vector3d(venue.getAudienceSpawn().getX() + 0.5, venue.getAudienceSpawn().getY() + 1.5, venue.getAudienceSpawn().getZ() + 0.5), pMatrixStack, pBuffer, pActiveRenderInfo, -1, new StringTextComponent("Audience Spawn"),
                                                                        RenderHelper.PACKED_LIGHT_MAX);

                                        RenderHelper.renderFloatingText(new Vector3d(venue.getPerformerSpawn().getX() + 0.5, venue.getPerformerSpawn().getY() + 1.5, venue.getPerformerSpawn().getZ() + 0.5), pMatrixStack, pBuffer, pActiveRenderInfo, -1, new StringTextComponent("Performer Spawn"),

                                                                        RenderHelper.PACKED_LIGHT_MAX);
                                    }
                                    pBuffer.endBatch(ModRenderType.TRANSPARENT_QUADS_NO_TEXTURE);
                                    pBuffer.endBatch(RenderType.lines());
                                    pBuffer.endBatch();
                                });
                });
    }

}
