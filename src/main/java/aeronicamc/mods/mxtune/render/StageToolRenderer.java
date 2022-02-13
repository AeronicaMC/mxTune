package aeronicamc.mods.mxtune.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;

public class StageToolRenderer
{
    private static final Minecraft mc = Minecraft.getInstance();

    private StageToolRenderer() { /*NOP */ }

    static void renderUUID(MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, LightTexture pLightTexture, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, ClippingHelper pClippingHelper)
    {
        if (mc.player != null)
        {
            Vector3d cam = pActiveRenderInfo.getPosition();

            mc.level.players().stream().filter(p -> pClippingHelper.isVisible(p.getBoundingBoxForCulling())).forEach(player -> {
                Vector3d playerPos = player.getPosition(pPartialTicks);
                RenderHelper.renderFloatingText(
                        new StringTextComponent(player.getUUID().toString()),
                        new Vector3d(playerPos.x(), playerPos.y()+ player.getBbHeight() + 0.8, playerPos.z()),
                        /*new Vector3d(player.getX(), player.getBbHeight() + 0.5D, player.getZ()),*/
                        pMatrixStack, pBuffer, pActiveRenderInfo ,-1);
                pBuffer.endBatch();
            });

        }
    }

}
