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

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.gui.GuiJamOverlay;
import net.aeronica.mods.mxtune.handler.CLEventHandler;
import net.aeronica.mods.mxtune.handler.KeyHandler;
import net.aeronica.mods.mxtune.handler.SoundEventHandler;
import net.aeronica.mods.mxtune.handler.TickHandler;
import net.aeronica.mods.mxtune.init.BlockModels;
import net.aeronica.mods.mxtune.init.ItemModels;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClientProxy extends CommonProxy
{
    @Override
    public Side getPhysicalSide() {return Side.CLIENT;}

    @Override
    public Side getEffectiveSide() {return FMLCommonHandler.instance().getEffectiveSide();}

    @Override
    public Minecraft getMinecraft() {return Minecraft.getMinecraft();}

    @Override
    public EntityPlayer getClientPlayer() {return Minecraft.getMinecraft().thePlayer;}

    @Override
    public World getClientWorld() {return Minecraft.getMinecraft().theWorld;}

    @Override
    public void spawnMusicParticles(EntityPlayer player) {}

    @Override
    public void initConfiguration(FMLPreInitializationEvent event)
    {
        super.initConfiguration(event);
    }

    @Override
    public void initPayload()
    {
        super.initPayload();
        // LayerThePlayer layer = new LayerThePlayer();
        // ((RenderPlayer)Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default")).addLayer(layer);
        // ((RenderPlayer)Minecraft.getMinecraft().getRenderManager().getSkinMap().get("slim")).addLayer(layer);
    }

    @Override
    public void registerRecipes()
    {
        super.registerRecipes();
    }

    @Override
    public void initEntities()
    {
        // EntityRegistry.getInstance().clientInit();
    }

    @Override
    public World getWorldByDimensionId(int dimension)
    {
        Side effectiveSide = FMLCommonHandler.instance().getEffectiveSide();
        if (effectiveSide == Side.SERVER)
        {
            return FMLClientHandler.instance().getServer().worldServerForDimension(dimension);
        } else
        {
            return getClientWorld();
        }
    }

    @Override
    public void registerEventHandlers()
    {
        super.registerEventHandlers();
        MinecraftForge.EVENT_BUS.register(SoundEventHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(TickHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(CLEventHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(BlockModels.getInstance());
    }

    @Override
    public void registerRenderers()
    {
        // ItemRegistry.getInstance().registerRenderers();
        /** Try using this in a LayerRenderer */
        // RenderingRegistry.registerEntityRenderingHandler(PlacardEntity.class,
        // new IRenderFactory<PlacardEntity>() {
        // @Override
        // public Render<? super PlacardEntity> createRenderFor(RenderManager
        // manager) { return new PlacardRender(manager); }
        // });

        /** The ALL important model loaders so we can use custom models */
        OBJLoader.INSTANCE.addDomain(MXTuneMain.MODID.toLowerCase());
        ItemModels.register();
        BlockModels.register();
    }

    @Override
    public void registerKeyBindings()
    {
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());
    }

    @Override
    public void initMML()
    {
        MIDISystemUtil.getInstance().mxTuneInit();
    }

    @Override
    public void registerHUD()
    {
        MinecraftForge.EVENT_BUS.register(new GuiJamOverlay(this.getMinecraft()));
    }

    @Override
    public void replacePlayerModel()
    {
        // ModelPlayerCustomized.replaceOldModel();
    }
    
    @Override
    public boolean playerIsInCreativeMode(EntityPlayer player)
    {
        if (player instanceof EntityPlayerMP)
        {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
            return entityPlayerMP.isCreative();
        } else if (player instanceof EntityPlayerSP) { return Minecraft.getMinecraft().playerController.isInCreativeMode(); }
        return false;
    }

    @Override
    public EntityPlayer getPlayerEntity(MessageContext ctx)
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

}