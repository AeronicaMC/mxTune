package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModSoundEvents;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public class MXTuneSoundDefinitionsProvider extends SoundDefinitionsProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final ExistingFileHelper helper;

    public MXTuneSoundDefinitionsProvider(DataGenerator generator, String modId, ExistingFileHelper helper)
    {
        super(generator, modId, helper);
        this.helper = helper;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return Reference.MOD_NAME + "SoundDefinitions";
    }

    /**
     * Registers the sound definitions that should be generated via one of the {@code add} methods.
     */
    @Override
    public void registerSounds()
    {
        add(ModSoundEvents.PCM_PROXY.get(), definition().with(
                sound(ModSoundEvents.PCM_PROXY.getId()).stream()).subtitle("subtitle.mxtune.pcm-proxy"));

        add(ModSoundEvents.FAILURE.get(), definition().with(
                sound(ModSoundEvents.FAILURE.getId())).subtitle("subtitle.mxtune.failure"));

        add(ModSoundEvents.CRUMPLE_PAPER.get(), definition().with(
                sound(ModSoundEvents.CRUMPLE_PAPER.getId()).pitch(1.2),
                sound(ModSoundEvents.CRUMPLE_PAPER.getId()),
                sound(ModSoundEvents.CRUMPLE_PAPER.getId()).pitch(0.8)).subtitle("subtitle.mxtune.crumple_paper"));
    }
}
