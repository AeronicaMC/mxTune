/*
 * Aeronica's mxTune MOD
 * Copyright 2020, Paul Boese a.k.a. Aeronica
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

/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.items.ItemMultiInst;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class SetMultiInstMessage extends AbstractServerMessage<SetMultiInstMessage>
{
    private int index;

    @SuppressWarnings("unused")
    public SetMultiInstMessage() {/* Required by the PacketDispatcher */}

    public SetMultiInstMessage(int index)
    {
        this.index = index;
    }
    
    @Override
    protected void read(PacketBuffer buffer)
    {
        index = buffer.readInt();
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeInt(index);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (player.getHeldItemMainhand().getItem() instanceof ItemMultiInst)
        {
            if (index >= 0 && index < SoundFontProxyManager.soundFontProxyMapByIndex.size())
            {
                player.getHeldItemMainhand().setItemDamage(index);
                ModLogger.debug("Set Instrument: %d, $s", index, SoundFontProxyManager.getName(index));
            }
            else
            {
                ModLogger.debug("Set Instrument index out of range: %d", index);
            }
        }
    }
}
