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

import net.aeronica.mods.mxtune.init.ModSounds;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.ClientStateDataMessage;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * ClientStateMonitor<p>
 * 
 * Pushes initial state and changes to state to the server. Items tracked are the 
 * availability of the MIDI system, the on/off state of MASTER and MXTUNE volume settings.</p>
 * 
 * @author Paul Boese aka Aeronica
 *
 */
@SideOnly(Side.CLIENT)
public class ClientCSDMonitor
{

    private static ClientStateData csd = null;
    private static Minecraft mc = Minecraft.getMinecraft();
    private static GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
    
    /*
     * Collect initial state just after logging on to a server then post it to the server.
     */
    public static void collectAndSend()
    {
        csd = snapShot();
        sendToServer();
        ModLogger.info("ClientStateMonitor#initialize: " + csd);
    }
    
    private static ClientStateData snapShot()
    {
        return new ClientStateData(
                MIDISystemUtil.midiUnavailable()==false,
                gameSettings.getSoundLevel(SoundCategory.MASTER)>0F,
                gameSettings.getSoundLevel(ModSounds.SC_MXTUNE)>0F);
    }
    
    /*
     * Monitor state changes and post them as they are detected.
     */
    
    private static void sendToServer()
    {
        ClientStateDataMessage message = new ClientStateDataMessage(csd);
        PacketDispatcher.sendToServer(message);
    }
    
    private static boolean inGui = false;
    public static void detectAndSend()
    {

        if (mc.currentScreen instanceof GuiScreenOptionsSounds && !inGui)
        {
            ModLogger.info("Opened GuiScreenOptionsSounds");
            inGui=true;
        }
        else if(!(mc.currentScreen instanceof GuiScreenOptionsSounds) && inGui)
        {
            ModLogger.info("Closed GuiScreenOptionsSounds");
            inGui=false;
            ClientStateData ss = snapShot();
            if(csd!=null && !csd.isEqual(ss)) 
            {
                csd = ss;
                sendToServer();
                ModLogger.info("ClientStateData ***Changed*** Sending to server");
            }
        }
    }
    
    public static boolean canMXTunesPlay()
    {
        return (csd !=null && csd.isGood());
    }
    
    /**
     * A Client side version to send the current status to the players chat.
     * @param playerIn
     */
    public static void sendErrorViaChat(EntityPlayer playerIn)
    {
        if (csd==null)
            snapShot();
            new CSDChatStatus(playerIn, csd);   
    }
}
