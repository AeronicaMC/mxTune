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
package net.aeronica.mods.mxtune.proxy;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.groups.GroupManager;
import net.aeronica.mods.mxtune.handler.SREventHandler;
import net.aeronica.mods.mxtune.init.StartupBlocks;
import net.aeronica.mods.mxtune.init.StartupItems;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.Recipes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** NEW */
public abstract class CommonProxy implements IProxy
{
    @Override
    public void initConfiguration(FMLPreInitializationEvent event)
    {
        ModConfig.setConfigFile(new Configuration(event.getSuggestedConfigurationFile()));
        ModConfig.syncConfig();
    }

    @Override
    public void initPayload()
    {
        StartupItems.register();
        StartupBlocks.register();
    }

    @Override
    public void registerRecipes()
    {
        Recipes.register();
    }

    @Override
    public void initMML() {}

    @Override
    public Entity getEntityById(int dimension, int id)
    {
        return getEntityById(getWorldByDimensionId(dimension), id);
    }

    @Override
    public Entity getEntityById(World world, int id)
    {
        return world.getEntityByID(id);
    }

    @Override
    public void registerEventHandlers()
    {
        MinecraftForge.EVENT_BUS.register(GroupManager.getInstance());
        MinecraftForge.EVENT_BUS.register(SREventHandler.getInstance());
    }

    @Override
    public void registerRenderers() {}

    @Override
    public void registerKeyBindings() {}

    @Override
    public void registerHUD() {}

    private static MinecraftServer minecraftServer = null;
    
    public MinecraftServer getMinecraftServer() {return minecraftServer;}
    
    @Override
    public void serverStarted(FMLServerStartedEvent event)
    {
        WorldServer worldServer = DimensionManager.getWorld(0); // default world
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "FakePlayer");
        FakePlayer fakePlayer = new FakePlayer(worldServer, gameProfile);
        minecraftServer = fakePlayer.mcServer;
        ModLogger.debug("serverStarted: " + (minecraftServer.isDedicatedServer() ? " Dedicated Server" : " Integrated Server") + " - " + event.getModState());
        MusicOptionsUtil.dumpAllPlayers(minecraftServer);
    }

    abstract public boolean playerIsInCreativeMode(EntityPlayer player);

    @Override
    public EntityPlayer getPlayerEntity(MessageContext ctx)
    {
        ModLogger.logInfo("Retrieving player from CommonProxy for message on side " + ctx.side);
        return ctx.getServerHandler().playerEntity;
    }

    @Override
    public IThreadListener getThreadFromContext(MessageContext ctx)
    {
        return ctx.getServerHandler().playerEntity.getServer();
    }
}
