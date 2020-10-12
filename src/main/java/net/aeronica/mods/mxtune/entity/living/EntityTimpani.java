package net.aeronica.mods.mxtune.entity.living;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.init.ModLootTables;
import net.aeronica.mods.mxtune.sound.ModSoundEvents;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearest;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;

public class EntityTimpani extends EntityLiving implements IMob
{
    private static final DataParameter<Integer> TIMPANI_SIZE = EntityDataManager.<Integer>createKey(EntityTimpani.class, DataSerializers.VARINT);
    public float squishAmount;
    public float squishFactor;
    public float prevSquishFactor;
    private boolean wasOnGround;

    public EntityTimpani(World worldIn)
    {
        super(worldIn);
        this.moveHelper = new TimpaniMoveHelper(this);
    }

    @Override
    protected void initEntityAI()
    {
        this.tasks.addTask(1, new EntityTimpani.AITimpaniFloat(this));
        this.tasks.addTask(2, new EntityTimpani.AISlimeAttack(this));
        this.tasks.addTask(3, new EntityTimpani.AITimpaniFaceRandom(this));
        this.tasks.addTask(5, new EntityTimpani.AITimpaniHop(this));
        this.targetTasks.addTask(1, new EntityAIFindEntityNearestPlayer(this));
        this.targetTasks.addTask(3, new EntityAIFindEntityNearest(this, EntityIronGolem.class));
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(TIMPANI_SIZE, Integer.valueOf(1));
    }

    protected void setTimpaniSize(int size, boolean resetHealth)
    {
        this.dataManager.set(TIMPANI_SIZE, Integer.valueOf(size));
        this.setSize(0.51000005F * (float)size, 0.51000005F * (float)size);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)(size * size));
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)(0.2F + 0.1F * (float)size));

        if (resetHealth)
        {
            this.setHealth(this.getMaxHealth());
        }

        this.experienceValue = size;
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)(size * 3));
    }

    /**
     * Returns the size of the timpani.
     */
    public int getTimpaniSize()
    {
        return ((Integer)this.dataManager.get(TIMPANI_SIZE)).intValue();
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        compound.setInteger("Size", this.getTimpaniSize() - 1);
        compound.setBoolean("wasOnGround", this.wasOnGround);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        int i = compound.getInteger("Size");

        if (i < 0)
        {
            i = 0;
        }

        this.setTimpaniSize(i + 1, false);
        this.wasOnGround = compound.getBoolean("wasOnGround");
    }

    public boolean isSmallSlime()
    {
        return this.getTimpaniSize() <= 1;
    }

    protected EnumParticleTypes getParticleType()
    {
        return EnumParticleTypes.NOTE;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        if (!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.getTimpaniSize() > 0)
        {
            this.isDead = true;
        }

        this.squishFactor += (this.squishAmount - this.squishFactor) * 0.5F;
        this.prevSquishFactor = this.squishFactor;
        super.onUpdate();

        if (this.onGround && !this.wasOnGround)
        {
            int i = this.getTimpaniSize();
            if (spawnCustomParticles()) { i = 0; } // don't spawn particles if it's handled by the implementation itself
            for (int j = 0; j < i * 8; ++j)
            {
                float f = this.rand.nextFloat() * ((float)Math.PI * 2F);
                float f1 = this.rand.nextFloat() * 0.5F + 0.5F;
                float f2 = MathHelper.sin(f) * (float)i * 0.5F * f1;
                float f3 = MathHelper.cos(f) * (float)i * 0.5F * f1;
                World world = this.world;
                EnumParticleTypes enumparticletypes = this.getParticleType();
                double d0 = this.posX + (double)f2;
                double d1 = this.posZ + (double)f3;
                world.spawnParticle(enumparticletypes, d0, this.getEntityBoundingBox().minY, d1, 0.0D, 0.0D, 0.0D);
            }

            this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
            this.squishAmount = -0.5F;
        }
        else if (!this.onGround && this.wasOnGround)
        {
            this.squishAmount = 1.0F;
        }

        this.wasOnGround = this.onGround;
        this.alterSquishAmount();
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
        BlockPos blockpos = new BlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ));
        Chunk chunk = this.world.getChunk(blockpos);

        if (this.world.getWorldInfo().getTerrainType().handleSlimeSpawnReduction(rand, world))
        {
            return false;
        }
        else
        {
            if (this.world.getDifficulty() != EnumDifficulty.PEACEFUL)
            {
                Biome biome = this.world.getBiome(blockpos);

                if (biome == Biomes.PLAINS && this.posY > 50.0D && this.posY < 70.0D && this.rand.nextFloat() < 0.5F && this.rand.nextFloat() < this.world.getCurrentMoonPhaseFactor() && this.world.getLightFromNeighbors(new BlockPos(this)) <= this.rand.nextInt(8))
                {
                    return super.getCanSpawnHere();
                }

                if (this.rand.nextInt(10) == 0 && chunk.getRandomWithSeed(987234911L).nextInt(10) == 0 && this.posY < 40.0D)
                {
                    return super.getCanSpawnHere();
                }
            }

            return false;
        }
    }

    /**
     * Checks that the entity is not colliding with any blocks / liquids
     */
    @Override
    public boolean isNotColliding()
    {
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this) && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(this.getEntityBoundingBox());
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
     * Gets the amount of time the timpani needs to wait between jumps.
     */
    protected int getJumpDelay()
    {
        return (this.rand.nextInt(20) + 10) * 4;
    }

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
        this.motionY = (double)(0.22F + (float)this.getTimpaniSize() * 0.1F);
        this.isAirBorne = true;
        net.minecraftforge.common.ForgeHooks.onLivingJump(this);
    }

    @Override
    protected void handleJumpLava()
    {
        this.motionY = (double)(0.22F + (float)this.getTimpaniSize() * 0.05F);
        this.isAirBorne = true;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        if (TIMPANI_SIZE.equals(key))
        {
            int i = this.getTimpaniSize();
            this.setSize(0.51000005F * (float)i, 0.51000005F * (float)i);
            this.rotationYaw = this.rotationYawHead;
            this.renderYawOffset = this.rotationYawHead;

            if (this.isInWater() && this.rand.nextInt(20) == 0)
            {
                this.doWaterSplashEffect();
            }
        }

        super.notifyDataManagerChange(key);
    }

    /**
     * Will get destroyed next tick.
     */
    @Override
    public void setDead()
    {
        int i = this.getTimpaniSize();

        if (!this.world.isRemote && i > 1 && this.getHealth() <= 0.0F)
        {
            int j = 2 + this.rand.nextInt(3);

            for (int k = 0; k < j; ++k)
            {
                float f = ((float)(k % 2) - 0.5F) * (float)i / 4.0F;
                float f1 = ((float)(k / 2) - 0.5F) * (float)i / 4.0F;
                EntityTimpani entityTimpani = this.createInstance();

                if (this.hasCustomName())
                {
                    entityTimpani.setCustomNameTag(this.getCustomNameTag());
                }

                if (this.isNoDespawnRequired())
                {
                    entityTimpani.enablePersistence();
                }

                entityTimpani.setTimpaniSize(i / 2, true);
                entityTimpani.setLocationAndAngles(this.posX + (double)f, this.posY + 0.5D, this.posZ + (double)f1, this.rand.nextFloat() * 360.0F, 0.0F);
                this.world.spawnEntity(entityTimpani);
            }
        }

        super.setDead();
    }

    /**
     * Applies a velocity to the entities, to push them away from eachother.
     */
    @Override
    public void applyEntityCollision(Entity entityIn)
    {
        super.applyEntityCollision(entityIn);

        if (entityIn instanceof EntityIronGolem && this.canDamagePlayer())
        {
            this.dealDamage((EntityLivingBase)entityIn);
        }
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    @Override
    public void onCollideWithPlayer(EntityPlayer entityIn)
    {
        if (this.canDamagePlayer())
        {
            this.dealDamage(entityIn);
        }
    }

    protected void dealDamage(EntityLivingBase entityIn)
    {
        int i = this.getTimpaniSize();

        if (this.canEntityBeSeen(entityIn) && this.getDistanceSq(entityIn) < 0.6D * (double)i * 0.6D * (double)i && entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)this.getAttackStrength()))
        {
            this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            this.applyEnchantments(this, entityIn);
        }
    }

    @Override
    public float getEyeHeight()
    {
        return this.height * 0.8F;
    }
    
    /**
     * Indicates weather the timpani is able to damage the player (based upon the timpani's size)
     */
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
        this.setTimpaniSize(j, true);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    /**
     * Gets the amount of damage dealt to the player when "attacked" by the timpani.
     */
    protected int getAttackStrength()
    {
        return this.getTimpaniSize() + 2;
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    @Override
    protected float getSoundVolume()
    {
        return 0.4F * (float)this.getTimpaniSize();
    }

    /**
     * The speed it takes to move the entityliving's rotationPitch through the faceEntity method. This is only currently
     * use in wolves.
     */
    @Override
    public int getVerticalFaceSpeed()
    {
        return 0;
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

    protected SoundEvent getSquishSound()
    {
        int timpaniSize = this.getTimpaniSize();
        SoundEvent soundEvent = ModSoundEvents.ENTITY_MEDIUM_TIMPANI_SQUISH;
        switch(timpaniSize)
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

    protected SoundEvent getJumpSound()
    {
        return ModSoundEvents.ENTITY_TIMPANI_JUMP;
    }

    protected boolean spawnCustomParticles() {
      if(this.getEntityWorld().isRemote) {
        int i = this.getTimpaniSize();
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

    /**
     * Returns true if the timpani makes a sound when it jumps (based upon the timpani's size)
     */
    protected boolean makesSoundOnJump()
    {
        return this.getTimpaniSize() > 0;
    }

    static class AISlimeAttack extends EntityAIBase
    {
        private final EntityTimpani timpani;
        private int growTieredTimer;

        public AISlimeAttack(EntityTimpani timpaniIn)
        {
            this.timpani = timpaniIn;
            this.setMutexBits(2);
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        public boolean shouldExecute()
        {
            EntityLivingBase entitylivingbase = this.timpani.getAttackTarget();

            if (entitylivingbase == null)
            {
                return false;
            }
            else if (!entitylivingbase.isEntityAlive())
            {
                return false;
            }
            else
            {
                return !(entitylivingbase instanceof EntityPlayer) || !((EntityPlayer)entitylivingbase).capabilities.disableDamage;
            }
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void startExecuting()
        {
            this.growTieredTimer = 300;
            super.startExecuting();
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean shouldContinueExecuting()
        {
            EntityLivingBase entitylivingbase = this.timpani.getAttackTarget();

            if (entitylivingbase == null)
            {
                return false;
            }
            else if (!entitylivingbase.isEntityAlive())
            {
                return false;
            }
            else if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer)entitylivingbase).capabilities.disableDamage)
            {
                return false;
            }
            else
            {
                return --this.growTieredTimer > 0;
            }
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void updateTask()
        {
            this.timpani.faceEntity(this.timpani.getAttackTarget(), 10.0F, 10.0F);
            ((TimpaniMoveHelper)this.timpani.getMoveHelper()).setDirection(this.timpani.rotationYaw, this.timpani.canDamagePlayer());
        }
    }

    static class AITimpaniFaceRandom extends EntityAIBase
    {
        private final EntityTimpani timpani;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public AITimpaniFaceRandom(EntityTimpani timpaniIn)
        {
            this.timpani = timpaniIn;
            this.setMutexBits(2);
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        public boolean shouldExecute()
        {
            return this.timpani.getAttackTarget() == null && (this.timpani.onGround || this.timpani.isInWater() || this.timpani.isInLava() || this.timpani.isPotionActive(MobEffects.LEVITATION));
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void updateTask()
        {
            if (--this.nextRandomizeTime <= 0)
            {
                this.nextRandomizeTime = 40 + this.timpani.getRNG().nextInt(60);
                this.chosenDegrees = (float)this.timpani.getRNG().nextInt(360);
            }

            ((TimpaniMoveHelper)this.timpani.getMoveHelper()).setDirection(this.chosenDegrees, false);
        }
    }

    static class AITimpaniFloat extends EntityAIBase
    {
        private final EntityTimpani timpani;

        public AITimpaniFloat(EntityTimpani timpaniIn)
        {
            this.timpani = timpaniIn;
            this.setMutexBits(5);
            ((PathNavigateGround)timpaniIn.getNavigator()).setCanSwim(true);
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        public boolean shouldExecute()
        {
            return this.timpani.isInWater() || this.timpani.isInLava();
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void updateTask()
        {
            if (this.timpani.getRNG().nextFloat() < 0.8F)
            {
                this.timpani.getJumpHelper().setJumping();
            }

            ((TimpaniMoveHelper)this.timpani.getMoveHelper()).setSpeed(1.2D);
        }
    }

    static class AITimpaniHop extends EntityAIBase
    {
        private final EntityTimpani timpani;

        public AITimpaniHop(EntityTimpani timpaniIn)
        {
            this.timpani = timpaniIn;
            this.setMutexBits(5);
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        public boolean shouldExecute()
        {
            return true;
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void updateTask()
        {
            ((TimpaniMoveHelper)this.timpani.getMoveHelper()).setSpeed(1.0D);
        }
    }

    static class TimpaniMoveHelper extends EntityMoveHelper
    {
        private float yRot;
        private int jumpDelay;
        private final EntityTimpani timpani;
        private boolean isAggressive;

        public TimpaniMoveHelper(EntityTimpani timpaniIn)
        {
            super(timpaniIn);
            this.timpani = timpaniIn;
            this.yRot = 180.0F * timpaniIn.rotationYaw / (float)Math.PI;
        }

        public void setDirection(float p_179920_1_, boolean p_179920_2_)
        {
            this.yRot = p_179920_1_;
            this.isAggressive = p_179920_2_;
        }

        public void setSpeed(double speedIn)
        {
            this.speed = speedIn;
            this.action = EntityMoveHelper.Action.MOVE_TO;
        }

        public void onUpdateMoveHelper()
        {
            this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, this.yRot, 90.0F);
            this.entity.rotationYawHead = this.entity.rotationYaw;
            this.entity.renderYawOffset = this.entity.rotationYaw;

            if (this.action != EntityMoveHelper.Action.MOVE_TO)
            {
                this.entity.setMoveForward(0.0F);
            }
            else
            {
                this.action = EntityMoveHelper.Action.WAIT;

                if (this.entity.onGround)
                {
                    this.entity.setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));

                    if (this.jumpDelay-- <= 0)
                    {
                        this.jumpDelay = this.timpani.getJumpDelay();

                        if (this.isAggressive)
                        {
                            this.jumpDelay /= 3;
                        }

                        this.timpani.getJumpHelper().setJumping();

                        if (this.timpani.makesSoundOnJump())
                        {
                            this.timpani.playSound(this.timpani.getJumpSound(), this.timpani.getSoundVolume(), ((this.timpani.getRNG().nextFloat() - this.timpani.getRNG().nextFloat()) * 0.2F + 1.0F) * 0.8F);
                        }
                    }
                    else
                    {
                        this.timpani.moveStrafing = 0.0F;
                        this.timpani.moveForward = 0.0F;
                        this.entity.setAIMoveSpeed(0.0F);
                    }
                }
                else
                {
                    this.entity.setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
                }
            }
        }
    }
}
