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
package net.aeronica.mods.mxtune.handler;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.SendKeyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyHandler
{
    private static class KeyHandlerHolder {private static final KeyHandler INSTANCE = new KeyHandler();}
    public static KeyHandler getInstance() {return KeyHandlerHolder.INSTANCE;}

    private KeyBinding keyOpenPartyGUI = new KeyBinding("mxtune.key.openParty", GLFW.GLFW_KEY_J, Reference.MOD_ID);
    private KeyBinding keyOpenMusicOptionsGUI = new KeyBinding("mxtune.key.openMusicOptions", GLFW.GLFW_KEY_P, Reference.MOD_ID);
    private boolean ctrlKeyDown = false;

    private KeyHandler()
    {
        ClientRegistry.registerKeyBinding(keyOpenPartyGUI);
        ClientRegistry.registerKeyBinding(keyOpenMusicOptionsGUI);
        Minecraft.getInstance().gameSettings.loadOptions();
    }

    @SubscribeEvent
    public void tick(InputEvent.KeyInputEvent event)
    {
        int key = event.getKey();
        boolean isDown = event.getAction() == GLFW.GLFW_PRESS;
        if (keyOpenPartyGUI.isPressed())
        {
            PacketDispatcher.sendToServer(new SendKeyMessage(keyOpenPartyGUI.getKeyDescription()));
        }
        if (keyOpenMusicOptionsGUI.isPressed())
        {
            PacketDispatcher.sendToServer(new SendKeyMessage(keyOpenMusicOptionsGUI.getKeyDescription()));
        }
        event.getKey();
        if (isDown && (key == GLFW.GLFW_KEY_LEFT_CONTROL) && !ctrlKeyDown)
        {
            PacketDispatcher.sendToServer(new SendKeyMessage("ctrl-down"));
            ctrlKeyDown = !ctrlKeyDown;
        }
        if (!isDown && (key == GLFW.GLFW_KEY_LEFT_CONTROL) && ctrlKeyDown)
        {
            PacketDispatcher.sendToServer(new SendKeyMessage("ctrl-up"));
            ctrlKeyDown = !ctrlKeyDown;
        }
    }
}
