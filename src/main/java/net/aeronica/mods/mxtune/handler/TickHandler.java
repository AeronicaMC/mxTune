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
 * Updates for MC 1.9+, removed LeftClickLoop, RightClickLoop, and
 * interactionHandler references from class. Removed keyBind* logic. 
 * ********************************************************************
 * 
 * zoonie's Custom Interaction Sounds Mod
 * Copyright (C) 2015  zoonie 
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2365875-custom-interaction-sounds
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
 */
package net.aeronica.mods.mxtune.handler;

import net.aeronica.mods.mxtune.sound.SoundPlayer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class TickHandler
{
    private Minecraft minecraft = Minecraft.getMinecraft();
    private int tick = 0;
    private boolean paused = false;

    private TickHandler() {}
    private static class TickHandlerHolder {private static final TickHandler INSTANCE = new TickHandler();}
    public static TickHandler getInstance() {return TickHandlerHolder.INSTANCE;}

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event)
    {
        if (paused == false && minecraft.isGamePaused())
        {
            SoundPlayer.getInstance().pauseSounds();
            paused = true;
        } else if (paused == true && !minecraft.isGamePaused())
        {
            SoundPlayer.getInstance().resumeSounds();
            paused = false;
        }
        if (tick == 0)
        {
            SoundPlayer.getInstance().cleanUp();
        }
        tick = ++tick % 100;
    }
}
