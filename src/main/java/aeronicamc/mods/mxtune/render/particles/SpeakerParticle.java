package aeronicamc.mods.mxtune.render.particles;

import aeronicamc.mods.mxtune.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BasicParticleType;

public class SpeakerParticle extends SpriteTexturedParticle
{
    private final float uo;
    private final float vo;

    @SuppressWarnings("deprecation")
    public SpeakerParticle(ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed)
    {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
        this.xd += pXSpeed;
        this.yd += pYSpeed;
        this.zd += pZSpeed;
        this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(new ItemStack(ModBlocks.MUSIC_BLOCK.get().asItem()), pLevel, (LivingEntity)null).getParticleIcon());
        this.gravity = 1.0F;
        this.quadSize /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uo + 1.0F) / 4.0F * 16.0F);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uo / 4.0F * 16.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0F * 16.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0F) / 4.0F * 16.0F);
    }

    @Override
    public IParticleRenderType getRenderType()
    {
        return IParticleRenderType.TERRAIN_SHEET;
    }

    public static class Factory implements IParticleFactory<BasicParticleType>
    {
        public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new SpeakerParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        }
    }
}
