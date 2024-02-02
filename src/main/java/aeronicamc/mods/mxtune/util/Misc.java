package aeronicamc.mods.mxtune.util;

import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.AudiblePingPlayerMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class Misc
{
    private Misc() { /* NOP */ }

    public static void addToast(IToast pToast)
    {
        if (FMLEnvironment.dist.isClient())
        {
            Minecraft.getInstance().getToasts().addToast(pToast);
        }
    }

    public static void audiblePingPlayer(PlayerEntity pPlayer, SoundEvent soundEvent)
    {
        if (pPlayer.level.isClientSide())
            PacketDispatcher.sendToServer(new AudiblePingPlayerMessage(soundEvent));
        else
            pPlayer.playNotifySound(soundEvent, SoundCategory.BLOCKS, 1F, 1F);
    }

    public static int clamp(int min, int max, int value) {return Math.max(Math.min(max, value), min);}

    @SuppressWarnings("ConstantConditions")
    public static <T> T nonNullInjected()
    {
        return null;
    }
}
