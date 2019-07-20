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
package net.aeronica.mods.mxtune.status;

import net.aeronica.mods.mxtune.Reference;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CSDChatStatus
{
    private ClientStateData csd;
    private PlayerEntity playerIn;
    public CSDChatStatus(PlayerEntity playerIn, ClientStateData csd)
    {
        this.csd = csd;
        this.playerIn = playerIn;
        process();
    }
    
    private void process()
    {
        if(!csd.isMidiAvailable())
            playerIn.sendMessage(new StringTextComponent("[" + Reference.MOD_NAME + "] " + TextFormatting.RED +I18n.format("mxtune.chat.msu.midiNotAvailable")));
        if(!csd.isMasterVolumeOn())
            playerIn.sendMessage(new StringTextComponent("[" + Reference.MOD_NAME + "] " + TextFormatting.YELLOW +I18n.format("mxtune.chat.musicAndSound.masterVolumeOff")));
        if(!csd.isMxtuneVolumeOn())
            playerIn.sendMessage(new StringTextComponent("[" + Reference.MOD_NAME + "] " + TextFormatting.YELLOW +I18n.format("mxtune.chat.musicAndSound.recordVolumeOff")));
    }
}
