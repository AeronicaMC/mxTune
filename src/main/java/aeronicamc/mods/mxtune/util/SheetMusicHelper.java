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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.lang.Thread.sleep;
import static net.minecraftforge.common.util.Constants.NBT;

public class SheetMusicHelper
{
    private static final Logger LOGGER = LogManager.getLogger(SheetMusicHelper.class);
    public static final String KEY_SHEET_MUSIC = "SheetMusic";
    public static final String KEY_EXTRA_DATA = "ExtraData"; // array of int
    public static final String KEY_DURATION = "Duration";
    public static final String KEY_MUSIC_TEXT_KEY = "MusicTextKey";
    public static final String KEY_PARTS_COUNT = "PartsCount"; // list of soundfont proxy indexes.
    public static final String KEY_PART_ID = "Part:"; // individual instrument id as a string. Order as-is.
    public static final String KEY_OWNER_UUID = "OwnerUUID";
    public static final String KEY_OWNER_NAME = "OwnerName";
    private final static ITextComponent SHEET_MUSIC_EMPTY =
            new TranslationTextComponent("tooltip.mxtune.sheet_music.empty")
                    .withStyle(TextFormatting.ITALIC)
                    .withStyle(TextFormatting.RED);
    private final static ITextComponent SHEET_MUSIC_DURATION_ERROR =
            new TranslationTextComponent("tooltip.mxtune.sheet_music.duration_error")
                    .withStyle(TextFormatting.RED);
    private final static String SHEET_MUSIC_DAYS_LEFT = "tooltip.mxtune.sheet_music.days_left";

    private final static ITextComponent MUSIC_SCORE_PART_ERROR =
            new TranslationTextComponent("tooltip.mxtune.music_score.parts_error")
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
     * Get the SoundfontProxy instrument id string
     * @param sheetMusic ItemStack
     * @return SoundProxy instrument id string
     */
    public static String getSheetMusicSoundProxyId(ItemStack sheetMusic)
    {
        CompoundNBT contents = sheetMusic.getTag();
        if (contents != null && contents.contains(KEY_SHEET_MUSIC))
        {
            CompoundNBT sm = contents.getCompound(KEY_SHEET_MUSIC);
            if (sm.contains(KEY_EXTRA_DATA))
            {
                byte[] extraData = sm.getByteArray(KEY_EXTRA_DATA);
                if (extraData.length == 2)
                    return sm.getString(String.format("%s%d",KEY_PART_ID, 0));
            }
        }
        return SoundFontProxyManager.INSTRUMENT_DEFAULT_ID;
    }

    /**
     * Get the SoundfontProxy instrument index
     * @param sheetMusic ItemStack
     * @return SoundProxy instrument index
     */
    public static int getSheetMusicSoundProxyIndex(ItemStack sheetMusic)
    {
        return SoundFontProxyManager.getIndexById(getSheetMusicSoundProxyId(sheetMusic));
    }

    public static String getSuggestedInstrumentId(ItemStack InstrumentStack)
    {
        ItemStack sheetMusic = getIMusicFromIInstrument(InstrumentStack);
        return sheetMusic.isEmpty() ? SoundFontProxyManager.INSTRUMENT_DEFAULT_ID : getSheetMusicSoundProxyId(sheetMusic);
    }

    public static int getSuggestedInstrumentIndex(ItemStack InstrumentStack)
    {
        return SoundFontProxyManager.getIndexById(getSuggestedInstrumentId(InstrumentStack));
    }

    public static ITextComponent getFormattedExtraText(ItemStack sheetMusicStack)
    {
        CompoundNBT contents = sheetMusicStack.getTag();
        if (contents != null && contents.contains(KEY_SHEET_MUSIC))
        {
            CompoundNBT sm = contents.getCompound(KEY_SHEET_MUSIC);
            if (sm.contains(KEY_EXTRA_DATA))
            {
                byte[] extraData = sm.getByteArray(KEY_EXTRA_DATA);
                switch (extraData.length) {
                    case 1:
                        return new TranslationTextComponent("tooltip.mxtune.music_score.n_part_score", extraData[0]).withStyle(TextFormatting.YELLOW);
                    case 2:
                        String instrumentName = new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(sm.getString(String.format("%s%d",KEY_PART_ID, 0)))).getString();
                        return new TranslationTextComponent("tooltip.mxtune.sheet_music.n_of_m_instrument_name", extraData[0], extraData[1], instrumentName).withStyle(TextFormatting.YELLOW);
                    default:
                }
            }
        }
        return StringTextComponent.EMPTY;
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
     * Client or Server Side
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

    public static List<ITextComponent> getFormattedMusicScoreParts(ItemStack musicScoreStack)
    {
        List<ITextComponent> part = new ArrayList<>();
        CompoundNBT contents = musicScoreStack.getTag();
        if (contents != null && contents.contains(KEY_SHEET_MUSIC))
        {
            CompoundNBT sm = contents.getCompound(KEY_SHEET_MUSIC);
            if ((sm.contains(KEY_PARTS_COUNT) && sm.getInt(KEY_PARTS_COUNT) > 0))
            {
                for (int i = 0; i < sm.getInt(KEY_PARTS_COUNT); i++)
                {
                    part.add(new StringTextComponent(String.format("%02d: ", i + 1))
                                     .withStyle(TextFormatting.GRAY)
                                     .append(new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(sm.getString(String.format("%s%d", KEY_PART_ID, i))))
                                                     .withStyle(TextFormatting.YELLOW)));
                }
                return part;
            }
            else
                part.add(MUSIC_SCORE_PART_ERROR);
        }
        else
            part.add(MUSIC_SCORE_PART_ERROR);
        return part;
    }

    /**
     * Client Side
     * <p></p>Get the formatted days left as a {@link ITextComponent} from the sheet music.
     * @param sheetMusicStack The sheet music stack.
     * @return The formatted days left as a {@link ITextComponent}
     */
    public static ITextComponent getFormattedSheetMusicDaysLeft(ItemStack sheetMusicStack)
    {
        int daysLeft = getSheetMusicDaysLeft(sheetMusicStack);
        if (MXTuneConfig.sheetMusicExpires())
            return new TranslationTextComponent(SHEET_MUSIC_DAYS_LEFT, daysLeft)
                .withStyle(TextFormatting.GRAY)
                .withStyle(TextFormatting.ITALIC);
        else
            return StringTextComponent.EMPTY;
    }

    /**
     * Client or Server Side
     * <p></p>Get the sheet music days left as a {@link int}.
     * @param sheetMusicStack The sheet music stack.
     * @return days left or SHEET_MUSIC_MAX_DAYS(999999) if there was a parse or item stack error.
     */
    public static int getSheetMusicDaysLeft(ItemStack sheetMusicStack)
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
                LOGGER.warn("Failure to parse SheetMusic key: {}", keyDateTimeString);
                return MXTuneConfig.SHEET_MUSIC_MAX_DAYS;
            }
            LocalDateTime keyPlusDaysLeft = keyDateTime.plusDays(MXTuneConfig.getSheetMusicLifeInDays());
            LocalDateTime now = LocalDateTime.now(ZoneId.of("GMT0"));
            return MXTuneConfig.sheetMusicExpires() ? (int) Math.max(Duration.between(now, keyPlusDaysLeft).getSeconds() / 86400L, 0L) : MXTuneConfig.SHEET_MUSIC_MAX_DAYS;
        }
        return MXTuneConfig.SHEET_MUSIC_MAX_DAYS;
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
     * Tests if the instrument contains SheetMusic
     * @param instrumentStack MultiInst ItemStack
     * @return true if the instrument holds a valid SheetMusic ItemStack
     */
    public static boolean hasSheetMusic(ItemStack instrumentStack)
    {
        return !getIMusicFromIInstrument(instrumentStack).isEmpty();
    }

    /**
     * Server Side
     * <p></p>Updates a new Sheet Music or Music Score ItemStack. Adds the title, duration and keystore id to it and stores
     * the data to an on-disk data store.
     * @param itemStack ItemStack reference to be updated.
     * @param musicTitle becomes the Sheet Music display name.
     * @param extraData additional information. For SheetMusic multipart tunes: Part n of m; Solo: not used. For MusicScore it would indicate how many parts.
     * @param musicText MML to be saved to storage.
     * @param partInstrumentIds String array of part instrument ids. Must contain 1 id for sheet music, or an id for each part for a music score.
     * @param musicType of IMusic item. PART or SCORE.
     * @param ownerUUID of the person who made the item.
     * @param ownerName of the person who made the item.
     * @return true on success.
     */
    public static boolean writeIMusic(ItemStack itemStack, String musicTitle, byte[] extraData, String musicText, String[] partInstrumentIds, MusicType musicType, UUID ownerUUID, String ownerName)
    {
        musicTitle = musicTitle.substring(0, Math.min(musicTitle.length(), Reference.MXT_SONG_TITLE_LENGTH));
        itemStack.setHoverName(new StringTextComponent(musicTitle));
        CompoundNBT compound = itemStack.getTag();
        ValidDuration validDuration = validateMML(musicText);
        if (compound != null && (itemStack.getItem() instanceof IMusic) && validDuration.isValidMML() && validDuration.getDuration() > 0)
        {
            String musicTextKey = ModDataStore.addMusicText(musicText);
            if (musicTextKey != null)
            {
                CompoundNBT contents = new CompoundNBT();
                contents.putString(KEY_MUSIC_TEXT_KEY, musicTextKey);
                contents.putByteArray(KEY_EXTRA_DATA, extraData);
                contents.putInt(KEY_DURATION, validDuration.getDuration());
                contents.putInt(KEY_PARTS_COUNT, partInstrumentIds.length);
                contents.putUUID(KEY_OWNER_UUID, ownerUUID);
                contents.putString(KEY_OWNER_NAME, ownerName);
                int[] index = { 0 };
                if (partInstrumentIds.length > 0 && partInstrumentIds.length <= Reference.MAX_MML_PARTS) {
                    Arrays.stream(partInstrumentIds).sequential()
                            .forEach(string -> contents.putString(String.format("%s%d", KEY_PART_ID, index[0]++), string));
                    compound.put(KEY_SHEET_MUSIC, contents);

                    LOGGER.debug("Wrote {} title: \"{}\", extra: {}", musicType, musicTitle, extraData);
                    return true;
                } else
                    LOGGER.warn("Number of parts out of range 1-{}. Found {}", Reference.MAX_MML_PARTS, partInstrumentIds.length);
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
            boolean canReap = (getSheetMusicDaysLeft(pStack) == 0) && MXTuneConfig.sheetMusicExpires();
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
                        Thread.currentThread().interrupt();
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
                        Thread.currentThread().interrupt();
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
            ItemStack sheetMusic = getIMusicFromIInstrument(pInstrumentStack);
            String key = getMusicTextKey(sheetMusic);
            boolean canReap = (getSheetMusicDaysLeft(sheetMusic) == 0) && MXTuneConfig.sheetMusicExpires();

            if (key != null && canReap) {
                if (slot != null) {
                    multiplier += slot.index;
                    slot.setChanged();
                }
                removeSheetMusicFromIInstrument(pInstrumentStack);
                ModDataStore.removeSheetMusic(key);
                Misc.audiblePingPlayer((PlayerEntity)pEntity, ModSoundEvents.CRUMPLE_PAPER.get());

                int finalMultiplier = multiplier;
                SidedThreadGroups.SERVER.newThread(()->{
                    try
                    {
                        sleep(RandomUtils.nextLong(400 + (50L * finalMultiplier), 600 + (50L * finalMultiplier)));
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
                        sleep(RandomUtils.nextLong(600 + (50L * finalMultiplier), 800 + (50L * finalMultiplier)));
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
     */
    private static void removeSheetMusicFromIInstrument(ItemStack pStack)
    {
        ItemStack sheetMusic = SheetMusicHelper.getIMusicFromIInstrument(pStack);
        if (!sheetMusic.isEmpty())
        {
            MultiInstInventory inv = new MultiInstInventory(pStack);
            inv.removeItem(0, 1);
            inv.setChanged();
        }
    }

    /**
     * Client and Server side
     * <p></p>Validate the supplied MML and return its length in seconds.
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
}
