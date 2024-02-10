package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModMigrationMapping
{
    private ModMigrationMapping() { /* NOOP */ }

    @SubscribeEvent
    public static void missingItemMapping(RegistryEvent.MissingMappings<Item> event)
    {
        for (RegistryEvent.MissingMappings.Mapping<Item> missing : event.getMappings(Reference.MOD_ID)) {
            // You like Accordions right :D
            SoundFontProxyManager.getProxyMapByIndex().values().stream()
                    .filter(proxy -> missing.key.getNamespace().equals(Reference.MOD_ID) && missing.key.getPath().equals(proxy.id))
                    .forEach(proxy -> missing.remap(ModItems.MULTI_INST.get()));
        }
    }
}
