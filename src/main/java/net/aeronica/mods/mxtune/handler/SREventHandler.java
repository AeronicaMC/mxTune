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

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.groups.PlayManager;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class SREventHandler
{
    private SREventHandler() {}
    private static class SREventHandlerHolder {private static final SREventHandler INSTANCE = new SREventHandler();}
    public static SREventHandler getInstance() {return SREventHandlerHolder.INSTANCE;}
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
    {
        if (eventArgs.getModID().equals(MXTuneMain.MODID)) ModConfig.syncConfig();
    }
    
    /* 
     * Stops a playing instrument if it's placed into a container and the container is closed.
     */
    @SubscribeEvent
    public void onEvent(PlayerContainerEvent.Close event)
    {        
//        if (GROUPS.getClientPlayStatuses().containsValue(event.getEntityPlayer().getEntityId()))
//            System.out.println("[mxTune] PlayerContainerEvent.Close inventory name: " + );
        for(Slot slot: event.getContainer().inventorySlots)
        {
            if(slot != null && slot.inventory != null && slot.inventory.getName() != null &&
                    !(slot.inventory.getName().contentEquals("container.inventory") ||
                    slot.inventory.getName().contentEquals("container.crafting") ||
                    slot.inventory.getName().contentEquals("Result") ||
                    slot.inventory.getName().contentEquals("container.mxtune.instrument")))
            {                
                ItemStack stack = slot.getStack();
                if (slot.getHasStack() && stack.getItem() instanceof IInstrument)
                {
                    if (stack.getRepairCost() > -1)
                    {
//                        System.out.println("[mxTune] PlayerContainerEvent.Close inventory name: " + slot.inventory.getName());
                        stack.getItem().onUpdate(stack, event.getEntityPlayer().getEntityWorld(), event.getEntityPlayer(), 0, false );
                    }
                }
            }
        }
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