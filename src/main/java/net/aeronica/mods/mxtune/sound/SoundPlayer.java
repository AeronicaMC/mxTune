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
 * Updates for MC 1.9+, removed LeftClickLoop and RightClickLoop Loop
 * references from class and methods. 
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
package net.aeronica.mods.mxtune.sound;

import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystem;

@SideOnly(Side.CLIENT)
public class SoundPlayer
{
    private SoundSystem soundSystem;
    private ArrayList<String> playing = new ArrayList<String>();

    private SoundPlayer() {}

    private static class SoundPlayerHolder {private static final SoundPlayer INSTANCE = new SoundPlayer();}

    public static SoundPlayer getInstance() {return SoundPlayerHolder.INSTANCE;}

    private void init()
    {
        if (soundSystem == null || soundSystem.randomNumberGenerator == null)
        {
            SoundManager soundManager = ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.audio.SoundHandler.class, Minecraft.getMinecraft().getSoundHandler(), "sndManager",
                    "field_147694_f");
            soundSystem = ObfuscationReflectionHelper.getPrivateValue(SoundManager.class, soundManager, "sndSystem", "field_148620_e");
        }
    }

    public String playNewSound(URL url, String id, BlockPos pos, boolean fading, float volume)
    {
        init();

        String identifier;
        String modType = "";
        String path = "";
        if (id == null)
            identifier = UUID.randomUUID().toString();
        else
            identifier = id;

        // Don't figure this stuff out here, do it at initialization time.
        modType = url.getPath().substring(url.getPath().lastIndexOf('.'));
        path = url.getPath();

        // testing existence of the file. Do this as part of initialization not
        // here.
        // File f = new File(path);
        // if(f.exists() && !f.isDirectory()) {
        // if(f.exists()) {
        ModLogger.logInfo("Path = " + path);
        // ModLogger.logInfo("Filename = " + modType);
        // } else {
        // ModLogger.logInfo("Path = " + path + " NOT FOUND!");
        // return identifier;
        // }

        double soundLength = 6; // SoundHelper.getSoundLength(sound);
        volume *= Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.PLAYERS);

        if (soundLength > 5)
            soundSystem.newStreamingSource(false, identifier, url, modType, false, pos.getX(), pos.getY(), pos.getZ(), fading ? 2 : 0, 16);
        else
            soundSystem.newSource(false, identifier, url, modType, false, pos.getX(), pos.getY(), pos.getZ(), fading ? 2 : 0, 16);

        soundSystem.setVolume(identifier, volume);
        soundSystem.play(identifier);

        playing.add(identifier);

        return identifier;
    }

    public void playSound(String identifier, float x, float y, float z)
    {
        if (playing.contains(identifier) && soundSystem.playing(identifier))
        {
            soundSystem.setPosition(identifier, x, y, z);
            soundSystem.play(identifier);
        }
    }

    public void stopSound(String identifier)
    {
        soundSystem.stop(identifier);
        removeSound(identifier);
    }

    private void removeSound(String identifier)
    {
        playing.remove(identifier);
        soundSystem.removeSource(identifier);
    }

    public void cleanUp()
    {
        ArrayList<String> temp = new ArrayList<String>(playing);
        for (String s : playing)
        {
            if (!soundSystem.playing(s))
            {
                soundSystem.removeSource(s);
                temp.remove(s);
            }
        }
        playing = temp;
    }

    public void adjustVolume(String identifier, float volume)
    {
        init();
        if (soundSystem.playing(identifier))
        {
            soundSystem.setVolume(identifier, volume);
        }
    }

    public void stopSounds()
    {
        if (soundSystem != null)
        {
            for (String s : playing)
            {
                soundSystem.stop(s);
            }
        }
        playing = new ArrayList<String>();
    }

    public void pauseSounds()
    {
        if (soundSystem != null)
        {
            for (String s : playing)
            {
                soundSystem.pause(s);
            }
        }
    }

    public void resumeSounds()
    {
        if (soundSystem != null)
        {
            for (String s : playing)
            {
                soundSystem.play(s);
            }
        }
    }

    public boolean isPlaying(String identifier)
    {
        init();
        return soundSystem.playing(identifier) ? true : false;
    }
}
