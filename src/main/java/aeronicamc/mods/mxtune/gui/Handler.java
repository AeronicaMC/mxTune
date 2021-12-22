package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.gui.mml.GuiMXT;
import aeronicamc.mods.mxtune.util.AntiNull;
import net.minecraft.client.Minecraft;

public class Handler
{
    private static final Minecraft mc = Minecraft.getInstance();

    private Handler()
    {
        // NOP
    }

    public static void OpenSheetMusicScreen()
    {
        mc.tell(()->mc.setScreen(new GuiMXT(AntiNull.nonNullInjected(), GuiMXT.Mode.SHEET_MUSIC)));
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
