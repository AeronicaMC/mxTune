/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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
 */
package net.aeronica.mods.mxtune.status;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CSDChatStatus
{
    
    ClientStateData csd;
    EntityPlayer playerIn;
    public CSDChatStatus(EntityPlayer playerIn, ClientStateData csd)
    {
        this.csd = new ClientStateData(csd.isMidiAvailable(), csd.isMasterVolumeOn(), csd.isMxtuneVolumeOn());
        this.playerIn = playerIn;
        process();
    }
    
    private void process()
    {
        if(csd.isMidiAvailable()==false)
            playerIn.sendMessage(new TextComponentString("[" + MXTuneMain.MODNAME + "] " + TextFormatting.RED +I18n.format("mxtune.chat.msu.midiNotAvailable")));
        if(csd.isMasterVolumeOn()==false)
            playerIn.sendMessage(new TextComponentString("[" + MXTuneMain.MODNAME + "] " + TextFormatting.YELLOW +I18n.format("mxtune.chat.musicAndSound.masterVolumeOff")));
        if(csd.isMxtuneVolumeOn()==false)
            playerIn.sendMessage(new TextComponentString("[" + MXTuneMain.MODNAME + "] " + TextFormatting.YELLOW +I18n.format("mxtune.chat.musicAndSound.mxTuneVolumeOff")));            
    }
    
}