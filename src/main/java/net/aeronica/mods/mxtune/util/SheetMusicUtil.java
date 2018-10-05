/*
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.util;

import net.aeronica.libs.mml.core.MMLParser;
import net.aeronica.libs.mml.core.MMLParserFactory;
import net.aeronica.libs.mml.core.MMLToMIDI;
import net.aeronica.libs.mml.core.ParseErrorListener;
import net.aeronica.mods.mxtune.blocks.IPlacedInstrument;
import net.aeronica.mods.mxtune.blocks.TileInstrument;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.inventory.IMusic;
import net.aeronica.mods.mxtune.sound.Midi2WavRenderer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.annotation.Nonnull;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.io.IOException;

public enum SheetMusicUtil
{
    ;
    public static String getMusicTitle(ItemStack stackIn)
    {
        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(stackIn);
        if (!sheetMusic.isEmpty() && sheetMusic.getTagCompound() != null)
        {
            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("SheetMusic");
            if (!contents.isEmpty())
            {
                return sheetMusic.getDisplayName();
            }
        }
        return "";
    }

    public static ItemStack getSheetMusic(BlockPos pos, EntityPlayer playerIn, boolean isPlaced)
    {
        if (isPlaced)
        {
            if (playerIn.getEntityWorld().getBlockState(pos).getBlock() instanceof IPlacedInstrument)
            {
                Block placedInst = playerIn.getEntityWorld().getBlockState(pos).getBlock();
                TileInstrument te = ((IPlacedInstrument) placedInst).getTE(playerIn.getEntityWorld(), pos);
                if(!te.getInventory().getStackInSlot(0).isEmpty())
                    return te.getInventory().getStackInSlot(0).copy();
            }
        } else
        {
            return SheetMusicUtil.getSheetMusic(playerIn.getHeldItemMainhand());
        }
        return ItemStack.EMPTY;
    }
    
    public static ItemStack getSheetMusic(ItemStack stackIn)
    {
        if (!stackIn.isEmpty() && stackIn.getTagCompound() != null && (stackIn.getItem() instanceof IInstrument))
        {
            NBTTagList items = stackIn.getTagCompound().getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);
            if (items.tagCount() == 1)
            {
                NBTTagCompound item = items.getCompoundTagAt(0);
                ItemStack sheetMusic = new ItemStack(item);
                if (!sheetMusic.isEmpty() && sheetMusic.getTagCompound() != null && (sheetMusic.getItem() instanceof IMusic))
                {
                    NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("SheetMusic");
                    if (contents != null)
                    {
                        return sheetMusic;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean writeSheetMusic(ItemStack sheetMusic, @Nonnull String musicTitle, @Nonnull String mml)
    {
        sheetMusic.setStackDisplayName(musicTitle);
        NBTTagCompound compound = sheetMusic.getTagCompound();
        Pair<Boolean, Integer> validTime = validateMML(mml);
        if (compound != null && (sheetMusic.getItem() instanceof IMusic) && validTime.a && validTime.b > 0)
        {
            NBTTagCompound contents = new NBTTagCompound();
            contents.setString("MML", mml);
            contents.setInteger("duration", validTime.b);
            compound.setTag("SheetMusic", contents);
            return true;
        }
        return false;
    }

    /**
     * Validate the supplied MML and return it's length in seconds.
     *
     * @param mml to be validated and its duration in seconds calculated.
     * @return a Pair with 'a' set true for valid MML else false, and 'b' the length of the tune in seconds<B></B>
     * for valid MML, else 0D.
     */
    public static Pair<Boolean, Integer> validateMML(@Nonnull String mml)
    {
        ParseErrorListener parseErrorListener = new ParseErrorListener();
        int seconds = 0;

        MMLParser parser =  MMLParserFactory.getMMLParser(mml);
        if (parser == null)
        {
            ModLogger.debug("MMLParserFactory.getMMLParser() is null in %s", SheetMusicUtil.class.getSimpleName());
            return new Pair<>(false, 0);
        }
        parser.removeErrorListeners();
        parser.addErrorListener(parseErrorListener);
        parser.setBuildParseTree(true);
        ParseTree tree = parser.band();
        ParseTreeWalker walker = new ParseTreeWalker();
        MMLToMIDI mmlTrans = new MMLToMIDI();
        walker.walk(mmlTrans, tree);
        if (parseErrorListener.getParseErrorEntries().isEmpty())
        {
            try (Midi2WavRenderer midi2WavRenderer = new Midi2WavRenderer())
            {
                // sequence in seconds plus 4 a second buffer. Same as the MIDI2WaveRenderer class.
                seconds = (int) (midi2WavRenderer.getSequenceInSeconds(mmlTrans.getSequence()) + 4);
            } catch (MidiUnavailableException | InvalidMidiDataException | IOException e)
            {
                ModLogger.info("ValidateMML Error: %s in %s", e, SheetMusicUtil.class.getSimpleName());
                return new Pair<>(false, 0);
            }
        }
        ModLogger.info("ValidateMML: valid: %s, length: %d", parseErrorListener.getParseErrorEntries().isEmpty(), seconds);
        return new Pair<>(parseErrorListener.getParseErrorEntries().isEmpty(), seconds);
    }

    public static String formatDuration(int seconds)
    {
        int absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }
}
