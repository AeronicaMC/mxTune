/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.aeronica.mods.mxtune.util;

import net.aeronica.libs.mml.core.MMLParser;
import net.aeronica.libs.mml.core.MMLParserFactory;
import net.aeronica.libs.mml.core.MMLToMIDI;
import net.aeronica.libs.mml.core.ParseErrorListener;
import net.aeronica.mods.mxtune.blocks.IMusicPlayer;
import net.aeronica.mods.mxtune.blocks.IPlacedInstrument;
import net.aeronica.mods.mxtune.blocks.TileInstrument;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.inventory.IMusic;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.sound.Midi2WavRenderer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.annotation.Nonnull;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.io.IOException;

public enum SheetMusicUtil
{
    ;

    public static final String KEY_SHEET_MUSIC = "SheetMusic";
    public static final String KEY_DURATION = "Duration";
    public static final String KEY_MML = "MML";
    public static final String ITEM_INVENTORY = "ItemInventory";

    public static String getMusicTitle(ItemStack stackIn)
    {
        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(stackIn);
        if (!sheetMusic.isEmpty() && sheetMusic.getTagCompound() != null)
        {
            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag(KEY_SHEET_MUSIC);
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
        if ((stackIn.getItem() instanceof IInstrument) && stackIn.getTagCompound() != null)
        {
            NBTTagList items = stackIn.getTagCompound().getTagList(ITEM_INVENTORY, Constants.NBT.TAG_COMPOUND);
            if (items.tagCount() == 1)
            {
                NBTTagCompound item = items.getCompoundTagAt(0);
                ItemStack sheetMusic = new ItemStack(item);
                if (sheetMusic.getItem() instanceof IMusic && sheetMusic.getTagCompound() != null)
                {
                    NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag(KEY_SHEET_MUSIC);
                    if (contents.hasKey(KEY_MML))
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
        ValidDuration validDuration = validateMML(mml);
        if (compound != null && (sheetMusic.getItem() instanceof IMusic) && validDuration.isValidMML() && validDuration.getDuration() > 0)
        {
            NBTTagCompound contents = new NBTTagCompound();
            contents.setString(KEY_MML, mml);
            contents.setInteger(KEY_DURATION, validDuration.getDuration());
            compound.setTag(KEY_SHEET_MUSIC, contents);
            return true;
        }
        return false;
    }

    /**
     * Validate the supplied MML and return it's length in seconds.
     *
     * @param mml to be validated and its duration in seconds calculated.
     * @return a ValidDuration with 'isValidMML' set true for valid MML else false, and 'getDuration' the length of the tune in seconds<B></B>
     * for valid MML, else 0.
     */
    public static ValidDuration validateMML(@Nonnull String mml)
    {
        ParseErrorListener parseErrorListener = new ParseErrorListener();
        int seconds = 0;
        MMLParser parser;
        try
        {
             parser = MMLParserFactory.getMMLParser(mml);
        }
        catch (IOException e)
        {
            ModLogger.debug("MMLParserFactory.getMMLParser() IOException in %s, Error: %s", SheetMusicUtil.class.getSimpleName(), e);
            return ValidDuration.INVALID;
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
                return ValidDuration.INVALID;
            }
        }
        ModLogger.info("ValidateMML: valid: %s, length: %d", parseErrorListener.getParseErrorEntries().isEmpty(), seconds);
        return new ValidDuration(parseErrorListener.getParseErrorEntries().isEmpty(), seconds);
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

    public static String getInventoryInstrumentBlockMML(TileEntity tileEntity)
    {
        StringBuilder buildMML = new StringBuilder();

        if (tileEntity instanceof IMusicPlayer)
        {
            IMusicPlayer musicPlayer =(IMusicPlayer) tileEntity;
            try
            {
                for (int slot = 0; slot < musicPlayer.getInventory().getSlots(); slot++)
                {
                    ItemStack stackInSlot = musicPlayer.getInventory().getStackInSlot(slot);
                    if (!stackInSlot.isEmpty() && stackInSlot.getItem() instanceof ItemInstrument)
                    {
                        ItemInstrument instrument = (ItemInstrument) stackInSlot.getItem();
                        int patch = instrument.getPatch(stackInSlot.getMetadata());
                        ItemStack sheetMusic = getSheetMusic(stackInSlot);
                        if (!sheetMusic.isEmpty() && sheetMusic.getTagCompound() != null)
                        {
                            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag(KEY_SHEET_MUSIC);
                            if (contents.hasKey(KEY_MML))
                            {
                                String mml = contents.getString(KEY_MML);
                                mml = mml.replace("MML@", "MML@I" + patch);
                                buildMML.append(slot).append("=").append(mml).append("|");
                            }
                        }
                    }
                }
            } catch (Exception e)
            {
                ModLogger.error(e);
            }
        }
        return buildMML.toString();
    }
}
