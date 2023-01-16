package aeronicamc.mods.mxtune.util;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;

public class InfoPanelType extends net.minecraftforge.registries.ForgeRegistryEntry<InfoPanelType>
{
    public static final RegistryObject<InfoPanelType> B1X1 = register("b1x1", 16, 16);
    public static final RegistryObject<InfoPanelType> B2X1 = register("b2x1", 32, 16);
    public static final RegistryObject<InfoPanelType> B2X2 = register("b2x2", 32, 32);
    public static final RegistryObject<InfoPanelType> B3X3 = register("b3x3", 48, 48);
    public static final RegistryObject<InfoPanelType> B4X1 = register("b4x1", 64, 16);
    public static final RegistryObject<InfoPanelType> B4X2 = register("b4x2", 64, 32);
    public static final RegistryObject<InfoPanelType> B4X4 = register("b4x4", 64, 64);
    public static final RegistryObject<InfoPanelType> B5X3 = register("b5x3", 80, 48);
    public static final RegistryObject<InfoPanelType> B5X5 = register("b5x5", 80, 80);
    public static final RegistryObject<InfoPanelType> B8X1 = register("b8x1", 128, 16);
    public static final RegistryObject<InfoPanelType> B8X2 = register("b8x2", 128, 32);
    private final int width;
    private final int height;

    public static RegistryObject<InfoPanelType> register(String pKey, int pWidth, int pHeight)
    {
        return MXRegistry.INFO_PANELS.register(pKey, ()-> new InfoPanelType(pWidth, pHeight));
    }

    public InfoPanelType(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public static void initialize(IEventBus modBus) {
        MXRegistry.INFO_PANELS.register(modBus);
    }
}
