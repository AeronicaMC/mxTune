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
        AddMultiInst(this);
    }

    private void AddMultiInst(LanguageProvider provider)
    {
        ModItems.MULTI_INST.forEach(
            (key, value) -> provider.add(value.get(),
                convertSnakeCaseToTitleCase(SoundFontProxyManager.getName(key))));
    }

    private String convertSnakeCaseToTitleCase(String input)
    {
        return WordUtils.capitalizeFully(StringUtils.replace(input, "_", " "));
    }
}
