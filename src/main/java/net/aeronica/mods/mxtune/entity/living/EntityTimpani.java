package net.aeronica.mods.mxtune.entity.living;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.init.ModLootTables;
import net.aeronica.mods.mxtune.sound.ModSoundEvents;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;

public class EntityTimpani extends EntitySlime
{
    
    public EntityTimpani(World worldIn)
    {
        super(worldIn);
        this.isImmuneToFire = true;
    }

    @Override
    protected boolean canDespawn()
    {
        return false;
    }
    
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20000000298023224D);
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    @Override
    public boolean getCanSpawnHere()
    {
        return this.world.getDifficulty() != EnumDifficulty.PEACEFUL;
    }

    /**
     * Checks that the entity is not colliding with any blocks / liquids
     */
    @Override
    public boolean isNotColliding()
    {
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this) && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(this.getEntityBoundingBox());
    }

    public void setSlimeSize(int size, boolean p_70799_2_)
    {
        super.setSlimeSize(size, p_70799_2_);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)(size * 3));
    }
    
//    @SideOnly(Side.CLIENT)
//    @Override
//    public int getBrightnessForRender(float partialTicks)
//    {
//        return 15728880;
//    }
//
//    /**
//     * Gets how bright this entity is.
//     */
//    @Override
//    public float getBrightness(float partialTicks)
//    {
//        return 1.0F;
//    }

//    @Override
//    protected EnumParticleTypes getParticleType()
//    {
//        return EnumParticleTypes.NOTE;
//    }

    @Override
    protected net.aeronica.mods.mxtune.entity.living.EntityTimpani createInstance()
    {
        return new net.aeronica.mods.mxtune.entity.living.EntityTimpani(this.world);
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable()
    {
        return this.isSmallSlime() ? LootTableList.EMPTY : ModLootTables.ENTITY_TIMPANI_OF_DOOM;
    }

    /**
     * Returns true if the entity is on fire. Used by render to add the fire effect on rendering.
     */
    @Override
    public boolean isBurning()
    {
        return false;
    }

    /**
     * Gets the amount of time the slime needs to wait between jumps.
     */
    @Override
    protected int getJumpDelay()
    {
        return super.getJumpDelay() * 4;
    }

    @Override
    protected void alterSquishAmount()
    {
        this.squishAmount *= 0.5F;
    }

    /**
     * Causes this entity to do an upwards motion (jumping).
     */
    @Override
    protected void jump()
    {
        this.motionY = (double)(0.22F + (float)this.getSlimeSize() * 0.1F);
        this.isAirBorne = true;
        net.minecraftforge.common.ForgeHooks.onLivingJump(this);
    }

    @Override
    protected void handleJumpLava()
    {
        this.motionY = (double)(0.22F + (float)this.getSlimeSize() * 0.05F);
        this.isAirBorne = true;
    }

    @Override
    public void fall(float distance, float damageMultiplier)
    {
    }

    @Override
    public float getEyeHeight()
    {
        return this.height * 0.8F;
    }
    
    /**
     * Indicates weather the slime is able to damage the player (based upon the slime's size)
     */
    @Override
    protected boolean canDamagePlayer()
    {
        return true;
    }
    
    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
    {
        int i = this.rand.nextInt(3);

        if (i < 2 && this.rand.nextFloat() < 0.5F * difficulty.getClampedAdditionalDifficulty())
        {
            ++i;
        }

        int j = 1 << i;
        this.setSlimeSize(j, true);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    /**
     * Gets the amount of damage dealt to the player when "attacked" by the slime.
     */
    @Override
    protected int getAttackStrength()
    {
        return super.getAttackStrength() + 2;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return this.isSmallSlime() ? ModSoundEvents.ENTITY_TINY_TIMPANI_HURT : ModSoundEvents.ENTITY_TIMPANI_HURT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return this.isSmallSlime() ? ModSoundEvents.ENTITY_TINY_TIMPANI_DEATH : ModSoundEvents.ENTITY_TIMPANI_DEATH;
    }

    @Override
    protected SoundEvent getSquishSound()
    {
        int slimeSize = this.getSlimeSize();
        //ModLogger.info("getSquishSound mobSize %d", slimeSize);
        SoundEvent soundEvent = ModSoundEvents.ENTITY_MEDIUM_TIMPANI_SQUISH;
        switch(slimeSize)
        {
        case 1:
            soundEvent = ModSoundEvents.ENTITY_TINY_TIMPANI_SQUISH;
            break;
        case 2:
            soundEvent = ModSoundEvents.ENTITY_MEDIUM_TIMPANI_SQUISH;
            break;
        case 4:
            soundEvent = ModSoundEvents.ENTITY_LARGE_TIMPANI_SQUISH;
            break;
        default:
        }
        return soundEvent;
    }

    @Override
    protected SoundEvent getJumpSound()
    {
        return ModSoundEvents.ENTITY_TIMPANI_JUMP;
    }
    
    @Override
    protected boolean spawnCustomParticles() {
      if(this.getEntityWorld().isRemote) {
        int i = this.getSlimeSize();
        for(int j = 0; j < i * 8; ++j) {
          float f = this.rand.nextFloat() * (float) Math.PI * 2.0F;
          float f1 = this.rand.nextFloat() * 0.5F + 0.5F;
          float f2 = MathHelper.sin(f) * (float) i * 0.5F * f1;
          float f3 = MathHelper.cos(f) * (float) i * 0.5F * f1;
          double d0 = this.posX + (double) f2;
          double d1 = this.posZ + (double) f3;
          double d2 = this.getEntityBoundingBox().minY;
          MXTune.proxy.spawnTimpaniParticle(this.getEntityWorld(), d0, d2, d1);
        }
      }
      return true;
    }

}
