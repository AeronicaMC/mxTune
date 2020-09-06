package net.aeronica.mods.mxtune.entity;

import net.minecraft.client.particle.ParticleBreaking;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityTimpaniFx extends ParticleBreaking
{

    public EntityTimpaniFx(World worldIn, double posXIn, double posYIn, double posZIn, Item item, int meta) {
        super(worldIn, posXIn, posYIn, posZIn, item, meta);
        this.particleScale /= 2F;
    }

    public EntityTimpaniFx(World worldIn, double posXIn, double posYIn, double posZIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, Item item, int meta) {
        super(worldIn, posXIn, posYIn, posZIn, xSpeedIn, ySpeedIn, zSpeedIn, item, meta);
        this.particleScale /= 2F;
    }

}
