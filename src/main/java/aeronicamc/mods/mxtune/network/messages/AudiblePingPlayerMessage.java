package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.init.ModSoundEvents;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class AudiblePingPlayerMessage extends AbstractMessage<AudiblePingPlayerMessage>
{
    ResourceLocation soundEventResource;

    public AudiblePingPlayerMessage() { /* NOP */ }

    public AudiblePingPlayerMessage(SoundEvent soundEvent)
    {
        this.soundEventResource = ForgeRegistries.SOUND_EVENTS.getKey(soundEvent);
    }

    @Override
    public void encode(AudiblePingPlayerMessage message, PacketBuffer buffer)
    {
        buffer.writeResourceLocation(message.soundEventResource);
    }

    @Override
    public AudiblePingPlayerMessage decode(PacketBuffer buffer)
    {
        final ResourceLocation soundEventResource = buffer.readResourceLocation();
        final SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(soundEventResource);
        return soundEvent !=null ? new AudiblePingPlayerMessage(soundEvent) : new AudiblePingPlayerMessage(ModSoundEvents.FAILURE.get());
    }

    @Override
    public void handle(AudiblePingPlayerMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->{
                ServerPlayerEntity pPlayer = ctx.get().getSender();
                SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(message.soundEventResource);
                if (pPlayer != null && soundEvent != null)
                {
//                    pPlayer.displayClientMessage(new TranslationTextComponent("container.isLocked"), true);
                    pPlayer.playNotifySound(soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            });
        ctx.get().setPacketHandled(true);
    }
}
