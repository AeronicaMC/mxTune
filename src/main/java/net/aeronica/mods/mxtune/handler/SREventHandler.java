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

import net.aeronica.mods.mxtune.groups.GroupManager;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.SyncPlayerPropsMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SREventHandler
{

    private SREventHandler() {}

    private static class SREventHandlerHolder {private static final SREventHandler INSTANCE = new SREventHandler();}

    public static SREventHandler getInstance() {return SREventHandlerHolder.INSTANCE;}

    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event)
    {
        /*
         * Only need to synchronize when the world is remote (i.e. we're on the
         * server side) and only for player entities, as that's what we need for
         * the GuiJamOverlay If you have any non-DataWatcher fields in your
         * extended properties that need to be synced to the client, you must
         * send a packet each time the player joins the world; this takes care
         * of dying, changing dimensions, etc.
         */
        if (event.getEntity() instanceof EntityPlayerMP)
        {
            ModLogger.logInfo("Player joined world, sending extended properties to client");
            PacketDispatcher.sendTo(new SyncPlayerPropsMessage((EntityPlayer) event.getEntity()), (EntityPlayerMP) event.getEntity());
            GroupManager.sync();
        }
    }
}