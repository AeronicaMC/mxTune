package aeronicamc.mods.mxtune.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.logging.log4j.LogManager;

public class MusicPositioned extends MxSound
{
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc = Minecraft.getInstance();
    private int counter;
    private float lastDistance;

    MusicPositioned(AudioData audioData)
    {
        super(audioData, SoundCategory.RECORDS);
        this.attenuation = AttenuationType.LINEAR;
        BlockPos blockPos = audioData.getBlockPos();
        if (blockPos != null)
        {
            this.x = blockPos.getX();
            this.y = blockPos.getY();
            this.z = blockPos.getZ();
            this.volume = 4.0F;
        }
    }

    @Override
    protected void onUpdate()
    {
        if (audioData != null && audioData.getBlockPos() != null && mc.player != null)
        {
            Vector3d thePlayerVec3d = new Vector3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            float distance = (float) thePlayerVec3d.distanceTo(new Vector3d(this.x, this.y, this.z));
            this.volume = (float) MathHelper.clamp(Math.log((1.0D - ((1.0D/16.0D)*(distance))) + 1.0D)*1.6D, 0.0D, 1.0D);
            if ((counter++ % 20 == 0) && (distance != lastDistance))
            {
                LOGGER.debug("dist {}, volume {}", distance, volume);
                lastDistance = distance;
            }
        }
    }
}
