package aeronicamc.mods.mxtune.mixins;

import aeronicamc.mods.mxtune.render.RenderEvents;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(ParticleManager.class)
public class MixinParticleManager
{
    /**
     * Hook into the {@link WorldRenderer} via the {@link ParticleManager} renderParticle method for our {@link RenderWorldLastEvent} replacement.
     *
     * @param pMatrixStack      The provided matrix stack
     * @param pBuffer           The provided render type buffers
     * @param pLightTexture     The provided light texture
     * @param pActiveRenderInfo The provided active render info
     * @param pPartialTicks     The provided partial ticks
     * @param clippingHelper    The provided clipping helper
     * @param ci                ignored
     */
    @Inject(method = "renderParticles(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/renderer/ActiveRenderInfo;FLnet/minecraft/client/renderer/culling/ClippingHelper;)V", at = @At("RETURN"), remap = false, require = 1)
    public void renderParticlesCallback(MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, LightTexture pLightTexture, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, ClippingHelper clippingHelper, CallbackInfo ci)
    {
        RenderEvents.renderLast(pMatrixStack, pBuffer, pLightTexture, pActiveRenderInfo, pPartialTicks, clippingHelper);
    }
}