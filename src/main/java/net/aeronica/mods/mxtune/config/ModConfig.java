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
	/** Client Configuration Settings */
	
	/** @return the configFile */
	public static Configuration getConfigFile() {return configFile;}

	/** @param configFile the configFile to set */
	public static void setConfigFile(Configuration configFile) {ModConfig.configFile = configFile;}

	public static float getListenerRange() {return listenerRange;}
			
	public static void syncConfig()
    {
        listenerRange = configFile.getFloat("listenerRange", Categories.CATEGORY_GENERAL.getName(), listenerRange, 4.0F, 64.0F, "Listener Range", "mxtune.configgui.listenerRange");
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
            return MXTuneMain.MODID.toLowerCase() +".configgui.ctgy." + this.name;
        }
    }
}
