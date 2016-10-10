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
import net.aeronica.mods.mxtune.sound.MovingMusicUnRegistered;
import net.aeronica.mods.mxtune.sound.UnregisteredSound;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.Sound.Type;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.event.world.WorldEvent;
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
        ModLogger.debug("SoundSetupEvent");
        try
        {
            SoundSystemConfig.setCodec("nul", CodecPCM.class);
        } catch (SoundSystemException e)
        {
            ModLogger.logError("SetCodec failed ");
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void PlaySoundEvent(PlaySoundEvent e)
    {
        if (e.getSound().getSoundLocation().equals(ModSoundEvents.PCM_PROXY.getSoundName()) && (e.getSound() instanceof ITickableSound))
        {
            /** entityID is the player holding/wearing the sound producing item */
            Integer entityId;
            if ((entityId = ClientAudio.pollEntityIDQueue01()) == null ) return;
            EntityPlayer player = (EntityPlayer) MXTuneMain.proxy.getClientPlayer().getEntityWorld().getEntityByID(entityId);    

            // tickable sound replacement
            UnregisteredSound unRegSnd = new UnregisteredSound("pcm-proxy", 1, 1, 0, Type.SOUND_EVENT, true);
       
            MovingMusicUnRegistered tmm = new MovingMusicUnRegistered(player, unRegSnd); // Moving Sound
            tmm.setAttenuationType(e.getSound().getAttenuationType());
            tmm.setCanRepeat(e.getSound().canRepeat());
            tmm.setPitch(1);
            tmm.setRepeatDelay(e.getSound().getRepeatDelay());
            tmm.setResourceLocation(e.getSound().getSoundLocation());
            tmm.setSoundCategory(e.getSound().getCategory());
            tmm.setSoundEventAccessor(new SoundEventAccessor(e.getSound().getSoundLocation(), "mxtune.subtitle.pcm-proxy"));
            tmm.setVolume(1);
            tmm.setxPosF((float) player.posX);
            tmm.setyPosF((float) player.posY);
            tmm.setzPosF((float) player.posZ);
            e.setResultSound(tmm);
        }
    }

    @SubscribeEvent
    public void onWorldEvent(WorldEvent.Unload event)
    {
        //SoundPlayer.getInstance().stopSounds();
    }
    
}
