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

package net.aeronica.mods.mxtune.groups;

import net.aeronica.mods.mxtune.util.ModLogger;

import java.util.function.Supplier;

public class PlayIdSupplier
{

    public enum PlayType implements Supplier<Integer>
    {
        EVENT(500000, 599999) {@Override protected PlayIdSource next(PlayIdSource playIdSource) { return playIdSource;}},
        PERSONAL(40000, 499999) {@Override protected PlayIdSource next(PlayIdSource playIdSource) { return playIdSource;}},
        PLAYERS(300000, 399999) {@Override protected PlayIdSource next(PlayIdSource playIdSource) { return playIdSource;}},
        WORLD(200000, 299999) {@Override protected PlayIdSource next(PlayIdSource playIdSource) { return playIdSource;}},
        AREA(100000, 199999) {@Override protected PlayIdSource next(PlayIdSource playIdSource) { return playIdSource;}},
        ;

        int start;
        int end;
        PlayIdSource playIdSource;
        PlayType playType;

        private static final PlayIdSource INVALID_ID = new PlayIdSource(-1, -1);
        public static final int INVALID = -1;

        PlayType(int start, int end)
        {
            this.start = start;
            this.end = end;
            playIdSource = new PlayIdSource(start, end);
            this.playType = this;
        }

        protected PlayIdSource next(PlayIdSource playIdSource) { return INVALID_ID; }

        @Override
        public Integer get()
        {
            int id = playIdSource.get();
            ModLogger.debug("Type: %s, start: %d, end: %s, id: %d", playType, start, end, id);
            return id;
        }

    }

    private static class PlayIdSource implements Supplier<Integer>
    {
        int start;
        int end;
        int range;
        int counter;

        PlayIdSource(int startIn, int endIn)
        {
            start =  Math.min(startIn, endIn);
            end =  Math.max(startIn, endIn) + 1;
            // range = end + 1 - start
            range = Math.abs(end - start);
        }

        // bounded inclusive integer from start to end that rolls over
        @Override
        public Integer get()
        {
            return (counter++ % range) + start;
        }
    }
}
