package aeronicamc.mods.mxtune.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;

@SuppressWarnings("unused")
public class MusicVenueToolRenderer
{
    private static final Minecraft mc = Minecraft.getInstance();

    private MusicVenueToolRenderer() { /* NOP */ }

    static void renderUUID(MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, ClippingHelper pClippingHelper)
    {
        if (mc.player != null && mc.level != null)
        {
            mc.level.players().stream().filter(p -> pClippingHelper.isVisible(p.getBoundingBoxForCulling())).forEach(player -> {
                int packedLight = mc.getEntityRenderDispatcher().getPackedLightCoords(player, pPartialTicks);
                Vector3d playerPos = player.getPosition(pPartialTicks);
                RenderHelper.renderFloatingText(
                        new Vector3d(playerPos.x(), playerPos.y()+ player.getBbHeight() + 0.8, playerPos.z()),
                        pMatrixStack, pBuffer, pActiveRenderInfo, -1,
                        new StringTextComponent(player.getUUID().toString()), packedLight);
                pBuffer.endBatch();
            });
        }
    }
}
