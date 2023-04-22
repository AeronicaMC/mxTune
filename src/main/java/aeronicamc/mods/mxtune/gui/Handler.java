package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.gui.group.GuiGroup;
import aeronicamc.mods.mxtune.gui.group.GuiPin;
import aeronicamc.mods.mxtune.gui.mml.GuiMXT;
import aeronicamc.mods.mxtune.util.Misc;
import net.minecraft.client.Minecraft;

public class Handler
{
    static final Minecraft mc = Minecraft.getInstance();

    private Handler() { /* NOP */ }

    public static void OpenGuiPinScreen(int groupId)
    {
        mc.tell(() -> mc.setScreen(new GuiPin(groupId)));
    }

    public static void OpenGuiGroupScreen()
    {
        mc.tell(() -> mc.setScreen(new GuiGroup()));
    }

    public static void OpenSheetMusicScreen()
    {
        mc.tell(() -> mc.setScreen(new GuiMXT(Misc.nonNullInjected())));
    }

    public static void openTestScreen()
    {
        mc.tell(() -> mc.setScreen(new TestScreen()));
    }
}
