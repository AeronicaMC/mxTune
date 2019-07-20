package net.aeronica.mods.mxtune.util;


import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.AudiblePingPlayerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
 * Miscellus
 * Latin: mixed
 */
public class Miscellus
{

    private Miscellus() { /* NOP */ }

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

    public static void audiblePingPlayer(PlayerEntity entityPlayer, SoundEvent soundEvent)
    {
        if (MXTune.proxy.getEffectiveSide() == Side.SERVER)
            PacketDispatcher.sendTo(new AudiblePingPlayerMessage(soundEvent), (ServerPlayerEntity) entityPlayer);
        else
            entityPlayer.playSound(soundEvent, 1F, 1F);
    }

    public static boolean inDev()
    {
        return ModConfig.moreDebugMessages() || (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
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
