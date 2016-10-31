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


import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.CodecPCM;
import net.aeronica.mods.mxtune.sound.ModSoundEvents;
import net.aeronica.mods.mxtune.sound.MusicBackground;
import net.aeronica.mods.mxtune.sound.MusicMoving;
import net.aeronica.mods.mxtune.sound.MusicPositioned;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;

public class SoundEventHandler
{

    private SoundEventHandler() {}
    private static class SoundEventHandlerHolder {private static final SoundEventHandler INSTANCE = new SoundEventHandler();}
    public static SoundEventHandler getInstance() {return SoundEventHandlerHolder.INSTANCE;}

    @SubscribeEvent
    public void SoundSetupEvent(SoundSetupEvent event) throws SoundSystemException
    {
        SoundSystemConfig.setCodec("nul", CodecPCM.class);
    }

    @SubscribeEvent
    public void PlaySoundEvent(PlaySoundEvent e)
    {
        /* Testing for a the PCM_PROXY sound. For playing MML though the MML->PCM ClientAudio chain */
        if (e.getSound().getSoundLocation().equals(ModSoundEvents.PCM_PROXY.getSoundName()))
        {
            /* entityID is the player holding/wearing/using the sound producing item */
            Integer entityID;
            if ((entityID = ClientAudio.pollEntityIDQueue01()) != null)
            {
                EntityPlayer playerPlaying = ClientAudio.getEntityPlayer(entityID);
                /* 
                 * --Sound Replacement--
                 */
                if (entityID == MXTuneMain.proxy.getClientPlayer().getEntityId())
                {
                    /*
                     * ThePlayer(s) hear their own music without any 3D distance
                     * effects applied. Not using the built-in background music
                     * feature here because it's managed by vanilla and might interrupt
                     * the players music. Doing this also eliminates a pulsing effect
                     * that occurs when the player moves and 3D sound system updates
                     * the sound position.
                     */
                    e.setResultSound(new MusicBackground(playerPlaying));
                }
                else if (ClientAudio.isPlaced(entityID))
                {
                    /*
                     * Positioned music source for instruments that are placed in the world -OR- a GROUP of players JAMMING.
                     */
                    e.setResultSound(new MusicPositioned(playerPlaying, ClientAudio.getBlockPos(entityID)));
                }
                else
                {
                    /*
                     * Moving music source for hand held or worn instruments. 
                     */
                    e.setResultSound(new MusicMoving(playerPlaying));
                }
            }
        }
    }
    
}
