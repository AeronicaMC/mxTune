/**
 * Copyright {2016} Paul Boese aka Aeronica
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
package net.aeronica.mods.mxtune.advancements.criterion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class PlayInstrumentTrigger implements ICriterionTrigger<PlayInstrumentTrigger.Instance>
{

    private static final ResourceLocation ID = new ResourceLocation(MXTuneMain.MODID, "play_instrument");
    private final Map<PlayerAdvancements, Listeners> listeners = new HashMap<>();

    static class Instance extends AbstractCriterionInstance
    {
        private final Integer temp;

        Instance(Integer temp)
        {
            super(ID);
            this.temp = temp;
        }

        boolean test(Integer temp)
        {
            return this.temp == temp;
        }
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener)
    {
        listeners.computeIfAbsent(playerAdvancementsIn, Listeners::new).add(listener);   
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener)
    {
        Listeners ls = listeners.get(playerAdvancementsIn);
        if(ls != null)
        {
            ls.remove(listener);
            if(ls.isEmpty())
                listeners.remove(playerAdvancementsIn);
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn)
    {
        listeners.remove(playerAdvancementsIn);
    }

    public void trigger(EntityPlayerMP player, Integer instrument)
    {
        Listeners ls = listeners.get(player.getAdvancements());
        if(ls != null)
            ls.trigger(instrument);
    }
    
    @SuppressWarnings("unused")
    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        String name = JsonUtils.getString(json, "instrument");
        Integer instrument = ItemInstrument.EnumType.valueOf(name).getPatch();
        if(instrument == null)
            throw new JsonSyntaxException("Unknown instrument: '" + name + "'");
        else
            return new Instance(instrument);
    }

    
    static class Listeners
    {
        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listeners = new HashSet<>();

        public Listeners(PlayerAdvancements playerAdvancements)
        {
            this.playerAdvancements = playerAdvancements;
        }

        public boolean isEmpty()
        {
            return listeners.isEmpty();
        }

        public void add(Listener<Instance> listener)
        {
            listeners.add(listener);
        }

        public void remove(Listener<Instance> listener)
        {
            listeners.remove(listener);
        }

        public void trigger(Integer temp)
        {
            listeners.stream()
                .filter(listener -> listener.getCriterionInstance().test(temp))
                .collect(ImmutableList.toImmutableList()) //Need this intermediate list to avoid ConcurrentModificationException
                .forEach(listener -> listener.grantCriterion(playerAdvancements));
        }
    }
    
}
