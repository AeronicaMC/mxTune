package aeronicamc.mods.mxtune.mixins;

import aeronicamc.mods.mxtune.sound.ClientAudio;
import net.minecraft.client.audio.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;

@Mixin(SoundEngine.class)
public class MixinSoundEngine
{
    /**
     * Our injection point that occurs right after the {@link ChannelManager.Entry} has been initialized
     * @param isound                    {@link ISound}
     * @param ci                        ignored
     * @param soundeventaccessor        {@link SoundEventAccessor}
     * @param resourcelocation          {@link ResourceLocation}
     * @param sound                     {@link Sound}
     * @param f                         volume
     * @param f1                        attenuated volume
     * @param soundcategory             {@link SoundCategory}
     * @param f2                        ISound volume clamped(0.0F, 1.0F)
     * @param f3                        ISound pitch clamped(0.5F, 2.0F)
     * @param isound$attenuationtype    ISound$AttenuationType {@link ISound.AttenuationType}
     * @param flag                      ISound boolean isRelative
     * @param vector3d                  ISound sound location vector
     * @param flag2                     ISound boolean shouldLoop
     * @param flag3                     iSound boolean shouldStream
     * @param completablefuture         CompletableFuture<ChannelManager.Entry>
     * @param channelmanager$entry      {@link ChannelManager.Entry}
     */
    @Inject(method = "play(Lnet/minecraft/client/audio/ISound;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/ChannelManager$Entry;execute(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void onPlaySound(ISound isound, CallbackInfo ci, SoundEventAccessor soundeventaccessor, ResourceLocation resourcelocation, Sound sound, float f, float f1, SoundCategory soundcategory, float f2, float f3, ISound.AttenuationType isound$attenuationtype, boolean flag, Vector3d vector3d, boolean flag2, boolean flag3, CompletableFuture completablefuture, ChannelManager.Entry channelmanager$entry)
    {
        ClientAudio.submitStream(isound, flag3, channelmanager$entry);
    }
}
