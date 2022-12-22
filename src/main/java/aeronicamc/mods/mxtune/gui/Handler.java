package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.gui.mml.GuiMXT;
import aeronicamc.mods.mxtune.util.Misc;
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
        mc.tell(()->mc.setScreen(new GuiMXT(Misc.nonNullInjected())));
    }
    public static void openTestScreen()
    {
        mc.tell(()->mc.setScreen(new TestScreen()));
    }
}
