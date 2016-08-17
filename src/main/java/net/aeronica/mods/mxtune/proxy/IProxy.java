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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public interface IProxy
{
    /**
     * @return The physical side, is always Side.SERVER on the server and
     *         Side.CLIENT on the client
     */
    Side getPhysicalSide();

    /**
     * @return The effective side, on the server, this is always Side.SERVER, on
     *         the client it is dependent on the thread
     */
    Side getEffectiveSide();

    /** Registers the relevant event handlers for the current side */
    void registerEventHandlers();

    /** Initialize Items and Blocks in Common PreInit */
    void initPayload();

    /**
     * Register the renderers during client PreInit, does nothing on the server
     */
    void registerRenderers();

    /** Register recipes in Common Init */
    void registerRecipes();

    /** Initialize MML in Client Init */
    void initMML();

    /**
     * Register the key bindings during client Init, does nothing on the server
     */
    void registerKeyBindings();

    /** Initializes and reads the configuration file with the options relevant to the current side */
    void initConfiguration(FMLPreInitializationEvent event);

    /** Initializes the EntityRegistry */
    void initEntities();

    /** Register HUD in Client PostInit */
    void registerHUD();
    
    /** Returns the instance of Minecraft */
    Minecraft getMinecraft();
    
    /** Returns the instance of the EntityPlayer on the client, null on the server */
    EntityPlayer getClientPlayer();

    /** Returns the client World object on the client, null on the server */
    World getClientWorld();

    /** Returns the World object corresponding to the dimension id */
    World getWorldByDimensionId(int dimension);

    /** Returns the entity in that dimension with that id */
    Entity getEntityById(int dimension, int id);

    /** Returns the entity in that World object with that id */
    Entity getEntityById(World world, int id);

    /** Spawns the music particles on the client, does nothing on the server */
    void spawnMusicParticles(EntityPlayer player);

    /** Replace the player model on the client, do nothing on the server */
    void replacePlayerModel();

    /** helper to determine whether the given player is in creative mode */
    boolean playerIsInCreativeMode(EntityPlayer player);

    /** Returns a side-appropriate EntityPlayer for use during message handling */
    EntityPlayer getPlayerEntity(MessageContext ctx);

    /**
     * Returns the current thread based on side during message handling, used
     * for ensuring that the message is being handled by the main thread
     */
    IThreadListener getThreadFromContext(MessageContext ctx);
}
