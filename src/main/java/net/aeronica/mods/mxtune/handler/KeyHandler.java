/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ********************************************************************
 * Changes for Aeronica's mxTune MOD:
 * Changed to accommodate my own key bindings and packets
 * ********************************************************************
 * 
 * Combustible Lemon Launcher
 * Copyright (C) 2014-2016  Phil Julian (aka iBuilder99)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Phil Julian (aka iBuilder99)
 */
package net.aeronica.mods.mxtune.handler;

import org.lwjgl.input.Keyboard;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.SendKeyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class KeyHandler
{
    private static class KeyHandlerHolder {private static final KeyHandler INSTANCE = new KeyHandler();}
    public static KeyHandler getInstance() {return KeyHandlerHolder.INSTANCE;}
    
    private KeyBinding key_openGUI = new KeyBinding("key.openParty", Keyboard.KEY_J, MXTuneMain.MODID);

    private KeyHandler()
    {
        ClientRegistry.registerKeyBinding(key_openGUI);
        Minecraft.getMinecraft().gameSettings.loadOptions();
    }

    @SubscribeEvent
    public void tick(KeyInputEvent event)
    {
        if (key_openGUI.isPressed())
        {
            PacketDispatcher.sendToServer(new SendKeyMessage(key_openGUI.getKeyDescription()));
        }
    }
}
