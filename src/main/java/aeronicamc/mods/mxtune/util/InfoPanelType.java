package aeronicamc.mods.mxtune.util;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;

public class InfoPanelType extends net.minecraftforge.registries.ForgeRegistryEntry<InfoPanelType>
{
    public static final RegistryObject<InfoPanelType> B1X1 = register("b1x1", 16, 16);
    public static final RegistryObject<InfoPanelType> B2X8 = register("b2x8", 32, 128);
    public static final RegistryObject<InfoPanelType> B3X5 = register("b3x5", 48, 80);
    public static final RegistryObject<InfoPanelType> B4X4 = register("b4x4", 64, 64);
    public static final RegistryObject<InfoPanelType> B5X3 = register("b5x3", 80, 48);
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
