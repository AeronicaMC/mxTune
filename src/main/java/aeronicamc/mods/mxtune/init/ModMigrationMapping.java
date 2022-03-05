package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModMigrationMapping
{
    @SubscribeEvent
    public static void missingItemMapping(RegistryEvent.MissingMappings<Item> event)
    {
//        for (RegistryEvent.MissingMappings.Mapping<Item> missing : event.getMappings()) {
//            // TODO: use SoundFontProxyManager keys
//            if (missing.key.getNamespace().equals(Reference.MOD_ID) && missing.key.getPath().equals("instrument")) {
//                missing.remap(ModItems.MULTI_INST);
//            }
//        }
    }
}
