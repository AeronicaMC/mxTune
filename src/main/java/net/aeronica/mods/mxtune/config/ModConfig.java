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

import java.util.Arrays;
import java.util.regex.Pattern;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiEditArray;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModConfig
{
	private static Configuration configFile;
	/** General Configuration Settings */
	private static float listenerRange = 24.0F;
	private static float groupPlayAbortDistance = 10.0F;
	private static boolean hideWelcomeStatusMessage = false;
	private static String mmlLink = "https://mabibeats.com/";
	/** Client Configuration Settings */
    private static boolean autoConfigureChannels = true;
    private static int normalSoundChannelCount = 24;
    private static int streamingSoundChannelCount = 8;
    
    /** Recipes */
    private static Property enabledRecipes;
    private static Pattern validationPattern = Pattern.compile("(mxtune:[a-z]*([_|a-z])*[.]*[a-z|_]*)"); 
    public static final String[] receipeDefaults = {
            "mxtune:block_piano", "mxtune:item_inst.lute", "mxtune:item_inst.ukulele", "mxtune:item_inst.mandolin",
            "mxtune:item_inst.whistle", "mxtune:item_inst.roncadora", "mxtune:item_inst.flute", "mxtune:item_inst.chalumeau",
            "mxtune:item_inst.tuba", "mxtune:item_inst.lyre", "mxtune:item_inst.electric_guitar", "mxtune:item_inst.violin",
            "mxtune:item_inst.cello", "mxtune:item_inst.harp", "mxtune:item_inst.tuned_flute", "mxtune:item_inst.tuned_whistle",
            "mxtune:item_inst.bass_drum", "mxtune:item_inst.snare_drum", "mxtune:item_inst.cymbels", "mxtune:item_inst.hand_chimes",
            "mxtune:item_inst.recorder", "mxtune:item_inst.trumpet", "mxtune:item_inst.harpsichord", "mxtune:item_inst.harpsichord_coupled",
            "mxtune:item_inst.standard", "mxtune:item_inst.orchestra", "mxtune:item_musicpaper"
    };
    
	/** @return the configFile */
	public static Configuration getConfigFile() {return configFile;}

	/** @param configFile the configFile to set */
	public static void setConfigFile(Configuration configFile) {ModConfig.configFile = configFile;}

	public static float getListenerRange() {return listenerRange;}
		
	public static float getGroupPlayAbortDistance() {return groupPlayAbortDistance;}

    public static boolean hideWelcomeStatusMessage() {return hideWelcomeStatusMessage;}
	
    public static boolean getAutoConfigureChannels() {return autoConfigureChannels;}
    
    public static int getNormalSoundChannelCount() {return normalSoundChannelCount;}
    
    public static int getStreamingSoundChannelCount() {return streamingSoundChannelCount;}

    public static String getMmlLink() {return mmlLink;}
    
    public static String[] getEnabledRecipes() {return enabledRecipes.getStringList();}

    public static void syncConfig()
    {
        listenerRange = configFile.getFloat("listenerRange", Categories.CATEGORY_GENERAL.getName(), listenerRange, 10.0F, 64.0F, "Listener Range", "mxtune.configgui.listenerRange");
        groupPlayAbortDistance = configFile.getFloat("groupPlayAbortDistance", Categories.CATEGORY_GENERAL.getName(), groupPlayAbortDistance, 10.0F, 24.0F, "Group Play Abort Distance", "mxtune.configgui.groupPlayAbortDistance");
        hideWelcomeStatusMessage = configFile.getBoolean("hideWelcomeStatusMessage", Categories.CATEGORY_GENERAL.getName(), hideWelcomeStatusMessage, "Hide Welcome Status Message", "mxtune.configgui.hideWelcomeStatusMessage");
        mmlLink = configFile.getString("mmlLink", Categories.CATEGORY_GENERAL.getName(), "https://mabibeats.com/", "MML Site URL", "mxtune.configgui.mmlLink");
        
        autoConfigureChannels = configFile.getBoolean("autoConfigureChannels", Categories.CATEGORY_CLIENT.getName(), autoConfigureChannels, "Automatically configure sound channels", "mxtune.configgui.autoConfigureChannels");
        normalSoundChannelCount  = configFile.getInt("normalSoundChannelCount", Categories.CATEGORY_CLIENT.getName(), normalSoundChannelCount, 4, 60, "Number of normal sound channels to configure in the sound system (manual)", "mxtune.configgui.normalSoundChannelCount");
        streamingSoundChannelCount = configFile.getInt("streamingSoundChannelCount", Categories.CATEGORY_CLIENT.getName(), streamingSoundChannelCount, 4, 60, "Number of streaming sound channels to configure in the sound system (manual)", "mxtune.configgui.streamingSoundChannelCount");
                
        enabledRecipes = configFile.get(Categories.CATEGPRY_RECIPES.getName(), "enabledRecipes", receipeDefaults, "Enabled Recipes", true, receipeDefaults.length, validationPattern).setLanguageKey("mxtune.configgui.enabledRecipes").setArrayEntryClass(EnabledRecipeEntry.class).setRequiresMcRestart(true);
        if (configFile.hasChanged()) configFile.save();
	}
	
	@SideOnly(Side.CLIENT)
	public static void syncConfigClient()
	{
        if (configFile.hasChanged()) configFile.save();
	}
	
    public enum Categories {
        CATEGORY_GENERAL("general"),
        CATEGORY_CLIENT("client"),
        CATEGPRY_RECIPES("recipes");

        private final String name;

        Categories(String name) {this.name = name;}

        public String getDesc() {return this.name;}
        
        public String getName() {return this.name;}

        public String getLangKey() {
            return MXTuneMain.MODID.toLowerCase() +".configgui.ctgy." + this.name;
        }
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
            Boolean OK = Arrays.asList(ModConfig.receipeDefaults).contains(textFieldValue.getText());
            textFieldValue.setTextColor((int) (OK ? 0x00FF00 : 0xFF0000));
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
        }
    }
}
