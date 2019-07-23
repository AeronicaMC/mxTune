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

package net.aeronica.mods.mxtune.config;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class MXTuneConfig
{
    private MXTuneConfig() { /* NOP */ }

    /** Client Configuration Settings */
    public static class Client
    {
        public final VanillaMusic vanillaMusic;
        public final ConfigValue<String> site;
        public final BooleanValue showPlayerName;

        private Client(final ForgeConfigSpec.Builder builder)
        {
            builder.comment("Client Only Settings")
                    .translation("config.mxtune.category.client")
                    .push("client");

            site = builder
                    .comment("Site Links")
                    .translation("config.mxtune.mmlLink")
                    .define("site", "https://mabibeats.com/");

            showPlayerName = builder
                    .comment("Show player name in window title")
                    .translation("config.mxtune.playerNameInWindowTitle")
                    .define("showPlayerName", false);

            vanillaMusic = new VanillaMusic(builder);

            builder.pop();
        }

        public static class VanillaMusic
        {
            public final BooleanValue disableCreativeMusic;
            public final BooleanValue disableCreditsMusic;
            public final BooleanValue disableEndDragonMusic;
            public final BooleanValue disableEndMusic;
            public final BooleanValue disableGameMusic;
            public final BooleanValue disableMenuMusic;
            public final BooleanValue disableNetherMusic;

            VanillaMusic(final ForgeConfigSpec.Builder builder)
            {
                builder.comment("Vanilla Background Music")
                        .push("vanillaMusic");

                disableCreativeMusic = builder
                        .comment("Disable Creative Music")
                        .translation("config.mxtune.vm.disableCreativeMusic")
                        .define("disableCreativeMusic", false);

                disableCreditsMusic = builder
                        .comment("Disable Credits Music")
                        .translation("config.mxtune.vm.disableCreditsMusic")
                        .define("disableCreditsMusic", false);

                disableEndDragonMusic = builder
                        .comment("Disable End Dragon Music")
                        .translation("config.mxtune.vm.disableEndDragonMusic")
                        .define("disableEndDragonMusic", false);

                disableEndMusic = builder
                        .comment("Disable End Music")
                        .translation("config.mxtune.vm.disableEndMusic")
                        .define("disableEndMusic", false);

                disableGameMusic = builder
                        .comment("Disable Game Music")
                        .translation("config.mxtune.vm.disableGameMusic")
                        .define("disableGameMusic", false);

                disableMenuMusic = builder
                        .comment("Disable Menu Music")
                        .translation("config.mxtune.vm.disableMenuMusic")
                        .define("disableMenuMusic", false);

                disableNetherMusic = builder
                        .comment("Disable Nether Music")
                        .translation("config.mxtune.vm.disableNetherMusic")
                        .define("disableNetherMusic", false);

                builder.pop();
            }
        }
    }

    public static class Common
    {
        public final BooleanValue showWelcomeStatusMessage;

        public final IntValue listenerRange;

        public final IntValue groupPlayAbortDistance;

        public final BooleanValue moreDebugMessages;

        public final BooleanValue disableJAMPartyRightClick;

        private Common(final ForgeConfigSpec.Builder builder)
        {
            builder.comment("Common Configuration")
                    .translation("config.mxtune.category.general")
                    .push("common");

            showWelcomeStatusMessage = builder
                    .comment("Show Welcome Status Message")
                    .translation("config.mxtune.showWelcomeStatusMessage")
                    .define("showWelcomeStatusMessage", false);

            listenerRange = builder
                    .comment("Listener Range")
                    .translation("config.mxtune.listenerRange")
                    .defineInRange("listenerRange",24,10, 64);

            groupPlayAbortDistance = builder
                    .comment("Group Play Abort Distance")
                    .translation("config.mxtune.groupPlayAbortDistance")
                    .defineInRange("groupPlayAbortDistance", 16, 10, 24);

            moreDebugMessages = builder
                    .comment("More Debug Messages")
                    .translation("config.mxtune.moreDebugMessages")
                    .define("moreDebugMessages", false);

            disableJAMPartyRightClick = builder
                    .comment("Disable JAM Party Right Click")
                    .translation("config.mxtune.disableJAMPartyRightClick")
                    .define("disableJAMPartyRightClick", false);

            builder.pop();
        }
    }

//    @Config(modid = Reference.MOD_ID, name = Reference.MOD_ID + "/" + Reference.MOD_ID + "_recipes", type = Type.INSTANCE, category="recipe")
//    @LangKey("config.mxtune.category.recipes")
//    public static class Recipes
//    {
//        private Recipes() {/* NOP */}
//
//        @LangKey("config.mxtune.enabledRecipes")
//        @Name("Toggles")
//        @Comment({"mxTune Recipes", "Requires a Server Restart if Changed!", "B:<name>=(true|false)"})
//        @RequiresMcRestart
//        public static Map<String, Boolean> recipeToggles;
//
//        private static final String[] modItemRecipeNames = {
//                "band_amp", "bass_drum",
//                "cello", "chalumeau",
//                "cymbels", "electric_guitar", "flute", "hand_chimes",
//                "harp", "harpsichord", "harpsichord_coupled", "lute",
//                "lyre", "mandolin", "music_paper", "orchestra_set",
//                "piano", "recorder", "roncadora", "snare_drum",
//                "spinet_piano", "standard_set", "trumpet", "tuba",
//                "tuned_flute", "tuned_whistle", "ukulele",
//                "violin", "whistle"
//        };
//
//        static
//        {
//            recipeToggles = Maps.newHashMap();
//            for (String modItemRecipeName : modItemRecipeNames)
//            {
//                recipeToggles.put(modItemRecipeName, true);
//            }
//        }
//    }

    public static boolean isJAMPartyRightClickDisabled() { return COMMON.disableJAMPartyRightClick.get(); }

    public static float getListenerRange() { return (float) COMMON.listenerRange.get(); }

    public static float getGroupPlayAbortDistance() { return (float) COMMON.groupPlayAbortDistance.get(); }

    public static boolean showWelcomeStatusMessage() { return COMMON.showWelcomeStatusMessage.get(); }

    public static boolean moreDebugMessages() { return COMMON.moreDebugMessages.get(); }

    public static boolean isCreativeMusicDisabled() { return CLIENT.vanillaMusic.disableCreativeMusic.get(); }

    public static boolean isCreditsMusicDisabled() { return CLIENT.vanillaMusic.disableCreditsMusic.get(); }

    public static boolean isDragonMusicDisabled() { return CLIENT.vanillaMusic.disableEndDragonMusic.get(); }

    public static boolean isEndMusicDisabled() { return CLIENT.vanillaMusic.disableEndMusic.get(); }

    public static boolean isGameMusicDisabled() { return CLIENT.vanillaMusic.disableGameMusic.get(); }

    public static boolean isMenuMusicDisabled() { return CLIENT.vanillaMusic.disableMenuMusic.get(); }

    public static boolean isNetherMusicDisabled() { return CLIENT.vanillaMusic.disableNetherMusic.get(); }

    public static String getMmlLink() { return CLIENT.site.get(); }

//    /**
//     * Will only allow this mods recipes to be disabled
//     * @param stackIn stack to be tested
//     * @return recipe state
//     */
//    public static boolean isRecipeEnabled(ItemStack stackIn)
//    {
//        // strip off "item." or "tile." and "instrument." to get the raw item name without domain and item base names
//        String itemName = stackIn.getTranslationKey().replaceFirst("(item.|tile.)" + Reference.MOD_ID + ":", "");
//        itemName = itemName.replaceFirst("instrument.", "");
//        boolean enableState = !Recipes.recipeToggles.containsKey(itemName) || (Recipes.recipeToggles.get(itemName) && !itemName.contains(":"));
//        ModLogger.debug("Recipe Enabled? %s %s", itemName, enableState);
//        return enableState;
//    }
    
//    public static boolean isRecipeHidden(ItemStack stackIn)
//    {
//        return !isRecipeEnabled(stackIn);
//    }
    public static boolean isRecipeHidden(ItemStack stackIn)
    {
        return false;
    }

    private static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    private static final ForgeConfigSpec clientSpec;
    public static final Client CLIENT;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static void register(final ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, commonSpec);
        context.registerConfig(ModConfig.Type.CLIENT, clientSpec);
    }
}
