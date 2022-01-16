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

    /**
     * Forge really likes annotation magic. This makes static analysis tools shut up.
     * <p></p>
     * This method was copied from https://github.com/JamiesWhiteShirt/clothesline
     * https://github.com/JamiesWhiteShirt/clothesline/blob/master/src/main/java/com/jamieswhiteshirt/clothesline/common/Util.java
     *
     * Copyright 2018 Erlend Åmdal
     *
     * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
     * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
     * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
     * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
     *
     * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
     * Software.
     *
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO TH
     * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
     * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
     * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
     */
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
