package aeronicamc.mods.mxtune.util;

import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.AudiblePingPlayerMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    /**
     * Creates a new array with the second array appended to the end of the
     * first array.
     *
     * @param arrayOne The first array.
     * @param arrayTwo The second array.
     * @param length   How many bytes to append from the second array.
     * @return Byte array containing information from both arrays.
     */
    @Nonnull
    public static byte[] appendByteArrays(@Nullable byte[] arrayOne, @Nullable byte[] arrayTwo, int length)
    {
        byte[] newArray;
        if (arrayOne == null && arrayTwo == null)
        {
            // no data, just return
            return new byte[0];
        }
        else if (arrayOne == null)
        {
            // create the new array, same length as arrayTwo:
            newArray = new byte[length];
            // fill the new array with the contents of arrayTwo:
            System.arraycopy(arrayTwo, 0, newArray, 0, length);
        }
        else if (arrayTwo == null)
        {
            // create the new array, same length as arrayOne:
            newArray = new byte[arrayOne.length];
            // fill the new array with the contents of arrayOne:
            System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
        }
        else
        {
            // create the new array large enough to hold both arrays:
            newArray = new byte[arrayOne.length + length];
            System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
            // fill the new array with the contents of both arrays:
            System.arraycopy(arrayTwo, 0, newArray, arrayOne.length, length);
        }

        return newArray;
    }
}
