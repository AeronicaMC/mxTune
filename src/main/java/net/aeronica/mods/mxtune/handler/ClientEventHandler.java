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
package net.aeronica.mods.mxtune.handler;

import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.init.IReBakeModel;
import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.init.ModModelManager;
import net.aeronica.mods.mxtune.render.PlacardRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.Display;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventHandler
{
    
    static Minecraft mc = Minecraft.getMinecraft();
    static PlacardRenderer placardRenderer = PlacardRenderer.getInstance();

    private ClientEventHandler() {}

    /** Render Placards */
    @SubscribeEvent
    public static void onRenderPlayerEvent(RenderLivingEvent.Specials.Post<EntityLivingBase> event)
    {
        if (event.getEntity() instanceof EntityPlayer && !event.getEntity().isInvisible() && !mc.gameSettings.showDebugInfo && !mc.gameSettings.hideGUI)
        {
            if (
                    GROUPS.getClientMembers() != null /* (mc.thePlayer.equals(player)) */
                    && GROUPS.getClientMembers().containsKey(event.getEntity().getEntityId()) &&
                    !(mc.gameSettings.thirdPersonView == 0 && mc.player.equals(event.getEntity())))
            {
                placardRenderer.setPlacard(GROUPS.getIndex(event.getEntity().getEntityId()));
                placardRenderer.doRender(event);
            }
        }
    } 
    
    @SubscribeEvent
    public static void textureRestitchEvent(TextureStitchEvent.Post e)
    {
        ModModelManager.getTESRRenderers().stream().filter(p -> p instanceof IReBakeModel).forEach(p -> ((IReBakeModel)p).reBakeModel());
    }

    @SubscribeEvent
    public static void onEvent(ItemTooltipEvent event)
    {
        if (event.getItemStack().getItem().equals(ModItems.ITEM_BAND_AMP))
        {
            event.getToolTip().add(TextFormatting.GREEN + I18n.format("tile.mxtune:band_amp.help"));
        }
    }

    /*
     * OBS likes unique window titles. Update on logon and/or changing dimension
     */
    @SubscribeEvent
    public static void onEvent(EntityJoinWorldEvent event)
    {
        if ((event.getEntity() instanceof EntityPlayerSP))
        {
            if (ModConfig.ConfigClient.windowTitle.showPlayerName)
                Display.setTitle(String.format("Minecraft 1.12.2 %s", event.getEntity().getName()));
            else
                Display.setTitle("Minecraft 1.12.2");
        }
    }
}
