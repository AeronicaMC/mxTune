/**
 * Aeronica's mxTune MOD
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
package net.aeronica.mods.mxtune.options;

import java.util.UUID;

public class PlayerLists
{
    String playerName;
    UUID uuid;
    Type type;
    boolean isOnline;
    

    public String getPlayerName() {return playerName;}

    public void setPlayerName(String playerName) {this.playerName = playerName;}

    public UUID getUuid() {return uuid;}

    public void setUuid(UUID uuid) {this.uuid = uuid;}

    public Type getType() {return type;}

    public void setType(Type type) {this.type = type;}

    public boolean isOnline() {return isOnline;}

    public void setOnline(boolean isOnline) {this.isOnline = isOnline;}

    public static enum Type {
        THE_PLAYER,
        PLAYER_LIST,
        WHITE_LIST,
        BLACK_LIST;
        
        private static final Type[] TYPE_LOOKUP = new Type[values().length];
        static
        {
            for (Type value : values())
            {
                TYPE_LOOKUP[value.ordinal()] = value;
            }
        }

        @Override
        public String toString(){return this.name();}  
    }
}
