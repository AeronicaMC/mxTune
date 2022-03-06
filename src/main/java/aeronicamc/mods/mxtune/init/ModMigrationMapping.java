package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static aeronicamc.mods.mxtune.util.SoundFontProxyManager.soundFontProxyMapByIndex;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModMigrationMapping
{
    @SubscribeEvent
    public static void missingItemMapping(RegistryEvent.MissingMappings<Item> event)
    {
        for (RegistryEvent.MissingMappings.Mapping<Item> missing : event.getMappings(Reference.MOD_ID)) {
            // You like Accordions right :D
            soundFontProxyMapByIndex.values().stream()
                    .filter(proxy -> missing.key.getNamespace().equals(Reference.MOD_ID) && missing.key.getPath().equals(proxy.id))
                    .forEach(proxy -> missing.remap(ModItems.MULTI_INST.get()));
        }
    }
}
