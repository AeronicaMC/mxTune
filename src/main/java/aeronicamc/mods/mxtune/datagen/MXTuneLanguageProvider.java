package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public class MXTuneLanguageProvider extends LanguageProvider
{
    public MXTuneLanguageProvider(DataGenerator gen)
    {
        super(gen, Reference.MOD_ID, "eng_us");
    }

    @Override
    protected void addTranslations()
    {
        // TODO
        addBlocks();
        addItems();
    }

    @Override
    public String getName()
    {
        return  Reference.MOD_NAME + " " +super.getName();
    }

    private void addBlocks()
    {
        // TODO
    }

    private void addItems()
    {
        // TODO
        add(ModItems.SHEET_MUSIC.get(), "Sheet Music");
        add(ModItems.MUSIC_PAPER.get(), "Music Paper");
        addTooltip("sheet_music.days_left", "Days left: %s");
        addTooltip("sheet_music.days_left_error", "Days left: nn");
        addTooltip("mxtune.sheet_music.duration_error", "h:mm:ss");
        addTooltip("sheet_music.empty", " - Empty - ");
        addInstrumentItems(this);
        addTooltip("instrument_item.shift_help_01", "Hold SHIFT for HELP");
        addTooltip("instrument_item.shift_help_02", "SHIFT+Right Click to OPEN");
        addTooltip("instrument_item.shift_help_03", "Right Click to PLAY");
    }

    private void addInstrumentItems(LanguageProvider provider)
    {
        ModItems.INSTRUMENT_ITEMS.forEach(
            (key, value) -> provider.add(value.get(),
                convertSnakeCaseToTitleCase(SoundFontProxyManager.getName(key))));
    }

    private String convertSnakeCaseToTitleCase(String input)
    {
        return WordUtils.capitalizeFully(StringUtils.replace(input, "_", " "));
    }

    /**
     * prepends tooltip.[mod_id].
     * @param identifier the unique help id
     * @param helpText help text
     */
    private void addTooltip(String identifier, String helpText)
    {
        add(String.format("tooltip.%s.%s", Reference.MOD_ID, identifier), helpText);
    }
}
