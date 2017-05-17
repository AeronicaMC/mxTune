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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class ModGuiFactory implements IModGuiFactory {

	public ModGuiFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(Minecraft minecraftInstance) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return ModConfigGui.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("deprecation")
    @Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static class ModConfigGui extends GuiConfig {

	    public ModConfigGui(GuiScreen parentScreen)
	    {
	        super(parentScreen,  getConfigElements(),
	                MXTuneMain.MODID, false, false, GuiConfig.getAbridgedConfigPath(ModConfig.getConfigFile().toString()));
	    }

	    private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> list = new ArrayList<IConfigElement>();
            for(ModConfig.Categories category : ModConfig.Categories.values()) {
                list.add(new DummyConfigElement.DummyCategoryElement(
                            category.getDesc(),
                            category.getLangKey(),
                            new ConfigElement(ModConfig.getConfigFile().getCategory(category.getName())).getChildElements()));
            }
            return list;
        }
	}

    @Override
    public boolean hasConfigGui()
    {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen)
    {
        return new ModConfigGui(parentScreen);
    }
}
