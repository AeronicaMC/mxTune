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

package aeronicamc.mods.mxtune.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class MXTuneConfig
{
    public static int SHEET_MUSIC_MAX_DAYS = 999999;
    private MXTuneConfig() { /* NOP */ }

    /** Client Configuration Settings */
    public static class Client
    {
        public final ConfigValue<Integer> doubleClickTime;
        public final ConfigValue<String> site;

        public Client(final ForgeConfigSpec.Builder builder)
        {
            builder.comment("Client Only Settings")
                    .push("client");

            doubleClickTime = builder
                    .comment("Double-click time in milliseconds for GUI widgets")
                    .translation("config.mxtune.client.double_click_time_ms")
                    .defineInRange("doubleClickTime", 500, 10, 5000);

            site = builder
                    .comment("Site Link")
                    .translation("config.mxtune.client.mml_Link")
                    .define("site", "https://mabibeats.com/");

            builder.pop();
        }
    }

    public static class Server
    {
        public final IntValue listenerRange;

        public final ForgeConfigSpec.BooleanValue sheetMusicExpires;
        public final IntValue sheetMusicLifeInDays;

        public Server(final ForgeConfigSpec.Builder builder)
        {
            builder.comment("Server Configuration")
                    .push("server");

            listenerRange = builder
                    .comment("Listener Range defines the radius in blocks where volume drops to zero")
                    .translation("config.mxtune.server.listener_range")
                    .defineInRange("listenerRange",24,10, 64);

            sheetMusicExpires = builder
                    .comment("Sheet Music Expires")
                    .translation("config.mxtune.server.sheet_music_expires")
                    .define("sheetMusicExpires", false);

            sheetMusicLifeInDays = builder
                    .comment("Sheet Music Life in Days before it breaks. valid only when expiration is enabled.")
                    .translation("config.mxtune.server.sheet_music_life_in_days")
                    .defineInRange("sheetMusicLifeInDays", SHEET_MUSIC_MAX_DAYS, 2, SHEET_MUSIC_MAX_DAYS);

            builder.pop();
        }
    }

    public static float getListenerRange() { return SERVER.listenerRange.get(); }

    public static int getSheetMusicLifeInDays() { return SERVER.sheetMusicLifeInDays.get(); }

    public static boolean sheetMusicExpires() { return SERVER.sheetMusicExpires.get(); }

    public static int getDoubleClickTimeMS() { return CLIENT.doubleClickTime.get(); }

    public static String getMmlLink() { return CLIENT.site.get(); }

    private static final ForgeConfigSpec serverSpec;
    public static final Server SERVER;
    static {
        final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        serverSpec = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    private static final ForgeConfigSpec clientSpec;
    public static final Client CLIENT;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static void register(final ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.SERVER, serverSpec);
        context.registerConfig(ModConfig.Type.CLIENT, clientSpec);
    }
}
