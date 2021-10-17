package aeronicamc.mods.mxtune.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MovingMusic extends MxSound
{
    private static final Logger LOGGER = LogManager.getLogger();
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
        super(audioData, SoundCategory.PLAYERS);
        this.entity = entity;
        this.stopped = false;
        this.x = (float) entity.getX();
        this.y = (float) entity.getY();
        this.z = (float) entity.getZ();
        LOGGER.debug("MovingMusic entity {}", entity.getName().getContents());
    }

    @Override
    public void onUpdate()
    {
        if (audioData == null || !entity.isAlive() || stopped)
        {
            this.stopped = true;
            ClientAudio.queueAudioDataRemoval(playID);
            LOGGER.debug("MovingMusic playID {} done", playID);
        } else if (mc.player != null)
        {
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();

            Vector3d vec3d = new Vector3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            float distance = (float) vec3d.distanceTo(new Vector3d(entity.getX(), entity.getY(), entity.getZ()));
            double log = (MathHelper.clamp(Math.log((1.0D - ((1.0D/16.0D)*(distance))) + 1.0D)*1.6D, 0.0D, 1.0D));
            this.volume = (float) MathHelper.clamp(log, 0.0d, 1.0d);
            if ((counter++ % 20 == 0) && (distance != lastDistance))
            {
                LOGGER.debug("log {}, dist {}, volume {}", log, distance, volume);
                lastDistance = distance;
            }
        }
    }
}
