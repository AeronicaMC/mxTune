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

import net.aeronica.mods.mxtune.gui.hud.GuiJamOverlay;
import net.aeronica.mods.mxtune.gui.mml.GuiStaffOverlay;
import net.aeronica.mods.mxtune.handler.KeyHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.LogicalSide;

public class ClientProxy extends ServerProxy
{
    @Override
    public void initEntities() { super.initEntities(); }

    @Override
    public void addScheduledTask(Runnable runnableToSchedule)
    {
        Minecraft.getInstance().runImmediately(runnableToSchedule);
    }

    @Override
    public LogicalSide getPhysicalSide() { return LogicalSide.CLIENT; }

    // TODO: How to handle side stuff properly in 1.13+
    @Override
    public LogicalSide getEffectiveSide() { return getPhysicalSide(); } //FMLCommonHandler.instance().getEffectiveSide(); }

    @Override
    public Minecraft getMinecraft() { return Minecraft.getInstance() ;}

    @Override
    public PlayerEntity getClientPlayer() { return Minecraft.getInstance().player; }

    @Override
    public PlayerEntity getPlayerByEntityID(int entityID)
    {
        return (PlayerEntity)  getClientPlayer().getEntityWorld().getEntityByID(entityID);
    }

    @Override
    public ClientWorld getClientWorld() { return Minecraft.getInstance().world; }

    @Override
    public void spawnMusicParticles(PlayerEntity player) { /* Future */ }

    @Override
    public void registerEventHandlers() { super.registerEventHandlers(); }

    @Override
    public void registerKeyBindings()
    {
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());
    }

    @Override
    public void registerHUD()
    {
        MinecraftForge.EVENT_BUS.register(GuiJamOverlay.getInstance());
        MinecraftForge.EVENT_BUS.register(GuiStaffOverlay.getInstance());
    }

    @Override
    public boolean playerIsInCreativeMode(PlayerEntity player)
    {
        if (player instanceof ServerPlayerEntity)
        {
            ServerPlayerEntity entityPlayerMP = (ServerPlayerEntity) player;
            return entityPlayerMP.isCreative();
        } else if (player instanceof ClientPlayerEntity) { return Minecraft.getInstance().playerController.isInCreativeMode(); }
        return false;
    }

    // TODO: Need a replacement for these
//    @Override
//    public void clientConnect(ClientConnectedToServerEvent event)
//    {
//        CallBackManager.start();
//        ClientPlayManager.reset();
//    }
//
//    @Override
//    public void clientDisconnect(ClientDisconnectionFromServerEvent event)
//    {
//        CallBackManager.shutdown();
//        ClientFileManager.clearCache();
//        MultiPacketSerializedObjectManager.shutdown();
//    }
}
