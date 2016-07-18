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

import net.aeronica.mods.mxtune.sound.CodecLiquinth;
import net.aeronica.mods.mxtune.sound.SoundPlayer;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecIBXM;

public class SoundEventHandler
{

    private SoundEventHandler() {}

    private static class SoundEventHandlerHolder {private static final SoundEventHandler INSTANCE = new SoundEventHandler();}

    public static SoundEventHandler getInstance() {return SoundEventHandlerHolder.INSTANCE;}

    @SubscribeEvent
    public void SoundSetupEvent(SoundSetupEvent event) throws SoundSystemException
    {
        ModLogger.debug("SoundSetupEvent");
        try
        {
            SoundSystemConfig.setCodec("mod", CodecIBXM.class);
            SoundSystemConfig.setCodec("s3m", CodecIBXM.class);
            SoundSystemConfig.setCodec("xm", CodecIBXM.class);
            SoundSystemConfig.setCodec("pat", CodecLiquinth.class);
        } catch (SoundSystemException e)
        {
            ModLogger.logError("SetCodec failed ");
            e.printStackTrace();
        }
        ModLogger.debug(String.format("What codec for test.pat? %s", SoundSystemConfig.getCodec("test.pat")));
    }

    @SubscribeEvent
    public void onWorldEvent(WorldEvent.Unload event)
    {
        SoundPlayer.getInstance().stopSounds();
    }
}
