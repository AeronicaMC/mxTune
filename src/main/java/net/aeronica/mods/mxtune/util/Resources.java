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
package net.aeronica.mods.mxtune.util;

import java.net.URL;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.sound.SoundPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class Resources {

	public static final ResourceLocation mod_xm = new ResourceLocation(
			MXTuneMain.MODID.toLowerCase(), "music/test.pat");
	
	public static void testPlay(BlockPos pos) {
		/** URL url = Main.class.getResource("/assets/" */
		URL url = MXTuneMain.class.getResource("/assets/"
				+ mod_xm.getResourceDomain() + "/"
				+ mod_xm.getResourcePath());
		
		ModLogger.debug("Resource location: " + url);
		SoundPlayer.getInstance().playNewSound(url, null, pos, true, 1.0F);
	}
}
