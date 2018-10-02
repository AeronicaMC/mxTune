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

import net.aeronica.libs.mml.core.*;
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
import java.util.ArrayList;
import java.util.List;

public enum SheetMusicUtil
{
    ;
    public static String getMusicTitle(ItemStack stackIn)
    {
        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(stackIn);
        if (!sheetMusic.isEmpty() && sheetMusic.getTagCompound() != null)
        {
            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("MusicBook");
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
                ItemStack sheetMusicOld = new ItemStack(item);
                if (!sheetMusicOld.isEmpty() && sheetMusicOld.getTagCompound() != null && (sheetMusicOld.getItem() instanceof IMusic))
                {
                    NBTTagCompound contents = (NBTTagCompound) sheetMusicOld.getTagCompound().getTag("MusicBook");
                    if (!contents.isEmpty())
                    {
                        return sheetMusicOld;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static void writeSheetMusic(ItemStack sheetMusic, @Nonnull String musicTitle, @Nonnull String mml)
    {
        sheetMusic.setStackDisplayName(musicTitle);
        NBTTagCompound compound = sheetMusic.getTagCompound();
        validateMML(mml);
        if (compound != null && sheetMusic.getItem() instanceof IMusic)
        {
            NBTTagCompound contents = new NBTTagCompound();
            contents.setString("MML", mml);
            compound.setTag("MusicBook", contents);
        }
    }

    public static Pair<Boolean, Double> validateMML(@Nonnull String mml)
    {
        ParseErrorListener parseErrorListener = new ParseErrorListener();
        List<ParseErrorEntry> parseErrorEntries = new ArrayList<>();
        Pair<Boolean, Double> valTime = new Pair<>(false, 0D);
        double seconds;

        MMLParser parser =  MMLParserFactory.getMMLParser(mml);
        if (parser == null)
        {
            ModLogger.debug("MMLParserFactory.getMMLParser() is null in %s", SheetMusicUtil.class.getSimpleName());
            return valTime;
        }
        parser.removeErrorListeners();
        parser.addErrorListener(parseErrorListener);
        parser.setBuildParseTree(true);
        ParseTree tree = parser.band();

        ParseTreeWalker walker = new ParseTreeWalker();
        MMLToMIDI mmlTrans = new MMLToMIDI();
        walker.walk(mmlTrans, tree);
        try (Midi2WavRenderer midi2WavRenderer = new Midi2WavRenderer())
        {
            seconds = midi2WavRenderer.getSequenceInSeconds(mmlTrans.getSequence());
        }
        catch(MidiUnavailableException | InvalidMidiDataException | IOException e)
        {
            ModLogger.info("ValidateMML Error: %s", e);
            seconds = 0D;
            return new Pair<>(false, seconds);
        }
        ModLogger.info("ValidateMML: valid: %s, length: %f",parseErrorListener.getParseErrorEntries().isEmpty(), seconds );
        return new Pair<>(parseErrorListener.getParseErrorEntries().isEmpty(), seconds);
    }
}
