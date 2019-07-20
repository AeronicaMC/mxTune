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
import net.aeronica.mods.mxtune.gui.hud.GuiJamOverlay;
import net.aeronica.mods.mxtune.gui.mml.GuiStaffOverlay;
import net.aeronica.mods.mxtune.handler.KeyHandler;
import net.aeronica.mods.mxtune.managers.ClientFileManager;
import net.aeronica.mods.mxtune.managers.ClientPlayManager;
import net.aeronica.mods.mxtune.network.MultiPacketSerializedObjectManager;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import static net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import static net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

public class ClientProxy extends ServerProxy
{
    
    @Override
    public void preInit() { /*  NOP */ }

    @Override
    public void init() { /*  NOP */ }

    @Override
    public void postInit() { /*  NOP */ }

    @Override
    public void initEntities() { super.initEntities(); }

    @Override
    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule)
    {
        return Minecraft.getMinecraft().addScheduledTask(runnableToSchedule);
    }

    @Override
    public Side getPhysicalSide() { return Side.CLIENT; }

    @Override
    public Side getEffectiveSide() { return FMLCommonHandler.instance().getEffectiveSide(); }

    @Override
    public Minecraft getMinecraft() { return Minecraft.getMinecraft() ;}

    @Override
    public PlayerEntity getClientPlayer() { return Minecraft.getMinecraft().player; }

    @Override
    public PlayerEntity getPlayerByEntityID(int entityID)
    {
        return (PlayerEntity)  getClientPlayer().getEntityWorld().getEntityByID(entityID);
    }

    @Override
    public ClientWorld getClientWorld() { return Minecraft.getMinecraft().world; }

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
    public void initMML()
    {
        MIDISystemUtil.mxTuneInit();
        ((IReloadableResourceManager) getMinecraft().getResourceManager()).registerReloadListener(ClientAudio.INSTANCE);
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
        } else if (player instanceof ClientPlayerEntity) { return Minecraft.getMinecraft().playerController.isInCreativeMode(); }
        return false;
    }

    @Override
    public PlayerEntity getPlayerEntity(MessageContext ctx)
    {
        // Note that if you simply return 'Minecraft.getMinecraft().thePlayer',
        // your packets will not work as expected because you will be getting a
        // client player even when you are on the server!
        // Sounds absurd, but it's true.

        // Solution is to double-check side before returning the player:
        return (ctx.side.isClient() ? this.getClientPlayer() : super.getPlayerEntity(ctx));
    }

    @Override
    public IThreadListener getThreadFromContext(MessageContext ctx)
    {
        return (ctx.side.isClient() ? this.getMinecraft() : super.getThreadFromContext(ctx));
    }

    @Override
    public void clientConnect(ClientConnectedToServerEvent event)
    {
        CallBackManager.start();
        ClientPlayManager.reset();
    }

    @Override
    public void clientDisconnect(ClientDisconnectionFromServerEvent event)
    {
        CallBackManager.shutdown();
        ClientFileManager.clearCache();
        MultiPacketSerializedObjectManager.shutdown();
    }
}
