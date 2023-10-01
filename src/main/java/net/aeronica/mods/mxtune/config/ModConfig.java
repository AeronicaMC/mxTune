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

package net.aeronica.mods.mxtune.config;

import com.google.common.collect.Maps;
import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class ModConfig
{
    private ModConfig() { /* NOP */ }

    /** Client Configuration Settings */
    @Config(modid = Reference.MOD_ID, name = Reference.MOD_ID + "/" + Reference.MOD_ID + "_client", category="client")
    @LangKey("config.mxtune.category.client")
    public static class ConfigClient
    {
        private ConfigClient() {/* NOP */}

        @LangKey("config.mxtune.vm.vanillaMusic")
        @Comment("Vanilla Background Music")
        public static final VanillaMusic vanillaMusic = new VanillaMusic();

        @LangKey("config.mxtune.autoConfigureChannels")
        @Comment("Sound Channel Configuration")
        public static final Sound sound = new Sound();

        @LangKey("config.mxtune.mmlLink")
        @Comment("Internet Resources")
        public static final Links links = new Links();

        @Config.LangKey("config.mxtune.playerNameInWindowTitle")
        @Config.Comment("Show player name in window title. Updates on logon and/or changing dimension.")
        public static final WindowTitle windowTitle = new WindowTitle();

        public static class Sound
        {
            @Name("Automatically configure sound channels")
            @LangKey("config.mxtune.autoConfigureChannels")
            public boolean autoConfigureChannels = true;
        }
        
        public static class Links
        {
            @Name("Site Links")
            @LangKey("config.mxtune.mmlLink")
            public String site = "https://musicalnexus.net/";
        }

        public static class WindowTitle
        {
            @Name("Show player name in window title")
            @Config.LangKey("config.mxtune.playerNameInWindowTitle")
            public boolean showPlayerName = false;
        }

        public static class VanillaMusic
        {
            @Name("Disable Creative Music")
            @Config.LangKey("config.mxtune.vm.disableCreativeMusic")
            public boolean disableCreativeMusic = false;

            @Name("Disable Credits Music")
            @Config.LangKey("config.mxtune.vm.disableCreditsMusic")
            public boolean disableCreditsMusic = false;

            @Name("Disable End Dragon Music")
            @Config.LangKey("config.mxtune.vm.disableEndDragonMusic")
            public boolean disableEndDragonMusic = false;

            @Name("Disable End Music")
            @Config.LangKey("config.mxtune.vm.disableEndMusic")
            public boolean disableEndMusic = false;

            @Name("Disable Game Music")
            @Config.LangKey("config.mxtune.vm.disableGameMusic")
            public boolean disableGameMusic = false;

            @Name("Disable Menu Music")
            @Config.LangKey("config.mxtune.vm.disableMenuMusic")
            public boolean disableMenuMusic = false;

            @Name("Disable Nether Music")
            @Config.LangKey("config.mxtune.vm.disableNetherMusic")
            public boolean disableNetherMusic = false;
        }
    }
    
    @Config(modid= Reference.MOD_ID, name = Reference.MOD_ID + "/" + Reference.MOD_ID + "_general", category="general")
    @LangKey("config.mxtune.category.general")
    public static class ConfigGeneral
    {
        private ConfigGeneral() {/* NOP */}

        @Comment("General Configuration")
        @LangKey("config.mxtune.generalConfig")
        public static final General general = new General();

        @Comment("Instrument Options")
        @LangKey("config.mxtune.generalConfig.instrumentOptions")
        public static final InstrumentOptions instrumentOptions = new InstrumentOptions();

        @Comment("Mob Spawn Options")
        @LangKey("config.mxtune.generalConfig.spawnOptions")
        public static final MobSpawnOptions mobSpawnOptions = new MobSpawnOptions();

        public static class General
        {
            @Name("Show Welcome Status Message")
            @LangKey("config.mxtune.showWelcomeStatusMessage")
            public boolean showWelcomeStatusMessage = false;
            
            @Name("Listener Range")
            @LangKey("config.mxtune.listenerRange")
            @RangeInt(min=10, max=64)
            public int listenerRange = 24;

            @Name("Group Play Abort Distance")
            @LangKey("config.mxtune.groupPlayAbortDistance")
            @RangeInt(min=10, max=24)    
            public int groupPlayAbortDistance = 16;

            @Name("More Debug Messages")
            @LangKey("config.mxtune.moreDebugMessages")
            public boolean moreDebugMessages = false;

            @Name("Disable JAM Party Right Click")
            @LangKey("config.mxtune.disableJAMPartyRightClick")
            public boolean disableJAMPartyRightClick = false;
        }

        public static class InstrumentOptions
        {
            @Name("Enable Band Amp Full-Bright effect when playing")
            @LangKey("config.mxtune.enableBandAmpFullBrightEffect")
            public boolean enableBandAmpFullBrightEffect = true;
        }

        public static class MobSpawnOptions
        {
            @Name("Enable Golden Skeleton")
            @LangKey("config.mxtune.enableGoldenSkeleton")
            @RequiresMcRestart()
            public boolean enableGoldenSkeleton = true;

            @Name("Enable Timpani of Doom")
            @LangKey("config.mxtune.enableTimpaniOfDoom")
            @RequiresMcRestart()
            public boolean enableTimpaniOfDoom = true;
        }
    }
    
    @Config(modid = Reference.MOD_ID, name = Reference.MOD_ID + "/" + Reference.MOD_ID + "_recipes", type = Type.INSTANCE, category="recipe")
    @LangKey("config.mxtune.category.recipes")
    public static class ConfigRecipes
    {
        private ConfigRecipes() {/* NOP */}

        @LangKey("config.mxtune.enabledRecipes")
        @Name("Toggles")
        @Comment({"mxTune Recipes", "Requires a Server Restart if Changed!", "B:<name>=(true|false)"})
        @RequiresMcRestart()
        public static Map<String, Boolean> recipeToggles;

        private static final String[] modItemRecipeNames = {
                "band_amp", "spinet_piano", "flute_pan"
        };
        
        static
        {
            recipeToggles = Maps.newHashMap();
            for (String modItemRecipeName : modItemRecipeNames)
            {
                recipeToggles.put(modItemRecipeName, true);
            }
        }
    }

    public static boolean isJAMPartyRightClickDisabled() { return ConfigGeneral.general.disableJAMPartyRightClick; }

    public static float getListenerRange() {return (float)ConfigGeneral.general.listenerRange;}

    public static float getGroupPlayAbortDistance() {return (float)ConfigGeneral.general.groupPlayAbortDistance;}

    public static boolean showWelcomeStatusMessage() {return ConfigGeneral.general.showWelcomeStatusMessage;}

    public static boolean moreDebugMessages() { return ConfigGeneral.general.moreDebugMessages; }

    public static boolean getAutoConfigureChannels() {return ConfigClient.sound.autoConfigureChannels;}

    public static boolean isCreativeMusicDisabled() {return ConfigClient.vanillaMusic.disableCreativeMusic;}

    public static boolean isCreditsMusicDisabled() {return ConfigClient.vanillaMusic.disableCreditsMusic;}

    public static boolean isDragonMusicDisabled() {return ConfigClient.vanillaMusic.disableEndDragonMusic;}

    public static boolean isEndMusicDisabled() {return ConfigClient.vanillaMusic.disableEndMusic;}

    public static boolean isGameMusicDisabled() {return ConfigClient.vanillaMusic.disableGameMusic;}

    public static boolean isMenuMusicDisabled() {return ConfigClient.vanillaMusic.disableMenuMusic;}

    public static boolean isNetherMusicDisabled() {return ConfigClient.vanillaMusic.disableNetherMusic;}

    public static boolean isGoldenSkeletonEnabled() {return ConfigGeneral.mobSpawnOptions.enableGoldenSkeleton;}

    public static boolean isTimpaniOfDoomEnabled() {return ConfigGeneral.mobSpawnOptions.enableTimpaniOfDoom;}

    public static String getMmlLink() {return ConfigClient.links.site;}

    public static boolean isBandAmpFullBrightEffectEnabled() {return ConfigGeneral.instrumentOptions.enableBandAmpFullBrightEffect;}

    /**
     * Will only allow this mods recipes to be disabled
     * @param stackIn stack to be tested
     * @return recipe state
     */
    public static boolean isRecipeEnabled(ItemStack stackIn)
    {
        // strip off "item." or "tile." and "instrument." to get the raw item name without domain and item base names
        String itemName = stackIn.getTranslationKey().replaceFirst("(item.|tile.)" + Reference.MOD_ID + ":", "");
        itemName = itemName.replaceFirst("multi_inst.", "");
        boolean enableState = !ConfigRecipes.recipeToggles.containsKey(itemName) || (ConfigRecipes.recipeToggles.get(itemName) && !itemName.contains(":"));
        ModLogger.debug("Recipe Enabled? %s %s", itemName, enableState);
        return enableState;
    }
    
    public static boolean isRecipeHidden(ItemStack stackIn)
    {
        return !isRecipeEnabled(stackIn);
    }
    
    @Mod.EventBusSubscriber()
    public static class RegistrationHandler
    {
        private RegistrationHandler() {/* NOP */}

        @SubscribeEvent
        public static void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            ModLogger.debug("On ConfigChanged: %s", event.getModID());
            if(event.getModID().equals(Reference.MOD_ID))
                sync();
        }
        public static void sync()
        {
            ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
        }
    }
}
