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
package net.aeronica.mods.mxtune.handler;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.blocks.IMusicPlayer;
import net.aeronica.mods.mxtune.config.MXTuneConfig;
import net.aeronica.mods.mxtune.managers.PlayManager;
import net.aeronica.mods.mxtune.world.IModLockableContainer;
import net.aeronica.mods.mxtune.world.LockableHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class CommonEventHandler
{
    private CommonEventHandler() { /* NOP */ }
    private static int count = 0;

    // Stops a playing player if they open any non-player inventory.
    @SubscribeEvent
    public static void onEvent(PlayerContainerEvent.Open event)
    {
        if(!event.getEntityPlayer().getEntityWorld().isRemote)
            PlayManager.stopPlayingPlayer(event.getEntityLiving());
    }
    

    // Test stop distance between players in a JAM session.
    @SubscribeEvent
    public static void onEvent(ServerTickEvent event)
    {
        /* Fired once every two seconds */
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END && (count++ % 40 == 0)) {
            PlayManager.testStopDistance(MXTuneConfig.getGroupPlayAbortDistance());
        }
    }

    // Test if a player can break this block with a TE of type IModLockableContainer.
    // Only the owner of the block can break these.
    @SubscribeEvent
    public static void onEvent(BlockEvent.BreakEvent event)
    {
        if(event.getWorld().isRemote()) return;
        if(event.getState().getBlock() instanceof IMusicPlayer)
        {
            PlayerEntity player = event.getPlayer();
            TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
            World world = (World) event.getWorld();
            if(player != null && world != null && (tileEntity instanceof IModLockableContainer) &&
                    LockableHelper.isBreakable(player, world, event.getPos()) && !player.isCreative())
                event.setCanceled(true);
        }
    }
}
