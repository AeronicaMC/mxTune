package aeronicamc.mods.mxtune.gui;

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
        mc.tell(()->mc.setScreen(new TestScreen(null)));
    }
    public static void openGuiMultiInstChooser()
    {
        mc.tell(()->mc.setScreen(new GuiMultiInstChooser(null)));
    }
}
