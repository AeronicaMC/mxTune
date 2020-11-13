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

import net.aeronica.libs.mml.parser.MMLParser;
import net.aeronica.libs.mml.parser.MMLParserFactory;
import net.aeronica.mods.mxtune.blocks.IMusicPlayer;
import net.aeronica.mods.mxtune.blocks.IPlacedInstrument;
import net.aeronica.mods.mxtune.blocks.TileInstrument;
import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.inventory.IMusic;
import net.aeronica.mods.mxtune.sound.MMLToMIDI;
import net.aeronica.mods.mxtune.sound.Midi2WavRenderer;
import net.aeronica.mods.mxtune.sound.ModMidiException;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

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
        int seconds = 0;
        MMLParser parser = MMLParserFactory.getMMLParser(mml);
        MMLToMIDI toMIDI = new MMLToMIDI();
        toMIDI.processMObjects(parser.getMmlObjects());

        try (Midi2WavRenderer midi2WavRenderer = new Midi2WavRenderer())
        {
            // sequence in seconds plus 4 a second buffer. Same as the MIDI2WaveRenderer class.
            seconds = (int) (midi2WavRenderer.getSequenceInSeconds(toMIDI.getSequence()) + 4);
        } catch (ModMidiException e)
        {
            ModLogger.debug("ValidateMML Error: %s in %s", e, SheetMusicUtil.class.getSimpleName());
            return ValidDuration.INVALID;
        }

        ModLogger.debug("ValidateMML: length: %d", seconds);
        return new ValidDuration(seconds > 4, seconds);
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

    /**
     * Reads an inventory of instruments containing sheet music and returns MML text ready for parsing.
     * The MML text is concatenated with instrument patch ids inserted into each part.
     * @param tileEntity instance of IMusicPlayer
     * @return MML text ready for parsing
     */
    public static String getInventoryInstrumentBlockMML(TileEntity tileEntity)
    {
        if (!(tileEntity instanceof IMusicPlayer)) return "";

        StringBuilder buildMML = new StringBuilder();
        IMusicPlayer musicPlayer = (IMusicPlayer) tileEntity;
        for (int slot = 0; slot < musicPlayer.getInventory().getSlots(); slot++)
        {
            ItemStack stackInSlot = musicPlayer.getInventory().getStackInSlot(slot);
            if (!stackInSlot.isEmpty() && stackInSlot.getItem() instanceof IInstrument)
            {
                IInstrument instrument = (IInstrument) stackInSlot.getItem();
                int patch = instrument.getPatch(stackInSlot);
                ItemStack sheetMusic = getSheetMusic(stackInSlot);
                if (!sheetMusic.isEmpty() && sheetMusic.getTagCompound() != null)
                {
                    NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag(KEY_SHEET_MUSIC);
                    if (contents.hasKey(KEY_MML))
                    {
                        String mml = contents.getString(KEY_MML);
                        mml = mml.replace("MML@", "MML@I" + patch);
                        buildMML.append(mml);
                    }
                }
            }
        }
        return buildMML.toString();
    }

    public static ItemStack createSheetMusic(String title, String mml)
    {
        ItemStack sheetMusic = new ItemStack(ModItems.ITEM_SHEET_MUSIC);
        if (SheetMusicUtil.writeSheetMusic(sheetMusic, title, mml))
        {
            return sheetMusic;
        }
        else
            return new ItemStack(ModItems.ITEM_MUSIC_PAPER);
    }

    public static ItemStack createSheetMusic(SheetMusicSongs sheetMusicSong)
    {
        return createSheetMusic(sheetMusicSong.getTitle(), sheetMusicSong.getMML());
    }
}
