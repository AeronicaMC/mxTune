package aeronicamc.mods.mxtune.sound;

import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MovingMusic extends MxSound
{
    private static final Logger LOGGER = LogManager.getLogger();
    private Entity entity;

    /**
     * Implements ISound<br></br>
     * For musical machines carried or used in the world
     * @param audioData
     */
    public MovingMusic(AudioData audioData, Entity entity)
    {
        super(audioData, SoundCategory.PLAYERS);
        this.entity = entity;
        this.stopped = false;
        this.x = (float) entity.blockPosition().getX();
        this.y = (float) entity.blockPosition().getY();
        this.z = (float) entity.blockPosition().getZ();
        LOGGER.debug("MovingMusic entity {}", entity.getName().getContents());
    }

    @Override
    public void onUpdate()
    {
        if (!entity.isAlive() && !stopped)
        {
            this.stopped = true;
            ClientAudio.queueAudioDataRemoval(playID);
            LOGGER.debug("MovingMusic playID {} done", playID);
        } else
        {
            this.x = (float) entity.blockPosition().getX();
            this.y = (float) entity.blockPosition().getY();
            this.z = (float) entity.blockPosition().getZ();
        }
    }
}
