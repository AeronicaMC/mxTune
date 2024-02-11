/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package aeronicamc.mods.mxtune.util;

import aeronicamc.mods.mxtune.MXTune;
import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.OpenScreenMessage;
import aeronicamc.mods.mxtune.network.messages.SendKeyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public enum KeyHandler
{
    INSTANCE;
    private static final Minecraft mc = Minecraft.getInstance();
    private final KeyBinding keyOpenGroupGUI = new KeyBinding("key.mxtune.open_group", GLFW.GLFW_KEY_J, Reference.MOD_ID);
    private final KeyBinding keyOpenMusicOptionsGUI = new KeyBinding("key.mxtune.open_music_options", GLFW.GLFW_KEY_P, Reference.MOD_ID);
    private boolean ctrlKeyDown = false;

    public static KeyHandler getInstance() { return INSTANCE; }

    KeyHandler()
    {
        ClientRegistry.registerKeyBinding(keyOpenGroupGUI);
        if (MXTune.isDevEnv()) // FUTURE: Music Options
            ClientRegistry.registerKeyBinding(keyOpenMusicOptionsGUI);
        Minecraft.getInstance().options.load();
    }

    @SubscribeEvent
    public void tick(InputEvent.KeyInputEvent event)
    {
        // If the player or world is null we must not try to send packets to the server!
        if (mc.player == null || mc.player.level == null) return;

        int key = event.getKey();
        boolean isDown = event.getAction() == (GLFW.GLFW_PRESS & GLFW.GLFW_REPEAT);
        if (keyOpenGroupGUI.consumeClick())
        {
            PacketDispatcher.sendToServer(new OpenScreenMessage(OpenScreenMessage.SM.GROUP_OPEN));
        }
        if (keyOpenMusicOptionsGUI.consumeClick())
        {
            PacketDispatcher.sendToServer(new SendKeyMessage(keyOpenMusicOptionsGUI.getName()));
        }

        if (isDown && (key == GLFW.GLFW_KEY_LEFT_CONTROL) && ctrlKeyDown)
        {
            PacketDispatcher.sendToServer(new SendKeyMessage("ctrl-up"));
            ctrlKeyDown = !ctrlKeyDown;
        }
        if (!isDown && (key == GLFW.GLFW_KEY_LEFT_CONTROL) && !ctrlKeyDown)
        {
            PacketDispatcher.sendToServer(new SendKeyMessage("ctrl-down"));
            ctrlKeyDown = !ctrlKeyDown;
        }
    }
}
