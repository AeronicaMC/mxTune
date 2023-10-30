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

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.ClientStateDataMessage;
import net.aeronica.mods.mxtune.network.client.SendCSDChatMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;
import java.util.Map;

/**
 * ServerStateManager<p>
 * Tracks the mxTune status on clients. Items tracked are the availability of the MIDI system,
 * the on/off state of MASTER and MXTUNE volume settings. The data is used to adjust the behavior
 * of the play system to minimize failures and unexpected behaviors.</p>
 * 
 * @author Paul Boese aka Aeronica
 *
 */
public class ServerCSDManager
{
    private static final Map<Integer, ClientStateData> clientState = new HashMap<>();

    private ServerCSDManager() { /* NOP */ }
    
    /**
     * <p>Asks the client for a copy of the current ClientStateData.
     * Typically done when the client logs into the server.</p>
     * @param playerIn to be queried
     */
    public static void queryClient(EntityPlayer playerIn)
    {
        ClientStateDataMessage message = new ClientStateDataMessage();
        PacketDispatcher.sendTo(message, (EntityPlayerMP) playerIn);
    }
    
    /**
     * Adds/Updates the ClientStateData received from the client.
     * @param playerIn the client
     * @param csd clint state data from client
     */
    public static void updateState(EntityPlayer playerIn, ClientStateData csd)
    {
        Integer playerID = playerIn.getEntityId();
        if(clientState.containsKey(playerID))
            clientState.replace(playerID, csd);
        else
            clientState.put(playerID, csd);      
    }

    /**
     * <p>Returns the overall availability of the MIDI system,
     * the on/off state of MASTER and MXTUNE volume settings.
     * A failure of a single test fails all.</p>
     * 
     * @param playerID player to check
     * @return A failure of a single test fails all.
     */
    private static boolean canMXTunesPlay(Integer playerID)
    {
        return (clientState.containsKey(playerID)) && clientState.get(playerID).isGood();
    }
    
    /**
     * <p>Returns the overall availability of the MIDI system,
     * the on/off state of MASTER and MXTUNE volume settings.
     * A failure of a single test fails all.</p>
     * 
     * @param playerIn can this client play?
     * @return A failure of a single test fails all.
     */
    public static boolean canMXTunesPlay(EntityPlayer playerIn)
    {
        return canMXTunesPlay(playerIn.getEntityId());
    }

    /**
     * <p>Sends the server side version of ClientStateData to the client to be displayed in chat.</p>
     * @param playerIn to be informed
     */
    public static void sendErrorViaChat(EntityPlayer playerIn)
    {
        Integer playerID = playerIn.getEntityId();
        if (clientState.containsKey(playerID))
        {
            SendCSDChatMessage message =  new SendCSDChatMessage(clientState.get(playerID));
            PacketDispatcher.sendTo(message, (EntityPlayerMP) playerIn);
        }
    }
}


