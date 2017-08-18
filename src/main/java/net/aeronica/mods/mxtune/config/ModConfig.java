/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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
package net.aeronica.mods.mxtune.config;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class ModConfig
{
    
    private ModConfig() { /* NOP */ }

    @Config(modid=MXTuneMain.MODID, name="mxtune/mxtune", type=Config.Type.INSTANCE, category="server")
    @Config.LangKey("config.mxtune:title")
    public static class Server
    {
        /** General Configuration Settings */
        @Config.Name("Listener Range")
        @Config.LangKey("mxtune.configgui.listenerRange")
        @Config.RangeDouble(min=10.0D, max=64.0D)
        public static float listenerRange = 24.0F;

        @Config.Name("Group Play Abort Distance")
        @Config.LangKey("mxtune.configgui.groupPlayAbortDistance")
        @Config.RangeDouble(min=10.0D, max=24.0D)    
        public static float groupPlayAbortDistance = 10.0F;

        @Config.Name("Hide Welcome Status Message")
        @Config.LangKey("mxtune.configgui.hideWelcomeStatusMessage")   
        public static boolean hideWelcomeStatusMessage = false;

        @Config.Name("Enabled Recipes")
        @Config.LangKey("mxtune.configgui.enabledRecipes")
        @Config.RequiresMcRestart
        public static String[] enabledRecipes = {
                "mxtune:block_piano", "mxtune:item_inst.lute", "mxtune:item_inst.ukulele", "mxtune:item_inst.mandolin",
                "mxtune:item_inst.whistle", "mxtune:item_inst.roncadora", "mxtune:item_inst.flute", "mxtune:item_inst.chalumeau",
                "mxtune:item_inst.tuba", "mxtune:item_inst.lyre", "mxtune:item_inst.electric_guitar", "mxtune:item_inst.violin",
                "mxtune:item_inst.cello", "mxtune:item_inst.harp", "mxtune:item_inst.tuned_flute", "mxtune:item_inst.tuned_whistle",
                "mxtune:item_inst.bass_drum", "mxtune:item_inst.snare_drum", "mxtune:item_inst.cymbels", "mxtune:item_inst.hand_chimes",
                "mxtune:item_inst.recorder", "mxtune:item_inst.trumpet", "mxtune:item_inst.harpsichord", "mxtune:item_inst.harpsichord_coupled",
                "mxtune:item_inst.standard", "mxtune:item_inst.orchestra", "mxtune:item_musicpaper"
        };
    }

    /** Client Configuration Settings */
    @Config(modid = MXTuneMain.MODID, name="mxtune/mxtune_client", category="client")
    @Config.LangKey("config.mxtune:client_title")
    public static class Client
    {   

        @Config.Comment("Sound Channel Configuration")
        @Config.LangKey("mxtune.configgui.soundChannelConfig")
        public static Sound sound = new Sound();
        
        @Config.Comment("Internet Resources")
        @Config.LangKey("mxtune.configgui.internetResouces")
        public static Links links = new Links();
        
        public static class Sound
        {
            @Config.Name("Automatically configure sound channels")
            @Config.LangKey("mxtune.configgui.autoConfigureChannels")
            public boolean autoConfigureChannels = true;

            @Config.Name("Number of normal sound channels (manual)")
            @Config.LangKey("mxtune.configgui.normalSoundChannelCount")
            @Config.RangeInt(min=4, max=60)
            public int normalSoundChannelCount = 24;

            @Config.Name("Number of streaming sound channels (manual)")
            @Config.LangKey("mxtune.configgui.streamingSoundChannelCount")
            @Config.RangeInt(min=4, max=60)
            public int streamingSoundChannelCount = 8;
        }
        
        public static class Links
        {
            @Config.Name("Site Links")
            @Config.LangKey("mxtune.configgui.mmlLink")
            public String site = "https://mabibeats.com/";
        }
    }

    /** @return the configFile */
    public static Configuration getConfigFile() {return null;}

    /** @param configFile the configFile to set */
    public static void setConfigFile(Configuration configFile) { /* TODO */ }

    public static float getListenerRange() {return Server.listenerRange;}

    public static float getGroupPlayAbortDistance() {return Server.groupPlayAbortDistance;}

    public static boolean hideWelcomeStatusMessage() {return Server.hideWelcomeStatusMessage;}

    public static boolean getAutoConfigureChannels() {return Client.sound.autoConfigureChannels;}

    public static int getNormalSoundChannelCount() {return Client.sound.normalSoundChannelCount;}

    public static int getStreamingSoundChannelCount() {return Client.sound.streamingSoundChannelCount;}

    public static String getMmlLink() {return Client.links.site;}

    public static String[] getEnabledRecipes() {return Server.enabledRecipes;}

    //    public static void syncConfig()
    //    {
    //        Configuration configFile = null;
    //        listenerRange = configFile.getFloat("listenerRange", "general", listenerRange, 10.0F, 64.0F, "Listener Range", "mxtune.configgui.listenerRange");
    //        groupPlayAbortDistance = configFile.getFloat("groupPlayAbortDistance", "general", groupPlayAbortDistance, 10.0F, 24.0F, "Group Play Abort Distance", "mxtune.configgui.groupPlayAbortDistance");
    //        hideWelcomeStatusMessage = configFile.getBoolean("hideWelcomeStatusMessage", "general", hideWelcomeStatusMessage, "Hide Welcome Status Message", "mxtune.configgui.hideWelcomeStatusMessage");
    //        mmlLink = configFile.getString("mmlLink", "general", "https://mabibeats.com/", "MML Site URL", "mxtune.configgui.mmlLink");
    //        
    //        autoConfigureChannels = configFile.getBoolean("autoConfigureChannels", "client", autoConfigureChannels, "Automatically configure sound channels", "mxtune.configgui.autoConfigureChannels");
    //        normalSoundChannelCount  = configFile.getInt("normalSoundChannelCount", "client", normalSoundChannelCount, 4, 60, "Number of normal sound channels to configure in the sound system (manual)", "mxtune.configgui.normalSoundChannelCount");
    //        streamingSoundChannelCount = configFile.getInt("streamingSoundChannelCount", "client", streamingSoundChannelCount, 4, 60, "Number of streaming sound channels to configure in the sound system (manual)", "mxtune.configgui.streamingSoundChannelCount");
    //                
    //        //enabledRecipes = configFile.get("recipes", "enabledRecipes", receipeDefaults, "Enabled Recipes", true, receipeDefaults.length, validationPattern).setLanguageKey("mxtune.configgui.enabledRecipes").setArrayEntryClass(ModConfig.EnabledRecipeEntry.class).setRequiresMcRestart(true);
    //        if (configFile.hasChanged()) configFile.save();
    //	}

    @Mod.EventBusSubscriber
    public static class RegistrationHandler {

        @SubscribeEvent
        public static void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            ModLogger.info("On ConfigChanged: %s", event.getModID());
            if(event.getModID().equals(MXTuneMain.MODID))
            {
                ConfigManager.sync(MXTuneMain.MODID, Config.Type.INSTANCE);
            }
        }
    }

}
