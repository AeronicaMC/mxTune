package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.util.AntiNull;
import net.minecraft.client.Minecraft;

public class Handler
{
    private static final Minecraft mc = Minecraft.getInstance();

    private Handler()
    {
        // NOP
    }

    public static void openTestScreen()
    {
        mc.tell(()->mc.setScreen(new TestScreen()));
    }
    public static void openGuiMultiInstChooser()
    {
        mc.tell(()->mc.setScreen(new GuiMultiInstChooser(AntiNull.nonNullInjected())));
    }
}
