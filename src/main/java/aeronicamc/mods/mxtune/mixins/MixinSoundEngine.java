package aeronicamc.mods.mxtune.mixins;

import aeronicamc.mods.mxtune.Reference;
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
    @Inject(method = "play(Lnet/minecraft/client/audio/ISound;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/ChannelManager$Entry;execute(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void onPlaySound(ISound pISound, CallbackInfo ci, SoundEventAccessor pSoundeventAccessor, ResourceLocation pRresourceLocation, Sound sound, float f, float f1, SoundCategory pSoundCategory, float f2, float f3, ISound.AttenuationType pAttenuationType, boolean flag, Vector3d pVector3d, boolean flag2, boolean isStream, CompletableFuture pCompletableFuture, ChannelManager.Entry entry)
    {
        if (pSoundeventAccessor.getSound().getLocation().getNamespace().equals(Reference.MOD_ID))
            ClientAudio.submitStream(pISound, isStream, entry);
    }
}
