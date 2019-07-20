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
package net.aeronica.mods.mxtune.proxy;

import com.google.common.util.concurrent.ListenableFuture;
import net.aeronica.mods.mxtune.init.ModEntities;
import net.aeronica.mods.mxtune.managers.GroupManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerProxy
{
    public void preInit() { /*  NOP */ }

    public void init() { /*  NOP */ }
    
    public void postInit() { /*  NOP */ }

    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule)
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnableToSchedule);
    }

    public void initEntities() { ModEntities.init(); }

    public Side getPhysicalSide()  { return Side.SERVER ;}

    public Side getEffectiveSide() { return getPhysicalSide(); }

    public PlayerEntity getClientPlayer() { return null; }

    public PlayerEntity getPlayerByEntityID(int entityID)
    {
        return (PlayerEntity) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getEntityByID(entityID);
    }

    public Minecraft getMinecraft() { return null; }

    public World getClientWorld() { return null; }

    public void spawnMusicParticles(PlayerEntity player) { /* NOP */ }
    
    public void registerEventHandlers()
    {
        MinecraftForge.EVENT_BUS.register(GroupManager.INSTANCE);
    }

    public void registerKeyBindings() { /* NOP */ }

    public void initMML() { /*  NOP */ }

    public void registerHUD() { /*  NOP */ }

    public boolean playerIsInCreativeMode(PlayerEntity player)
    {
        if (player instanceof ServerPlayerEntity)
        {
            ServerPlayerEntity entityPlayerMP = (ServerPlayerEntity) player;
            return entityPlayerMP.isCreative();
        }
        return false;
    }

    public PlayerEntity getPlayerEntity(MessageContext ctx) { return ctx.getServerHandler().player; }

    public IThreadListener getThreadFromContext(MessageContext ctx) { return ctx.getServerHandler().player.getServer(); }

    public void clientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        // Override and use in ClientProxy Only
    }

    public void clientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        // Override and use in ClientProxy Only
    }
}
