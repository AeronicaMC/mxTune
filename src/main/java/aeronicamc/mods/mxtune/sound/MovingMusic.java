package aeronicamc.mods.mxtune.sound;

import aeronicamc.mods.mxtune.config.MXTuneConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MovingMusic extends MxSound
{
    private static final Logger LOGGER = LogManager.getLogger(MovingMusic.class);
    private final Minecraft mc = Minecraft.getInstance();
    private final Entity entity;
    private int counter;
    private float lastDistance;

    /**
     * Implements ISound<br></br>
     * For musical machines carried or used in the world
     * @param audioData audio data associated with the entity
     * @param entity the entity that is the source of the music
     */
    public MovingMusic(AudioData audioData, Entity entity)
    {
        super(audioData);
        this.entity = entity;
        this.x = (float) entity.getX();
        this.y = (float) entity.getY();
        this.z = (float) entity.getZ();
        this.attenuation = audioData.getAudioFormat().equals(ClientAudio.AUDIO_FORMAT_STEREO) ? AttenuationType.NONE : AttenuationType.LINEAR;
        LOGGER.debug("MovingMusic entity {}, playId {}", entity.getName().getContents(), audioData.getPlayId());
    }

    @Override
    public void onUpdate()
    {
        if (!this.entity.isAlive() && !isStopped())
        {
            setDonePlaying();
            LOGGER.debug("MovingMusic playID {} done, this {}", playID, this);
        } else if (audioData != null && mc.player != null && audioData.getAudioFormat().equals(ClientAudio.AUDIO_FORMAT_3D))
        {
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();

            Vector3d vec3d = new Vector3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            float distance = (float) vec3d.distanceTo(new Vector3d(entity.getX(), entity.getY(), entity.getZ()));
            this.volumeBase = (1.0F - MathHelper.clamp(distance / MXTuneConfig.getListenerRange(), 0.0F, 1.0F));
            if ((counter++ % 20 == 0) && (distance != lastDistance))
            {
                this.lastDistance = distance;
            }
        } else
            this.volumeBase = 0.6F;
    }
}
