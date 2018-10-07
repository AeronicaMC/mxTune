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
 */
package net.aeronica.mods.mxtune.handler;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.groups.PlayManager;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class CommonEventHandler
{
    public static final CommonEventHandler INSTANCE = new CommonEventHandler();
    private CommonEventHandler() {}
    
    /* 
     * Stops a playing player if they open any non-player inventory.
     */
    @SubscribeEvent
    public void onEvent(PlayerContainerEvent.Open event)
    {
        if(MXTune.proxy.getEffectiveSide() == Side.SERVER)
            PlayManager.stopPlayingPlayer(event.getEntityLiving());
    }
    
    private static int count = 0;
    @SubscribeEvent
    public void onEvent(ServerTickEvent event)
    {
        /* Fired once every two seconds */
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.END && (count++ % 40 == 0)) {
            PlayManager.testStopDistance(ModConfig.getGroupPlayAbortDistance());
        }
    }
}
