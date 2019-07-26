/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

/* ********************************************************************
 * Changes for Aeronica's mxTune MOD:
 * Updated for MC 1.9+, added additional constructors for more control
 * over rotation and offset. Added ability to stand while riding and a
 * "data-watcher" to sync the boolean SHOULD_SIT to the client.
 * ********************************************************************
 * 
 * MrCrayfish's Furniture Mod
 * Copyright (C) 2016  MrCrayfish (http://www.mrcrayfish.com/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.aeronica.mods.mxtune.entity;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.managers.PlayManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntitySittableBlock extends Entity
{
    public static final EntityType<Entity> SITTABLE_BLOCK_ENTITY_TYPE = EntityType.Builder.create(EntityClassification.MISC).build(new ResourceLocation(Reference.MOD_ID, "sittable").toString());
    private static final DataParameter<Boolean> SHOULD_SIT = EntityDataManager.createKey(EntitySittableBlock.class, DataSerializers.BOOLEAN);
    private static final DataParameter<BlockPos> BLOCK_POS = EntityDataManager.createKey(EntitySittableBlock.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Integer> PLAY_ID = EntityDataManager.createKey(EntitySittableBlock.class, DataSerializers.VARINT);

    private BlockPos blockPos;
    private float yaw;

    public EntitySittableBlock(World worldIn)
    {
        super(SITTABLE_BLOCK_ENTITY_TYPE, worldIn);
    }

    // Allow riding standing up if shouldRiderSit is false
    public EntitySittableBlock(World world, BlockPos posIn, double yOffset, boolean shouldRiderSit)
    {
        super(SITTABLE_BLOCK_ENTITY_TYPE, world);
        this.blockPos = posIn;
        setPosition(posIn.getX() + 0.5D, posIn.getY() + yOffset, posIn.getZ() + 0.5D);
        this.dataManager.set(SHOULD_SIT, shouldRiderSit);
        this.dataManager.set(BLOCK_POS, posIn);
    }

    public EntitySittableBlock(World world, BlockPos posIn, double xOffset, double yOffset, double zOffset)
    {
        super(SITTABLE_BLOCK_ENTITY_TYPE, world);
        this.blockPos = posIn;
        setPosition(posIn.getX() + xOffset, posIn.getY() + yOffset, posIn.getZ() + zOffset);
        this.dataManager.set(SHOULD_SIT, Boolean.TRUE);
        this.dataManager.set(BLOCK_POS, posIn);
    }

    public EntitySittableBlock(World world, BlockPos posIn, double xOffset, double yOffset, double zOffset, float yaw)
    {
        super(SITTABLE_BLOCK_ENTITY_TYPE, world);
        this.blockPos = posIn;
        this.yaw = yaw;
        this.setPositionAndRotation(posIn.getX() + xOffset, posIn.getY() + yOffset, posIn.getZ() + zOffset, yaw, 0);
        this.dataManager.set(SHOULD_SIT, Boolean.TRUE);
        this.dataManager.set(BLOCK_POS, posIn);
    }

    public EntitySittableBlock(World world, BlockPos posIn, double yOffset, int rotation, double rotationOffset)
    {
        super(SITTABLE_BLOCK_ENTITY_TYPE, world);
        this.blockPos = posIn;
        setPositionConsideringRotation(posIn.getX() + 0.5D, posIn.getY() + yOffset, posIn.getZ() + 0.5D, rotation, rotationOffset);
        this.dataManager.set(SHOULD_SIT, Boolean.TRUE);
        this.dataManager.set(BLOCK_POS, posIn);
    }

    private void setPositionConsideringRotation(double xIn, double yIn, double zIn, int rotationIn, double rotationOffsetIn)
    {
        double x = xIn;
        double z = zIn;
        switch (rotationIn)
        {
            case 2:
                z += rotationOffsetIn;
                break;
            case 0:
                z -= rotationOffsetIn;
                break;
            case 3:
                x -= rotationOffsetIn;
                break;
            case 1:
                x += rotationOffsetIn;
                break;
            default:
        }
        setPosition(x, yIn, z);
    }

    @Override
    public double getMountedYOffset() {return this.getHeight() * 0.0D;}

    @Override
    protected boolean shouldSetPosAfterLoading() {return false;}

    @Override
    public void baseTick()
    {
        if (!this.world.isRemote && ((this.getPassengers().isEmpty() && !this.isAlive()) || ( this.world.isAirBlock(blockPos))))
        {
            this.remove();
            world.updateComparatorOutputLevel(getPosition(), world.getBlockState(getPosition()).getBlock());
            PlayManager.stopPlayID(dataManager.get(PLAY_ID));
        }
    }

    @Override
    protected void registerData()
    {
        this.dataManager.register(SHOULD_SIT, Boolean.TRUE);
        this.dataManager.register(BLOCK_POS, blockPos);
        this.dataManager.register(PLAY_ID, -1);
    }

    @Override
    public boolean shouldRiderSit() {return this.dataManager.get(SHOULD_SIT);}

    public BlockPos getMountedPosition() {return blockPos;}

    @SuppressWarnings("unused")
    public float getYaw() {return yaw;}
    
    public void setPlayID(@Nullable Integer playID)
    {
        int id = playID != null ? playID : -1;
        dataManager.set(PLAY_ID, id);
    }

    public Integer getPlayID()
    {
        return dataManager.get(PLAY_ID);
    }

    public BlockPos getBlockPos()
    {
        return (this.dataManager.get(BLOCK_POS)).toImmutable();
    }

    @Override
    public boolean writeUnlessRemoved(CompoundNBT compound)
    {
        return false;
    }

    @Override
    public boolean writeUnlessPassenger(CompoundNBT compound)
    {
        return false;
    }

    @Override
    public CompoundNBT writeWithoutTypeId(CompoundNBT compound)
    {
        return new CompoundNBT();
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return new SSpawnObjectPacket(this);
    }

    @Override
    public void read(CompoundNBT compound)
    {
    }

    @Override
    protected void readAdditional(CompoundNBT compound)
    {
        // NOP
    }

    @Override
    protected void writeAdditional(CompoundNBT compound)
    {
        // NOP
    }

    @Override
    public boolean equals(Object otherEntity)
    {
        // Entities are unique in each world so there should never be a case where they are equal
        // At the the super class level they are tested by their assigned entityID.
        // Overridden as a SonarQube recommendation
        return super.equals(otherEntity);
    }

    @Override
    public int hashCode()
    {
        // At the the super class level the hash code is the entityID.
        // Overridden as a SonarQube recommendation
        return super.hashCode();
    }

}
