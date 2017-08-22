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

import java.util.regex.Pattern;

import net.aeronica.mods.mxtune.gui.GuiJamOverlay;
import net.aeronica.mods.mxtune.handler.KeyHandler;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.config.GuiEditArray;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClientProxy extends CommonProxy
{
    
    private static Pattern validationPattern = Pattern.compile("(mxtune:[a-z]*([_|a-z])*[.]*[a-z|_]*)"); 
    
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
        
    }
    
    @Override
    public Side getPhysicalSide() {return Side.CLIENT;}

    @Override
    public Side getEffectiveSide() {return FMLCommonHandler.instance().getEffectiveSide();}

    @Override
    public Minecraft getMinecraft() {return Minecraft.getMinecraft();}

    @Override
    public EntityPlayer getClientPlayer() {return Minecraft.getMinecraft().player;}

    @Override
    public World getClientWorld() {return Minecraft.getMinecraft().world;}

    @Override
    public void spawnMusicParticles(EntityPlayer player) {}

    @Override
    public void initEntities()
    {
        super.initEntities();
    }

    @Override
    public World getWorldByDimensionId(int dimension)
    {
        Side effectiveSide = FMLCommonHandler.instance().getEffectiveSide();
        if (effectiveSide == Side.SERVER)
        {
            return FMLClientHandler.instance().getServer().getWorld(dimension);
        } else
        {
            return getClientWorld();
        }
    }

    @Override
    public void registerEventHandlers()
    {
        super.registerEventHandlers();
    }

    @Override
    public void registerRenderers()
    {
    }

    @Override
    public void registerKeyBindings()
    {
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());
    }

    @Override
    public void initMML()
    {
        MIDISystemUtil.mxTuneInit();
    }

    @Override
    public void registerHUD()
    {
        MinecraftForge.EVENT_BUS.register(GuiJamOverlay.getInstance());
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

    public static class EnabledRecipeEntry extends GuiEditArrayEntries.StringEntry
    {

        public EnabledRecipeEntry(GuiEditArray owningScreen, GuiEditArrayEntries owningEntryList, IConfigElement configElement, Object value)
        {
            super(owningScreen, owningEntryList, configElement, value);
        }
        
        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial)
        {
//            Boolean OK = Arrays.asList(ModConfig.receipeDefaults).contains(textFieldValue.getText());
//            textFieldValue.setTextColor((int) (OK ? 0x00FF00 : 0xFF0000));
//            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
        }
    }
    
}