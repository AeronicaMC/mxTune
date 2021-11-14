package aeronicamc.mods.mxtune.util;

import aeronicamc.libs.mml.parser.MMLParser;
import aeronicamc.libs.mml.parser.MMLParserFactory;
import aeronicamc.mods.mxtune.blocks.IMusicPlayer;
import aeronicamc.mods.mxtune.caches.ModDataStore;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.sound.MMLToMIDI;
import aeronicamc.mods.mxtune.sound.Midi2WavRenderer;
import aeronicamc.mods.mxtune.sound.ModMidiException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

import static net.minecraftforge.common.util.Constants.NBT;

public enum SheetMusicHelper
{
    ;
    private static final Logger LOGGER = LogManager.getLogger(SheetMusicHelper.class);
    public static final String KEY_SHEET_MUSIC = "SheetMusic";
    public static final String KEY_DURATION = "Duration";
    public static final String KEY_MUSIC_TEXT_KEY = "MusicTextKey";
    private final static ITextComponent SHEET_MUSIC_EMPTY =
            new TranslationTextComponent("item.mxtune.sheet_music.empty")
                    .withStyle(TextFormatting.ITALIC)
                    .withStyle(TextFormatting.RED);
    private final static ITextComponent SHEET_MUSIC_DURATION_ERROR =
            new TranslationTextComponent("item.mxtune.sheet_music.duration_error")
                    .withStyle(TextFormatting.RED);


    /**
     * Returns the TITLE from the handheld IMusic item.
     * e.g. Returns TITLE from ItemSheetMusic
     * @param sheetMusicStack IInstrument music ItemStack
     * @return hoover name of the IMusic ItemStack
     */
    public static ITextComponent getFormattedMusicTitle(ItemStack sheetMusicStack)
    {
        CompoundNBT contents = sheetMusicStack.getTag();
        if (contents != null && contents.contains(KEY_SHEET_MUSIC))
        {
            return new StringTextComponent(sheetMusicStack.getHoverName().getString()).withStyle(TextFormatting.GOLD);
        }
        return SHEET_MUSIC_EMPTY;
    }

    public static String getMusicTitleAsString(ItemStack sheetMusicStack)
    {
        return getFormattedMusicTitle(sheetMusicStack).plainCopy().getString();
    }

    public static ITextComponent getFormattedMusicDuration(ItemStack sheetMusicStack)
    {
        int duration = getMusicDuration(sheetMusicStack);
        if (duration > 0)
        {
            return new StringTextComponent(formatDuration(duration)).withStyle(TextFormatting.YELLOW);
        }

        return SHEET_MUSIC_DURATION_ERROR;
    }

    public static int getMusicDuration(ItemStack sheetMusicStack)
    {
        CompoundNBT contents = sheetMusicStack.getTag();
        if (contents != null && contents.contains(KEY_SHEET_MUSIC))
        {
            CompoundNBT sm = contents.getCompound(KEY_SHEET_MUSIC);
            if ((sm.contains(KEY_MUSIC_TEXT_KEY) && sm.getInt(KEY_DURATION) > 0))
            {
                return sm.getInt(KEY_DURATION);
            }
        }

        return 0;
    }

    @Nullable
    public static Integer getMusicTextKey(ItemStack sheetMusicStack)
    {
        if (hasMusicText(sheetMusicStack))
        {
            CompoundNBT contents = sheetMusicStack.getTag();
            if (contents != null && contents.contains(KEY_SHEET_MUSIC))
            {
                CompoundNBT sm = contents.getCompound(KEY_SHEET_MUSIC);
                return sm.contains(KEY_MUSIC_TEXT_KEY) ? sm.getInt(KEY_MUSIC_TEXT_KEY) : null;
            }
        }
        return null;
    }

    public static MusicProperties getMusicFromIMusicPlayer(TileEntity pTileEntity)
    {
        if (!(pTileEntity instanceof IMusicPlayer)) return MusicProperties.INVALID;
        StringBuilder buildMML = new StringBuilder();
        int duration = 0;
        IMusicPlayer musicPlayer = (IMusicPlayer) pTileEntity;
        int slotCount = musicPlayer.getInventory() != null ? musicPlayer.getInventory().getSlots() : 0;
        for (int slot = 0; slot < slotCount; slot++)
        {
            ItemStack stackInSlot = musicPlayer.getInventory().getStackInSlot(slot);
            if (!stackInSlot.isEmpty() && stackInSlot.getItem() instanceof IInstrument)
            {
                IInstrument instrument = (IInstrument) stackInSlot.getItem();
                int patch = instrument.getPatch(stackInSlot);
                ItemStack sheetMusic = getIMusicFromIInstrument(stackInSlot);
                if (!sheetMusic.isEmpty() && sheetMusic.getTag() != null)
                {
                    CompoundNBT contents = (CompoundNBT) sheetMusic.getTag().get(KEY_SHEET_MUSIC);
                    if (contents != null && contents.contains(KEY_MUSIC_TEXT_KEY, NBT.TAG_INT))
                    {
                        int keyMusicTextKey = contents.getInt(KEY_MUSIC_TEXT_KEY);
                        String musicText = ModDataStore.getMusicText(keyMusicTextKey);
                        if (musicText != null && musicText.contains("MML@"))
                        {
                            musicText = musicText.replace("MML@", "MML@I" + patch);
                        }
                        else
                        {
                            musicText = "";
                        }
                        buildMML.append(musicText);
                        duration = Math.max(duration, contents.getInt(KEY_DURATION));
                    }

                }
            }
        }
        return new MusicProperties(buildMML.toString(), duration);
    }

    public static boolean hasMusicText(ItemStack sheetMusicStack)
    {
        CompoundNBT contents = sheetMusicStack.getTag();
        if (contents != null && sheetMusicStack.getItem() instanceof IMusic && contents.contains(KEY_SHEET_MUSIC))
        {
            CompoundNBT sm = contents.getCompound(KEY_SHEET_MUSIC);
            return (sm.getInt(KEY_DURATION) > 0) && (ModDataStore.getMusicText(sm.getInt(KEY_MUSIC_TEXT_KEY)) != null);
        }
        return false;
    }

    public static ItemStack getIMusicFromIPlacedInstrument(BlockPos pos, PlayerEntity playerIn, boolean isPlaced)
    {
        return ItemStack.EMPTY; // TODO: rewrite tile instrument inventory slot queries
    }

    /**
     * Returns the IMusic stack of 1 from an IInstrument Stack of 1
     * e.g. ItemSheetMusic from an ItemMultiInst
     * Validates the existance of keys only
     * @param iInstStack of handheld IInstrument inventory
     * @return ItemStack of handheld IMusic
     */
    public static ItemStack getIMusicFromIInstrument(ItemStack iInstStack)
    {
        if ((iInstStack.getItem() instanceof IInstrument) && iInstStack.getTag() != null)
        {
            ListNBT items = iInstStack.getTag().getList("Items", NBT.TAG_COMPOUND);
            if (items.size() == 1)
            {
                CompoundNBT item = items.getCompound(0);
                ItemStack sheetMusic = ItemStack.of(item);
                if (sheetMusic.getItem() instanceof IMusic && sheetMusic.getTag() != null)
                {
                    CompoundNBT contents = (CompoundNBT) sheetMusic.getTag().get(KEY_SHEET_MUSIC);
                    if (contents != null && contents.contains(KEY_MUSIC_TEXT_KEY))
                    {
                        return sheetMusic;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean writeSheetMusic(ItemStack sheetMusic, String musicTitle, String musicText)
    {
        sheetMusic.setHoverName(new StringTextComponent(musicTitle));
        CompoundNBT compound = sheetMusic.getTag();
        ValidDuration validDuration = validateMML(musicText);
        if (compound != null && (sheetMusic.getItem() instanceof IMusic) && validDuration.isValidMML() && validDuration.getDuration() > 0)
        {
            Integer musicTextKey = ModDataStore.addMusicText(musicText);
            if (musicTextKey != null)
            {
                CompoundNBT contents = new CompoundNBT();
                contents.putInt(KEY_MUSIC_TEXT_KEY, musicTextKey);
                contents.putInt(KEY_DURATION, validDuration.getDuration());
                compound.put(KEY_SHEET_MUSIC, contents);
                return true;
            }
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
    public static ValidDuration validateMML(@Nullable String mml)
    {
        int seconds;
        if (mml == null)
            return ValidDuration.INVALID;
        MMLParser parser = MMLParserFactory.getMMLParser(mml);
        MMLToMIDI toMIDI = new MMLToMIDI();
        toMIDI.processMObjects(parser.getMmlObjects());

        try (Midi2WavRenderer midi2WavRenderer = new Midi2WavRenderer())
        {
            // sequence in seconds plus 4 a second buffer. Same as the MIDI2WaveRenderer class.
            seconds = (int) (midi2WavRenderer.getSequenceInSeconds(toMIDI.getSequence()) + 4);
        } catch (ModMidiException e)
        {
            LOGGER.debug("ValidateMML Error: {}", e.getLocalizedMessage());
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

    public static ItemStack createSheetMusic(String title, String mml)
    {
        ItemStack sheetMusic = new ItemStack(ModItems.SHEET_MUSIC.get());
        if (SheetMusicHelper.writeSheetMusic(sheetMusic, title, mml))
        {
            return sheetMusic;
        }
        else
            return new ItemStack(ModItems.MUSIC_PAPER.get());
    }

    public static int getMusicIndex(ItemStack itemStack)
    {
        return 0;
    }

//    public static ItemStack createSheetMusic(SheetMusicSongs sheetMusicSong)
//    {
//        return createSheetMusic(sheetMusicSong.getTitle(), sheetMusicSong.getMML());
//    }


}
