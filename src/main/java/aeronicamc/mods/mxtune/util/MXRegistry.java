package aeronicamc.mods.mxtune.util;

import aeronicamc.mods.mxtune.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Supplier;

public class MXRegistry
{
    private static final Logger LOGGER = LogManager.getLogger(MXRegistry.class);
    public static final DeferredRegister<InfoPanelType> INFO_PANELS = DeferredRegister.create(InfoPanelType.class, Reference.MOD_ID);

    public static Supplier<IForgeRegistry<InfoPanelType>> INFO_PANEL_REGISTRY = INFO_PANELS
            .makeRegistry("info_panel_types", () ->
                                  new RegistryBuilder<InfoPanelType>()
                                          .setDefaultKey(new ResourceLocation(Reference.MOD_ID, "b1x1"))
                                          .setMaxID(Integer.MAX_VALUE - 1)
                                          .onAdd(((owner, stage, id, obj, oldObj) ->
                                                         LOGGER.debug("InfoPanelType: {} Sync: {}" ,getName(obj), stage.getName())))
                         );

    public static InfoPanelType getInfoPanelType(String name) {
        return Objects.requireNonNull(INFO_PANEL_REGISTRY.get().getValue(new ResourceLocation(name)));
    }

    public static InfoPanelType getInfoPanelType(ResourceLocation location) {
        return Objects.requireNonNull(INFO_PANEL_REGISTRY.get().getValue(location));
    }

    public static <T extends IForgeRegistryEntry<?>> ResourceLocation getName(T type) {
        return Objects.requireNonNull(type.getRegistryName());
    }

    public static <T extends IForgeRegistryEntry<?>> ResourceLocation getName(Supplier<T> supplier) {
        return getName(supplier.get());
    }
}
