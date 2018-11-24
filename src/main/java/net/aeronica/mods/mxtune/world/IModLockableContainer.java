/*
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.world;

import net.minecraft.world.IWorldNameable;
import net.minecraft.world.LockCode;

public interface IModLockableContainer extends IWorldNameable
{
    boolean isLocked();

    void setLockCode(LockCode code);

    LockCode getLockCode();

    boolean isOwner();

    void setOwner(OwnerUUID ownerUUID);

    OwnerUUID getOwner();
}
