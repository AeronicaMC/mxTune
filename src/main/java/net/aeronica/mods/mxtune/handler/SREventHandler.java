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
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
     * TODO: read the inventory slots list instead so it can be determined if the instrument
     * is in player inventory or a containers inventory.
     */
    @SubscribeEvent
    public void onEvent(PlayerContainerEvent.Close event)
    {
        if (event.getEntityPlayer().worldObj.isRemote) return;
        for(ItemStack stack: event.getContainer().getInventory())
        {
            if (stack != null && stack.getItem() instanceof IInstrument)
            {
                ModLogger.logInfo("PCE: " + stack.getRepairCost());
                if (stack.getRepairCost() > 0)
                {
                    stack.getItem().onUpdate(stack, event.getEntityPlayer().getEntityWorld(), event.getEntityPlayer(), 0, false );
                }
            }
        }
        for(Slot slot: event.getContainer().inventorySlots)
        {
            ModLogger.logInfo("PCE slot: " +  slot.getSlotIndex() + ", has:" + slot.getHasStack() + ", " + slot.getStack());
        }
    }
}