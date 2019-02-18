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

package net.aeronica.mods.mxtune.caches;

import net.aeronica.libs.mml.core.MMLUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

@SideOnly(Side.CLIENT)
public class Simularity
{
    private Simularity() { /* NOP */ }
    
    private static double compareStrings(String stringA, String stringB)
    {
        double likeness = StringUtils.getJaroWinklerDistance(stringA, stringB);
        return likeness;
    }

    /**
     * Make the best guess for an instrument name and packed Preset
     * @param testString Instrument name
     * @return
     */
    public static Tuple<Integer, String> getPackedPresetFromName(String testString)
    {
        int packedPreset = 0;
        double lastFuzzy = 0;
        String instName = I18n.format("sf.mxtune.m.lute");
        for (UglyHack uglyHack : UglyHack.values())
        {
            String name = I18n.format(uglyHack.langKey);
            double fuzzy = compareStrings(testString, name);
            if (fuzzy > lastFuzzy)
            {
                lastFuzzy = fuzzy;
                instName = name;
                packedPreset = MMLUtil.preset2PackedPreset(uglyHack.bank, uglyHack.program);
            }
        }
        return new Tuple<>(packedPreset, instName);
    }

    public static void test()
    {
        ModLogger.info(" Piano: %s", getPackedPresetFromName("Part1-Piano.out.ms2mml").getFirst());
        ModLogger.info(" Grand Piano: %s", getPackedPresetFromName("Part1-Grand-Piano.out.ms2mml").getFirst());
        ModLogger.info(" Guitar Lute: %s", getPackedPresetFromName("Part4-Guitar-Lute.out.ms2mml").getFirst());
        ModLogger.info(" Cello: %s", getPackedPresetFromName("Part4-Cello.out.ms2mml").getFirst());
        ModLogger.info(" Lute: %s", getPackedPresetFromName("Part4-Lute.out.ms2mml").getFirst());
        ModLogger.info(" Standard Set: %s", getPackedPresetFromName("Part7-Standard.out.ms2mml").getFirst());
        ModLogger.info(" Harpsichord: %s", getPackedPresetFromName("Part7-Harpsichord.out.ms2mml").getFirst());
    }

    public enum UglyHack
    {
        LUTE(0, 0,   0, "sf.mxtune.m.lute"),
        UKUL(1, 0,   1, "sf.mxtune.m.ukulele"),
        MAND(2, 0,   2, "sf.mxtune.m.mandolin"),
        WHIS(3, 0,   3, "sf.mxtune.m.whistle"),
        RONC(4, 0,   4, "sf.mxtune.m.roncador"),
        FLUT(5, 0,   5, "sf.mxtune.m.flute"),
        CHAL(6, 0,   6, "sf.mxtune.m.chalameu"),
        TUBA(7, 0,  18, "sf.mxtune.m.tuba"),
        LYRE(8, 0,  19, "sf.mxtune.m.lyre"),
        EGUI(9, 0,  20, "sf.mxtune.m.eguitar"),
        PIAN(10, 0,  21, "sf.mxtune.m.piano"),
        VIOL(11, 0,  22, "sf.mxtune.m.violin"),
        CELL(12, 0,  23, "sf.mxtune.m.cello"),
        HARP(13, 0,  24, "sf.mxtune.m.harp"),
        TFLU(14, 0,  55, "sf.mxtune.t.flute"),
        TWHI(15,0,  56, "sf.mxtune.t.whistle"),
        BDRU(16, 0,  66, "sf.mxtune.m.bdrum"),
        SNAR(18, 0,  67, "sf.mxtune.m.snare"),
        CYMB(19, 0,  68, "sf.mxtune.m.cymbals"),
        HCHI(20, 0,  77, "sf.mxtune.m.hchime"),
        HSCD(21, 16,   6, "sf.mxtune.g.hrpsicd"),
        CSCD(22, 16,   7, "sf.mxtune.g.chrpsicd"),
        TRUM(23, 16,  56, "sf.mxtune.g.trumpet"),
        RECO(24, 16,  74, "sf.mxtune.g.recorder"),
        STDS(25, 128,   0, "sf.mxtune.p.stdset"),
        ORCS(26, 128,  48, "sf.mxtune.p.orchset"),
        ;

        int index;
        int bank;
        int program;
        String langKey;

        UglyHack(int index, int bank, int program, String lankKey)
        {
            this.index = index;
            this.bank = bank;
            this.program = program;
            this.langKey = lankKey;
        }
    }
}
