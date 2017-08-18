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
import net.minecraftforge.common.config.Configuration;
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

    public static void syncConfig()
    {
        listenerRange = configFile.getFloat("listenerRange", Categories.CATEGORY_GENERAL.getName(), listenerRange, 10.0F, 64.0F, "Listener Range", "config.mxtune:listenerRange");
        groupPlayAbortDistance = configFile.getFloat("groupPlayAbortDistance", Categories.CATEGORY_GENERAL.getName(), groupPlayAbortDistance, 10.0F, 24.0F, "Group Play Abort Distance", "config.mxtune:groupPlayAbortDistance");
        hideWelcomeStatusMessage = configFile.getBoolean("hideWelcomeStatusMessage", Categories.CATEGORY_GENERAL.getName(), hideWelcomeStatusMessage, "Hide Welcome Status Message", "config.mxtune:hideWelcomeStatusMessage");
        mmlLink = configFile.getString("mmlLink", Categories.CATEGORY_GENERAL.getName(), "https://mabibeats.com/", "MML Site URL", "config.mxtune:mmlLink");
        
        autoConfigureChannels = configFile.getBoolean("autoConfigureChannels", Categories.CATEGORY_CLIENT.getName(), autoConfigureChannels, "Automatically configure sound channels", "config.mxtune:autoConfigureChannels");
        normalSoundChannelCount  = configFile.getInt("normalSoundChannelCount", Categories.CATEGORY_CLIENT.getName(), normalSoundChannelCount, 4, 60, "Number of normal sound channels to configure in the sound system (manual)", "config.mxtune:normalSoundChannelCount");
        streamingSoundChannelCount = configFile.getInt("streamingSoundChannelCount", Categories.CATEGORY_CLIENT.getName(), streamingSoundChannelCount, 4, 60, "Number of streaming sound channels to configure in the sound system (manual)", "config.mxtune:streamingSoundChannelCount");
        if (configFile.hasChanged()) configFile.save();	
	}
	
	@SideOnly(Side.CLIENT)
	public static void syncConfigClient()
	{
        if (configFile.hasChanged()) configFile.save();
	}
	
    public enum Categories {
        CATEGORY_GENERAL("general"),
        CATEGORY_CLIENT("client");

        private final String name;

        Categories(String name) {this.name = name;}

        public String getDesc() {return this.name;}
        
        public String getName() {return this.name;}

        public String getLangKey() {
            return "config.mxtune:ctgy." + this.name;
        }
    }
}
