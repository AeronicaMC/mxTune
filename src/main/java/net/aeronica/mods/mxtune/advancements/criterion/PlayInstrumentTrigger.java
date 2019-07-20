/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, 2015 Pokefenn, ljfa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.aeronica.mods.mxtune.advancements.criterion;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.aeronica.mods.mxtune.Reference;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Copied and adapted to my needs from code by:
 * @author Pokefenn, ljfa
 * </br><a href="https://github.com/TeamTotemic/Totemic">https://github.com/TeamTotemic/Totemic</a>
 */
public class PlayInstrumentTrigger implements ICriterionTrigger<PlayInstrumentTrigger.Instance>
{
    private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "play_instrument");
    private final Map<PlayerAdvancements, Listeners> listeners = new HashMap<>();

    static class Instance extends AbstractCriterionInstance
    {
        private final String instrumentName;

        Instance(String instrumentName)
        {
            super(ID);
            this.instrumentName = instrumentName;
        }

        boolean test(String instrumentName)
        {
            return this.instrumentName.equals(instrumentName);
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

    public void trigger(ServerPlayerEntity player, String instrumentName)
    {
        Listeners ls = listeners.get(player.getAdvancements());
        if(ls != null)
            ls.trigger(instrumentName);
    }
    
    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        String instrumentName = JSONUtils.getString(json, "instrument");

        if(instrumentName == null)
            throw new JsonSyntaxException("Unknown instrument: '" + instrumentName + "'");
        else
            return new Instance(instrumentName);
    }

    
    static class Listeners
    {
        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listenerSet = new HashSet<>();

        public Listeners(PlayerAdvancements playerAdvancements)
        {
            this.playerAdvancements = playerAdvancements;
        }

        public boolean isEmpty()
        {
            return listenerSet.isEmpty();
        }

        public void add(Listener<Instance> listener)
        {
            listenerSet.add(listener);
        }

        public void remove(Listener<Instance> listener)
        {
            listenerSet.remove(listener);
        }

        public void trigger(String instrumentName)
        {
            listenerSet.stream()
                .filter(listener -> listener.getCriterionInstance().test(instrumentName))
                .collect(ImmutableList.toImmutableList()) //Need this intermediate list to avoid ConcurrentModificationException
                .forEach(listener -> listener.grantCriterion(playerAdvancements));
        }
    }
}
