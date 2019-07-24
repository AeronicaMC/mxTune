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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.config.MXTuneConfig;
import net.aeronica.mods.mxtune.init.IReBakeModel;
import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.init.ModModelManager;
import net.aeronica.mods.mxtune.render.PlacardRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import static net.aeronica.mods.mxtune.managers.GroupHelper.getClientMembers;
import static net.aeronica.mods.mxtune.managers.GroupHelper.getIndex;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, value=Dist.CLIENT)
public class ClientEventHandler
{
    private static Minecraft mc = Minecraft.getInstance();
    private static PlacardRenderer placardRenderer = PlacardRenderer.getInstance();
    private static boolean lastShowPlayerNameState;

    private ClientEventHandler() {}

    // Render Placards
    @SubscribeEvent
    public static void onRenderPlayerEvent(RenderLivingEvent.Specials.Post event)
    {
        if (
                (event.getEntity() instanceof PlayerEntity) &&
                        !event.getEntity().isInvisible() &&
                        !mc.gameSettings.showDebugInfo &&
                        !mc.gameSettings.hideGUI &&
                        getClientMembers().containsKey(event.getEntity().getEntityId()) &&
                        !(mc.gameSettings.thirdPersonView == 0 && mc.player.equals(event.getEntity())))
        {
            placardRenderer.setPlacard(getIndex(event.getEntity().getEntityId()));
            placardRenderer.doRender(event);
        }

    }

    @SubscribeEvent
    public static void textureRestitchEvent(TextureStitchEvent.Post e)
    {
        ModModelManager.getTESRRenderer().stream().filter(p -> p instanceof IReBakeModel).forEach(p -> ((IReBakeModel)p).reBakeModel());
    }

    @SubscribeEvent
    public static void onEvent(ItemTooltipEvent event)
    {
        if (event.getItemStack().getItem().equals(ModItems.ITEM_BAND_AMP))
        {
            event.getToolTip().add(new TranslationTextComponent(TextFormatting.GREEN + "tile.mxtune:band_amp.help"));
        }
    }

    // OBS likes unique window titles. Update on logon and/or changing dimension
    @SubscribeEvent
    public static void onEvent(EntityJoinWorldEvent event)
    {
        if ((event.getEntity() instanceof ClientPlayerEntity))
        {
            ClientPlayerEntity player = (ClientPlayerEntity) event.getEntity();
            String windowTitle = String.format("Minecraft %s", SharedConstants.getVersion().getName());

            if (MXTuneConfig.CLIENT.showPlayerName.get())
                windowTitle = String.format("Minecraft %s - %s", SharedConstants.getVersion().getName(), player.getScoreboardName());

            if (lastShowPlayerNameState != MXTuneConfig.CLIENT.showPlayerName.get())
            {
                long handle = mc.mainWindow.getHandle();
                GLFW.glfwSetWindowTitle(handle, windowTitle);
                lastShowPlayerNameState = MXTuneConfig.CLIENT.showPlayerName.get();
            }
        }
    }
}
