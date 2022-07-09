package aeronicamc.mods.mxtune.util;

import aeronicamc.libs.mml.parser.MMLParser;
import aeronicamc.libs.mml.parser.MMLParserFactory;
import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.IMusicPlayer;
import aeronicamc.mods.mxtune.caches.ModDataStore;
import aeronicamc.mods.mxtune.config.MXTuneConfig;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.init.ModSoundEvents;
import aeronicamc.mods.mxtune.inventory.MultiInstInventory;
import aeronicamc.mods.mxtune.items.MultiInstItem;
import aeronicamc.mods.mxtune.items.SheetMusicItem;
import aeronicamc.mods.mxtune.sound.MMLToMIDI;
import aeronicamc.mods.mxtune.sound.Midi2WavRenderer;
import aeronicamc.mods.mxtune.sound.ModMidiException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.lang.Thread.sleep;
import static net.minecraftforge.common.util.Constants.NBT;

public enum SheetMusicHelper
{
    ;
    private static final Logger LOGGER = LogManager.getLogger(SheetMusicHelper.class);
    public static final String KEY_SHEET_MUSIC = "SheetMusic";
    public static final String KEY_DURATION = "Duration";
    public static final String KEY_MUSIC_TEXT_KEY = "MusicTextKey";
    private final static ITextComponent SHEET_MUSIC_EMPTY =
            new TranslationTextComponent("tooltip.mxtune.sheet_music.empty")
                    .withStyle(TextFormatting.ITALIC)
                    .withStyle(TextFormatting.RED);
    private final static ITextComponent SHEET_MUSIC_DURATION_ERROR =
            new TranslationTextComponent("tooltip.mxtune.sheet_music.duration_error")
                    .withStyle(TextFormatting.RED);
    private final static String SHEET_MUSIC_DAYS_LEFT = "tooltip.mxtune.sheet_music.days_left";

    private final static ITextComponent SHEET_MUSIC_DAYS_LEFT_ERROR =
            new TranslationTextComponent("tooltip.mxtune.sheet_music.days_left_error")
                    .withStyle(TextFormatting.RED);


    /**
     * Client Side
     * <p></p>Returns the TITLE from the handheld IMusic item.
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

    /**
     * Client Side
     * <p></p>Get the sheet music title in plain string format
     * @param sheetMusicStack The sheet music stack.
     * @return The title as a string.
     */
    public static String getMusicTitleAsString(ItemStack sheetMusicStack)
    {
        return getFormattedMusicTitle(sheetMusicStack).plainCopy().getString();
    }

    /**
     * Client side
     * <p></p>Get formatted music duration as a {@link ITextComponent} from the sheet music stack.
     * @param sheetMusicStack The sheet music stack.
     * @return The formatted music duration as a {@link ITextComponent}
     */
    public static ITextComponent getFormattedMusicDuration(ItemStack sheetMusicStack)
    {
        int duration = getMusicDuration(sheetMusicStack);
        if (duration > 0)
        {
            return new StringTextComponent(formatDuration(duration)).withStyle(TextFormatting.YELLOW);
        }

        return SHEET_MUSIC_DURATION_ERROR;
    }

    /**
     * CLient or Server Side
     * <p></p>Get the duration in seconds from a sheet music stack. (not validated here)
     * @param sheetMusicStack The sheet music stack.
     * @return The stored duration in seconds if found or 0. (The minimum duration is 4 seconds if validated)
     */
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

    /**
     * Client Side
     * <p></p>Get the formatted days left as a {@link ITextComponent} from the sheet music.
     * @param sheetMusicStack The sheet music stack.
     * @return The formatted days left as a {@link ITextComponent}
     */
    public static ITextComponent getFormattedSheetMusicDaysLeft(ItemStack sheetMusicStack)
    {
        long daysLeft = getSheetMusicDaysLeft(sheetMusicStack);
        if (daysLeft != 99999)
            return new TranslationTextComponent(SHEET_MUSIC_DAYS_LEFT, getSheetMusicDaysLeft(sheetMusicStack))
                .withStyle(TextFormatting.GRAY)
                .withStyle(TextFormatting.ITALIC);
        else
            return SHEET_MUSIC_DAYS_LEFT_ERROR;
    }

    /**
     * Client or Server Side
     * <p></p>Get the sheet music days left as a {@link long}.
     * @param sheetMusicStack The sheet music stack.
     * @return days left or 99999 if there was a parse or item stack error.
     */
    public static long getSheetMusicDaysLeft(ItemStack sheetMusicStack)
    {
        String keyDateTimeString = getMusicTextKey(sheetMusicStack);
        if (keyDateTimeString != null)
        {
            LocalDateTime keyDateTime;
            try
            {
                keyDateTime = LocalDateTime.parse(keyDateTimeString);
            } catch (DateTimeException e)
            {
                return 99999;
            }
            LocalDateTime keyPlusDaysLeft = keyDateTime.plusDays(MXTuneConfig.getSheetMusicLifeInDays());
            LocalDateTime now = LocalDateTime.now(ZoneId.of("GMT0"));
            return Math.max(Duration.between(now, keyPlusDaysLeft).getSeconds() / 86400L, 0L);
        }
        return 99999;
    }

    /**
     * Client Side
     * <p></p>Get the Sheet Music key for days left calculations.
     * @param sheetMusicStack SheetMusic of course
     * @return The LocalDateTime key in string format or null if not available.
     */
    @Nullable
    public static String getMusicTextKey(ItemStack sheetMusicStack)
    {
        if (hasMusicText(sheetMusicStack))
        {
            CompoundNBT contents = sheetMusicStack.getTag();
            if (contents != null && contents.contains(KEY_SHEET_MUSIC))
            {
                CompoundNBT sm = contents.getCompound(KEY_SHEET_MUSIC);
                return sm.contains(KEY_MUSIC_TEXT_KEY) && !sm.getString(KEY_MUSIC_TEXT_KEY).isEmpty() ? sm.getString(KEY_MUSIC_TEXT_KEY) : null;
            }
        }
        return null;
    }

    /**
     * Client side
     * <p></p>Used in the HooverText methods
     * @param sheetMusicStack to be tested
     * @return true if the ItemStack has music
     */
    public static boolean hasMusicText(ItemStack sheetMusicStack)
    {
        CompoundNBT contents = sheetMusicStack.getTag();
        if (contents != null && sheetMusicStack.getItem() instanceof IMusic && contents.contains(KEY_SHEET_MUSIC))
        {
            CompoundNBT sm = contents.getCompound(KEY_SHEET_MUSIC);
            return sm.contains(KEY_DURATION) && (sm.getInt(KEY_DURATION) > 0);
        }
        return false;
    }

    /**
     * Server Side
     * <p></p>Get the combined musicText in MML@ paste format from TileEntity inventory.
     * @param pTileEntity of type {@link IMusicPlayer}
     * @return A MusicProperties tuple that contains the musicText and duration.
     */
    public static MusicProperties getMusicFromIMusicPlayer(TileEntity pTileEntity)
    {
        if (!(pTileEntity instanceof IMusicPlayer)) return MusicProperties.INVALID;
        StringBuilder buildMML = new StringBuilder();
        IMusicPlayer musicPlayer = (IMusicPlayer) pTileEntity;
        final int[] duration = new int[1];

        musicPlayer.getItemHandler().ifPresent(inventory -> {
            for (int slot = 0; slot < inventory.getSlots(); slot++)
                {
                    ItemStack stackInSlot = inventory.getStackInSlot(slot);
                    if (!stackInSlot.isEmpty() && stackInSlot.getItem() instanceof IInstrument)
                    {
                        IInstrument instrument = (IInstrument) stackInSlot.getItem();
                        int patch = instrument.getPatch(stackInSlot);
                        ItemStack sheetMusic = getIMusicFromIInstrument(stackInSlot);
                        if (!sheetMusic.isEmpty() && sheetMusic.getTag() != null)
                        {
                            CompoundNBT contents = (CompoundNBT) sheetMusic.getTag().get(KEY_SHEET_MUSIC);
                            if (contents != null && contents.contains(KEY_MUSIC_TEXT_KEY, NBT.TAG_STRING))
                            {
                                String keyMusicTextKey = contents.getString(KEY_MUSIC_TEXT_KEY);
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
                                duration[0] = Math.max(duration[0], contents.getInt(KEY_DURATION));
                            }
                        }
                    }
                }
        });
        return new MusicProperties(buildMML.toString(), duration[0]);
    }

    // TODO: when placed instruments are created.
    public static ItemStack getIMusicFromIPlacedInstrument(BlockPos pos, PlayerEntity playerIn, boolean isPlaced)
    {
        return ItemStack.EMPTY; // TODO: rewrite tile instrument inventory slot queries
    }

    /**
     * Server Side
     * <p></p>Returns the IMusic stack of 1 from an IInstrument Stack of 1
     * e.g. ItemSheetMusic from an ItemMultiInst
     * Validates the existence of keys only
     * @param pInstrumentStack of handheld IInstrument inventory
     * @return ItemStack of handheld IMusic
     */
    public static ItemStack getIMusicFromIInstrument(ItemStack pInstrumentStack)
    {
        if ((pInstrumentStack.getItem() instanceof IInstrument) && pInstrumentStack.getTag() != null)
        {
            ListNBT items = pInstrumentStack.getTag().getList("Items", NBT.TAG_COMPOUND);
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

    /**
     * Server Side
     * <p></p>Updates a new Sheet Music ItemStack. Adds the title, duration and keystore id to the it and stores
     * the data to an on-disk data store.
     * @param sheetMusic ItemStack reference to be updated.
     * @param musicTitle becomes the Sheet Music display name
     * @param musicText MML to be saved to disk
     * @return true on success.
     */
    public static boolean writeSheetMusic(ItemStack sheetMusic, String musicTitle, String musicText)
    {
        musicTitle = musicTitle.substring(0, Math.min(musicTitle.length(), Reference.MXT_SONG_TITLE_LENGTH));
        sheetMusic.setHoverName(new StringTextComponent(musicTitle));
        CompoundNBT compound = sheetMusic.getTag();
        ValidDuration validDuration = validateMML(musicText);
        if (compound != null && (sheetMusic.getItem() instanceof IMusic) && validDuration.isValidMML() && validDuration.getDuration() > 0)
        {
            String musicTextKey = ModDataStore.addMusicText(musicText);
            if (musicTextKey != null)
            {
                CompoundNBT contents = new CompoundNBT();
                contents.putString(KEY_MUSIC_TEXT_KEY, musicTextKey);
                contents.putInt(KEY_DURATION, validDuration.getDuration());
                compound.put(KEY_SHEET_MUSIC, contents);
                return true;
            }
        }
        return false;
    }

    /**
     * Server Side
     * <p></p>Scraps expired sheet music from the players inventory and replaces it with two paper scraps.
     * @param pStack        The sheet music stack
     * @param pLevel        The level
     * @param pEntity       The player
     * @param pItemSlot     The slot holding the sheet music
     * @param pIsSelected   True if selected
     */
    public static void scrapSheetMusicIfExpired(ItemStack pStack, World pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected)
    {
        if (!pLevel.isClientSide() && !pStack.isEmpty() && (pEntity instanceof PlayerEntity) && !pIsSelected)
        {
            String key = getMusicTextKey(((PlayerEntity) pEntity).inventory.getItem(pItemSlot));
            boolean canReap = getSheetMusicDaysLeft(pStack) == 0L;
            if (key != null && canReap)
            {
                ModDataStore.removeSheetMusic(key);
                ((PlayerEntity) pEntity).inventory.removeItem(pStack);
                Misc.audiblePingPlayer((PlayerEntity)pEntity, ModSoundEvents.CRUMPLE_PAPER.get());

                SidedThreadGroups.SERVER.newThread(()->{
                    try
                    {
                        sleep(RandomUtils.nextLong(400, 600));
                    } catch (InterruptedException e)
                    {
                        LOGGER.warn(e);
                    }
                    if (!((PlayerEntity) pEntity).inventory.add(pItemSlot, new ItemStack(ModItems.SCRAP_ITEM.get(), 1))){
                        ((PlayerEntity) pEntity).drop(new ItemStack(ModItems.SCRAP_ITEM.get(), 1), true, true);
                    }
                    Misc.audiblePingPlayer((PlayerEntity)pEntity, ModSoundEvents.CRUMPLE_PAPER.get());
                }).start();
                SidedThreadGroups.SERVER.newThread(()->{
                    try
                    {
                        sleep(RandomUtils.nextLong(600, 700));
                    } catch (InterruptedException e)
                    {
                        LOGGER.warn(e);
                    }
                    if (!((PlayerEntity) pEntity).inventory.add(pItemSlot, new ItemStack(ModItems.SCRAP_ITEM.get(), 1))){
                        ((PlayerEntity) pEntity).drop(new ItemStack(ModItems.SCRAP_ITEM.get(), 1), true, true);
                    }
                    Misc.audiblePingPlayer((PlayerEntity)pEntity, ModSoundEvents.CRUMPLE_PAPER.get());
                }).start();
            }
        }
    }

    /**
     * * Server Side
     * <p></p>Check for an expired {@link SheetMusicItem} stack in a {@link IInstrument}/{@link MultiInstItem}. If expired it
     * is removed from the instrument and the associated music text from the data store, then two
     * {@link ModItems#SCRAP_ITEM} are dropped into either the player inventory or into the world depending on the context.
     * @param slot The {@link Slot} if provided. Can be null
     * @param pInstrumentStack The {@link IInstrument}/{@link MultiInstItem} stack
     * @param pLevel  The world
     * @param pEntity The Player whose inventory will receive the items.
     * @param blockPos The {@link BlockPos} of the {@link IMusicPlayer if provided. Can be null
     */
    public static void scrapSheetMusicInInstrumentIfExpired(@Nullable Slot slot, ItemStack pInstrumentStack, World pLevel, Entity pEntity, @Nullable BlockPos blockPos)
    {
        if (!pLevel.isClientSide() && !pInstrumentStack.isEmpty() && (pEntity instanceof PlayerEntity) && !pEntity.isSpectator())
        {
            int multiplier = 0;
            ItemStack sheetMusic = removeSheetMusicFromIInstrument(pInstrumentStack);
            String key = getMusicTextKey(sheetMusic);
            boolean canReap = getSheetMusicDaysLeft(sheetMusic) == 0L;

            if (key != null && canReap) {
                if (slot != null) {
                    multiplier += slot.index;
                    slot.setChanged();
                }
                ModDataStore.removeSheetMusic(key);
                Misc.audiblePingPlayer((PlayerEntity)pEntity, ModSoundEvents.CRUMPLE_PAPER.get());

                int finalMultiplier = multiplier;
                SidedThreadGroups.SERVER.newThread(()->{
                    try
                    {
                        sleep(RandomUtils.nextLong(400 + (50 * finalMultiplier), 600 + (50 * finalMultiplier)));
                    } catch (InterruptedException e)
                    {
                        LOGGER.warn("Interrupted!", e);
                        // Restore interrupted state...
                        Thread.currentThread().interrupt();
                    }
                    if (blockPos != null)
                    {
                        InventoryHelper.dropItemStack(pLevel, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ModItems.SCRAP_ITEM.get()));
                    }
                    else if (!((PlayerEntity) pEntity).inventory.add(new ItemStack(ModItems.SCRAP_ITEM.get(), 1)))
                    {
                        ((PlayerEntity) pEntity).drop(new ItemStack(ModItems.SCRAP_ITEM.get(), 1), true, true);
                    }
                    Misc.audiblePingPlayer((PlayerEntity)pEntity, ModSoundEvents.CRUMPLE_PAPER.get());
                }).start();
                SidedThreadGroups.SERVER.newThread(()->{
                    try
                    {
                        sleep(RandomUtils.nextLong(600 + (50 * finalMultiplier), 800 + (50 * finalMultiplier)));
                    } catch (InterruptedException e)
                    {
                        LOGGER.warn("Interrupted!", e);
                        // Restore interrupted state...
                        Thread.currentThread().interrupt();
                    }
                    if (blockPos != null && pLevel.getBlockState(blockPos).hasTileEntity() && pLevel.getBlockEntity(blockPos) instanceof IMusicPlayer)
                    {
                        InventoryHelper.dropItemStack(pLevel, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ModItems.SCRAP_ITEM.get()));
                    }
                    else if (!((PlayerEntity) pEntity).inventory.add(new ItemStack(ModItems.SCRAP_ITEM.get(), 1)))
                    {
                        ((PlayerEntity) pEntity).drop(new ItemStack(ModItems.SCRAP_ITEM.get(), 1), true, true);
                    }
                    Misc.audiblePingPlayer((PlayerEntity)pEntity, ModSoundEvents.CRUMPLE_PAPER.get());
                }).start();
            }
        }
    }

    /**
     * Server Side
     * <p></p>Remove a {@link SheetMusicItem} stack from an {@link IInstrument}/{@link MultiInstItem}
     * @param pStack The {@link IInstrument}/{@link MultiInstItem} stack
     * @return a {@link SheetMusicItem} stack or empty stack
     */
    private static ItemStack removeSheetMusicFromIInstrument(ItemStack pStack)
    {
        ItemStack sheetMusic = SheetMusicHelper.getIMusicFromIInstrument(pStack);
        if (!sheetMusic.isEmpty() && SheetMusicHelper.getSheetMusicDaysLeft(sheetMusic) == 0)
        {
            MultiInstInventory inv = new MultiInstInventory(pStack);
            ItemStack stack = inv.removeItem(0, 1);
            inv.setChanged();
            return stack.copy();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Client and Server side
     * <p></p>Validate the supplied MML and return it's length in seconds.
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
            LOGGER.error("ValidateMML Error: {}", e.getLocalizedMessage());
            return ValidDuration.INVALID;
        }

        return new ValidDuration(seconds > 4, seconds);
    }

    /**
     * Client or Server side
     * <p></p>Get formatted music duration as a {@link String} from the sheet music stack.
     * @param seconds The duration in seconds
     * @return The formatted music duration as a {@link String} formatted as "h:mm:ss"
     */
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

    // TODO: When music mobs are created and can drop sheet music.
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

    // TODO: When music mobs are created and can drop sheet music.
//    public static ItemStack createSheetMusic(SheetMusicSongs sheetMusicSong)
//    {
//        return createSheetMusic(sheetMusicSong.getTitle(), sheetMusicSong.getMML());
//    }

}
