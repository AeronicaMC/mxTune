package net.aeronica.mods.mxtune.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.logging.log4j.LogManager;

public class MusicPositioned extends MxSound
{
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    private Minecraft mc = Minecraft.getInstance();
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
            Vector3d vec3d = new Vector3d(mc.player.blockPosition().getX(), mc.player.blockPosition().getY(), mc.player.blockPosition().getZ());
            BlockPos blockPos = audioData.getBlockPos();
            float distance = (float) vec3d.distanceTo(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            this.volume = (float) MathHelper.clamp(MathHelper.lerp(MathHelper.clamp((4 / (distance + .001)), 0.0F, 1F), -1, 4), 0, 4);
            if ((counter++ % 20 == 0) && (distance != lastDistance))
            {
                LOGGER.debug("PosSound {}, dist {}, volume {}", audioData.getBlockPos(), distance, volume);
                lastDistance = distance;
            }
        }
    }
}
