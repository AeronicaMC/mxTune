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

import aeronicamc.mods.mxtune.render.IOverlayItem;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class MXTuneConfig
{
    public static final Object SYNC = new Object();
    public static final int SHEET_MUSIC_MAX_DAYS = 999999;
    private MXTuneConfig() { /* NOP */ }

    /** Client Configuration Settings */
    public static class Client
    {
        public final ConfigValue<Integer> doubleClickTime;
        public final ConfigValue<String> site;
        public final ConfigValue<IOverlayItem.Position> instrumentOverlayXPosition;
        public final ConfigValue<Integer> instrumentOverlayYPercent;
        public final ConfigValue<IOverlayItem.Position> venueToolOverlayXPosition;
        public final ConfigValue<Integer> venueToolOverlayYPercent;

        public Client(final ForgeConfigSpec.Builder builder)
        {
            builder.comment("1) GUI widgets Double-click time")
                    .push("1) GUI widgets Double-click time");
            doubleClickTime = builder
                    .comment("Double-click time in milliseconds for GUI widgets")
                    .translation("config.mxtune.client.double_click_time_ms")
                    .defineInRange("doubleClickTime", 500, 10, 5000);
            builder.pop();

            builder.comment("2) MML Site Link")
                    .push("2) MML Site Link");
            site = builder
                    .comment("MML Site Link")
                    .translation("config.mxtune.client.mml_Link")
                    .define("site", "https://mabibeats.com/");
            builder.pop();

            builder.comment("3) Instrument Overlay Positions")
                    .push("3) Instrument Overlay Positions");
            instrumentOverlayXPosition = builder
                    .comment("Instrument Overlay X Position")
                    .translation("config.mxtune.client.instrument_overlay_x_pos")
                    .defineEnum("instrumentOverlayXPosition", IOverlayItem.Position.LEFT);

            instrumentOverlayYPercent = builder
                    .comment("Instrument Overlay Y Percent down screen")
                    .translation("config.mxtune.client.instrument_overlay_y_pct")
                    .defineInRange("instrumentOverlayYPercent", 0, 0, 100);
            builder.pop();

            builder.comment("4) Venue Tool Overlay Positions")
                    .push("4) Venue Tool Overlay Positions");
            venueToolOverlayXPosition = builder
                    .comment("Venue Tool Overlay X Position")
                    .translation("config.mxtune.client.venue_tool_overlay_x_pos")
                    .defineEnum("venueToolOverlayXPosition", IOverlayItem.Position.CENTER);

            venueToolOverlayYPercent = builder
                    .comment("Venue Tool Overlay Y Percent down screen")
                    .translation("config.mxtune.client.venue_tool_overlay_y_pct")
                    .defineInRange("venueToolOverlayYPercent", 70, 0, 100);
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

    public static IOverlayItem.Position getInstrumentOverlayPosition() { return CLIENT.instrumentOverlayXPosition.get(); }

    public static int getInstrumentOverlayPercent() { return CLIENT.instrumentOverlayYPercent.get(); }

    public static IOverlayItem.Position getVenueToolOverlayPosition() { return CLIENT.venueToolOverlayXPosition.get(); }

    public static int getVenueToolOverlayPercent() { return CLIENT.venueToolOverlayYPercent.get(); }

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
