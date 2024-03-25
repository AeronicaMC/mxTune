package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.gui.group.GuiGroup;
import aeronicamc.mods.mxtune.gui.group.GuiPin;
import aeronicamc.mods.mxtune.gui.mml.GuiMXT;
import aeronicamc.mods.mxtune.util.Misc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import javax.annotation.Nullable;

public class Handler
{
    static final Minecraft mc = Minecraft.getInstance();

    private Handler() { /* NOP */ }

    public static void openGuiPinScreen(int groupId)
    {
        mc.tell(() -> mc.setScreen(new GuiPin(groupId)));
    }

    public static void openGuiGroupScreen()
    {
        mc.tell(() -> mc.setScreen(new GuiGroup()));
    }

    public static void openSheetMusicScreen()
    {
        mc.tell(() -> mc.setScreen(new GuiMXT(Misc.nonNullInjected())));
    }

    public static void openTestScreen()
    {
        mc.tell(() -> mc.setScreen(new TestScreen()));
    }

    public static void openOverlayManagerScreen(@Nullable Screen parent) { mc.tell(() -> mc.setScreen(new OverlayManagerScreen(parent))); }
}
