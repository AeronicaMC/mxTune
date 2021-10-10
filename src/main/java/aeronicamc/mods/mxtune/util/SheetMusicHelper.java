package aeronicamc.mods.mxtune.util;

import aeronicamc.libs.mml.parser.MMLParser;
import aeronicamc.libs.mml.parser.MMLParserFactory;
import aeronicamc.mods.mxtune.sound.MMLToMIDI;
import aeronicamc.mods.mxtune.sound.Midi2WavRenderer;
import aeronicamc.mods.mxtune.sound.ModMidiException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static aeronicamc.mods.mxtune.Reference.*;
import static net.minecraftforge.common.util.Constants.NBT;

public enum SheetMusicHelper
{
    ;
    private static final Logger LOGGER = LogManager.getLogger(SheetMusicHelper.class);
    /**
     * getMusicTitle(ItemStack pStack)
     * @param pStack Sheet music ItemStack
     * @return translation id
     */
    public static String getMusicTitle(ItemStack pStack)
    {
        ItemStack sheetMusic = SheetMusicHelper.getSheetMusic(pStack);
        if (!sheetMusic.isEmpty() && sheetMusic.hasTag() && sheetMusic.getTag() != null)
        {
            CompoundNBT contents = (CompoundNBT) sheetMusic.getTag().get(KEY_SHEET_MUSIC);
            if (contents != null && !contents.isEmpty())
            {
                return sheetMusic.getDescriptionId();
            }
        }
        return "";
    }

    public static ItemStack getSheetMusic(BlockPos pos, PlayerEntity playerIn, boolean isPlaced)
    {
        return ItemStack.EMPTY; // TODO: rewrite tile instrument inventory slot queries
    }

    public static ItemStack getSheetMusic(ItemStack stackIn)
    {
        if ((stackIn.getItem() instanceof IInstrument) && stackIn.getTag() != null)
        {
            ListNBT items = stackIn.getTag().getList(ITEM_INVENTORY, NBT.TAG_COMPOUND);
            if (items.size() == 1)
            {
                CompoundNBT item = items.getCompound(0);
                ItemStack sheetMusic = ItemStack.of(item);
                if (sheetMusic.getItem() instanceof IMusic && sheetMusic.getTag() != null)
                {
                    CompoundNBT contents = (CompoundNBT) sheetMusic.getTag().get(KEY_SHEET_MUSIC);
                    if (contents != null && contents.contains(KEY_MML))
                    {
                        return sheetMusic;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean writeSheetMusic(ItemStack sheetMusic, String musicTitle, String mml)
    {
        sheetMusic.setHoverName(new StringTextComponent(musicTitle));
        CompoundNBT compound = sheetMusic.getTag();
        ValidDuration validDuration = validateMML(mml);
        if (compound != null && (sheetMusic.getItem() instanceof IMusic) && validDuration.isValidMML() && validDuration.getDuration() > 0)
        {
            CompoundNBT contents = new CompoundNBT();
            contents.putString(KEY_MML, mml);
            contents.putInt(KEY_DURATION, validDuration.getDuration());
            compound.put(KEY_SHEET_MUSIC, contents);
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
    public static ValidDuration validateMML(String mml)
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
            LOGGER.debug("ValidateMML Error: {} in {}", e, SheetMusicHelper.class.getSimpleName());
            return ValidDuration.INVALID;
        }

        LOGGER.debug("ValidateMML: length: {}", seconds);
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

//    public static ItemStack createSheetMusic(String title, String mml)
//    {
//        ItemStack sheetMusic = new ItemStack(ModItems.ITEM_SHEET_MUSIC);
//        if (SheetMusicHelper.writeSheetMusic(sheetMusic, title, mml))
//        {
//            return sheetMusic;
//        }
//        else
//            return new ItemStack(ModItems.ITEM_MUSIC_PAPER);
//    }

//    public static ItemStack createSheetMusic(SheetMusicSongs sheetMusicSong)
//    {
//        return createSheetMusic(sheetMusicSong.getTitle(), sheetMusicSong.getMML());
//    }
}
