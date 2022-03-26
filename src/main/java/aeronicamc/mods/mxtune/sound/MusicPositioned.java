package aeronicamc.mods.mxtune.sound;

import aeronicamc.mods.mxtune.config.MXTuneConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MusicPositioned extends MxSound
{
    private static final Logger LOGGER = LogManager.getLogger(MusicPositioned.class);
    private int counter;
    private float lastDistance;

    MusicPositioned(AudioData audioData)
    {
        super(audioData);
        this.attenuation = AttenuationType.LINEAR;
        BlockPos blockPos = audioData.getBlockPos();
        if (blockPos != null)
        {
            this.x = blockPos.getX();
            this.y = blockPos.getY();
            this.z = blockPos.getZ();
            this.stopped = false;
            this.volume = 1.0F;
            LOGGER.debug("MusicPositioned BlockPos {}", blockPos);
        }
    }

    @Override
    protected void onUpdate()
    {
        if (audioData != null && audioData.getBlockPos() != null && mc.player != null)
        {
            Vector3d thePlayerVec3d = new Vector3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            float distance = (float) thePlayerVec3d.distanceTo(new Vector3d(this.x, this.y, this.z));
            this.volumeBase = (1.0F - MathHelper.clamp(distance / MXTuneConfig.getListenerRange(), 0.0F, 1.0F));
            if ((counter++ % 20 == 0) && (distance != lastDistance))
            {
                this.lastDistance = distance;
            }
        } else
        {
            setDonePlaying();
            ClientAudio.queueAudioDataRemoval(playID);
            LOGGER.debug("MusicPositioned playID {} done", playID);
        }
    }
}
