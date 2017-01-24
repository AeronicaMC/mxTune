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

import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.groups.GroupManager;
import net.aeronica.mods.mxtune.handler.CommonEventHandler;
import net.aeronica.mods.mxtune.init.ModBlocks;
import net.aeronica.mods.mxtune.init.ModSounds;
import net.aeronica.mods.mxtune.util.Recipes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class CommonProxy implements IProxy
{

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {

    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        
    }
    
    @Override
    public void initConfiguration(FMLPreInitializationEvent event)
    {
        ModConfig.setConfigFile(new Configuration(event.getSuggestedConfigurationFile()));
        ModConfig.syncConfig();
    }

    @Override
    public void initPayload()
    {
        ModSounds.init();
        ModBlocks.registerTileEntities();
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
        MinecraftForge.EVENT_BUS.register(CommonEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(GroupManager.getInstance());
    }

    @Override
    public void registerRenderers() {}

    @Override
    public void registerKeyBindings() {}

    @Override
    public void registerHUD() {}

    abstract public boolean playerIsInCreativeMode(EntityPlayer player);

    @Override
    public EntityPlayer getPlayerEntity(MessageContext ctx)
    {
        return ctx.getServerHandler().playerEntity;
    }

    @Override
    public IThreadListener getThreadFromContext(MessageContext ctx)
    {
        return ctx.getServerHandler().playerEntity.getServer();
    }
}
