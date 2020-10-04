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

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.blocks.IMusicPlayer;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.entity.living.EntityGoldenSkeleton;
import net.aeronica.mods.mxtune.entity.living.EntityTimpani;
import net.aeronica.mods.mxtune.items.ItemSheetMusic;
import net.aeronica.mods.mxtune.managers.PlayManager;
import net.aeronica.mods.mxtune.util.SheetMusicSongs;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.aeronica.mods.mxtune.world.IModLockableContainer;
import net.aeronica.mods.mxtune.world.LockableHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Random;

@Mod.EventBusSubscriber()
public class CommonEventHandler
{
    private CommonEventHandler() { /* NOP */ }
    private static int count = 0;
    private static Random RANDOM = new Random();

    // Stops a playing player if they open any non-player inventory.
    @SubscribeEvent
    public static void onEvent(PlayerContainerEvent.Open event)
    {
        if(MXTune.proxy.getEffectiveSide() == Side.SERVER)
            PlayManager.stopPlayingPlayer(event.getEntityLiving());
    }
    

    // Test stop distance between players in a JAM session.
    @SubscribeEvent
    public static void onEvent(ServerTickEvent event)
    {
        /* Fired once every two seconds */
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.END && (count++ % 40 == 0)) {
            PlayManager.testStopDistance(ModConfig.getGroupPlayAbortDistance());
        }
    }

    // Test if a player can break this block with a TE of type IModLockableContainer.
    // Only the owner of the block can break these.
    @SubscribeEvent
    public static void onEvent(BlockEvent.BreakEvent event)
    {
        if(event.getWorld().isRemote) return;
        if(event.getState().getBlock() instanceof IMusicPlayer)
        {
            TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
            if(tileEntity instanceof IModLockableContainer)
            {
                boolean isCreativeMode = event.getPlayer() != null && event.getPlayer().capabilities.isCreativeMode;
                if (LockableHelper.isBreakable(event.getPlayer(), tileEntity.getWorld(), event.getPos()) && !isCreativeMode)
                    event.setCanceled(true);
            }
        }
    }

    // Add Title/MML to Sheet Music Drops for Mod Mobs
    @SubscribeEvent
    public static void onEvent(LivingDropsEvent event)
    {
        if (event.getEntityLiving() instanceof EntityGoldenSkeleton || event.getEntityLiving() instanceof EntityTimpani)
        {
            event.getDrops().forEach(drop ->
                {
                if (drop.getItem().getItem() instanceof ItemSheetMusic)
                    {
                        Random rand = event.getEntityLiving().getEntityWorld().rand;
                        int selection = rand.nextInt(SheetMusicSongs.values().length+1);
                        drop.setItem(SheetMusicUtil.createSheetMusic(
                                SheetMusicSongs.getMML(selection).getTitle(),
                                SheetMusicSongs.getMML(selection).getMML()));
                    }
                });
        }
    }
}
